/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.ContentResolver
import android.content.Context
import android.view.View
import com.aliucord.Http
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.GsonUtils
import com.aliucord.utils.RNSuperProperties
import com.discord.api.message.Message
import com.discord.api.premium.PremiumTier
import com.discord.models.user.User
import com.discord.restapi.RestAPIParams
import com.discord.restapi.utils.CountingRequestBody
import com.discord.stores.*
import com.discord.utilities.messagesend.`MessageQueue$doSend$2`
import com.discord.utilities.premium.PremiumUtils
import com.discord.utilities.rest.AttachmentRequestBody
import com.discord.utilities.rest.SendUtils
import com.lytefast.flexinput.model.Attachment
import de.robv.android.xposed.XposedBridge
import rx.subjects.BehaviorSubject
import b.a.a.c as ImageUploadFailedDialog

internal class UploadSize : CorePlugin(Manifest("UploadSize")) {
    override val isHidden = true
    override val isRequired = true

    @Suppress("PropertyName", "unused")
    private companion object {
        const val DEFAULT_MAX_FILE_SIZE = 25
        var id = 1

        class InitAttachmentUpload(val files: Array<File>) {
            class File(val filename: String, val file_size: Long, val id: String)
        }

        class InitAttachmentUploadRes(val attachments: Array<File>) {
            class File(val upload_url: String, val upload_filename: String)
        }

        class MessagePayload(
            val content: String,
            val channelId: String,
            val type: Int,
            val messageReference: RestAPIParams.Message.MessageReference?,
            val allowedMentions: RestAPIParams.Message.AllowedMentions?,
            val attachments: List<Attachment>,
            val nonce: String
        ) {
            class Attachment(val id: String, val filename: String, val uploadedFilename: String)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun start(context: Context) {
        patcher.instead<PremiumUtils>("getGuildMaxFileSizeMB", Int::class.java) { (_, tier: Int) ->
            when (tier) {
                2 -> 50
                3 -> 100
                else -> DEFAULT_MAX_FILE_SIZE
            }
        }

        patcher.instead<PremiumUtils>("getMaxFileSizeMB", User::class.java) { (_, user: User) ->
            when (user.premiumTier!!) {
                PremiumTier.TIER_1 -> 50 // Nitro Classic
                PremiumTier.TIER_2 -> 500 // Nitro
                else -> DEFAULT_MAX_FILE_SIZE
            }
        }

        patcher.instead<ImageUploadFailedDialog>("onViewBound", View::class.java) {
            val maxFileSize = argumentsOrDefault.getInt("PARAM_MAX_FILE_SIZE_MB")

            argumentsOrDefault.putInt("PARAM_MAX_FILE_SIZE_MB", 8)

            XposedBridge.invokeOriginalMethod(it.method, it.thisObject, it.args)

            @Suppress("SetTextI18n")
            g().j.text = "Max file size is $maxFileSize MB"

            null
        }

        val countingRequestBody = CountingRequestBody::class.java
        val delegate = countingRequestBody.getDeclaredField("delegate").apply { isAccessible = true }
        val bytesWrittenSubject = countingRequestBody.getDeclaredField("bytesWrittenSubject").apply { isAccessible = true }

        val attachmentRequestBody = AttachmentRequestBody::class.java
        val contentResolverField = attachmentRequestBody.getDeclaredField("contentResolver").apply { isAccessible = true }
        val attachmentField = attachmentRequestBody.getDeclaredField("attachment").apply { isAccessible = true }

        patcher.before<`MessageQueue$doSend$2`<*, *>>("call", SendUtils.SendPayload.ReadyToSend::class.java) {
            val payload = it.args[0] as SendUtils.SendPayload.ReadyToSend
            if (payload.uploads.isEmpty()) return@before

            val channelId = `$message`.channelId
            val attachments = ArrayList<MessagePayload.Attachment>(payload.uploads.size)
            for (upload in payload.uploads) {
                val countingReqBody = upload.part.b
                val reqBody = delegate[countingReqBody]
                val contentResolver = contentResolverField[reqBody] as ContentResolver
                val attachment = attachmentField[reqBody] as Attachment<*>

                contentResolver.openInputStream(attachment.uri)?.use { inputStream ->
                    val initReq = Http.Request.newDiscordRNRequest("/channels/$channelId/attachments", "POST")
                        .executeWithJson(InitAttachmentUpload(arrayOf(
                            InitAttachmentUpload.File(upload.name, upload.contentLength, (++id).toString())
                        )))
                    if (!initReq.ok()) {
                        logger.error("Failed to upload ${initReq.statusCode}: ${initReq.text()}", null)
                        return@before // fallbacks to legacy upload
                    }
                    val initRes = initReq.json(InitAttachmentUploadRes::class.java).attachments[0]

                    val uploadReq = Http.Request(initRes.upload_url, "PUT")
                        .setHeader("User-Agent", RNSuperProperties.userAgent)
                        .setHeader("Content-Type", upload.mimeType)
                        .setHeader("Content-Length", upload.contentLength.toString())
                    uploadReq.conn.doOutput = true
                    uploadReq.conn.setFixedLengthStreamingMode(upload.contentLength)

                    uploadReq.conn.outputStream.use { outputStream ->
                        var totalBytes: Long = 0
                        var currentBytes: Int
                        val buffer = ByteArray(8192)
                        val subject = bytesWrittenSubject[countingReqBody] as BehaviorSubject<Long>
                        while (inputStream.read(buffer).also { b -> currentBytes = b } > 0) {
                            outputStream.write(buffer, 0, currentBytes)
                            totalBytes += currentBytes
                            subject.onNext(totalBytes)
                        }
                        outputStream.flush()
                    }
                    uploadReq.execute().run {
                        if (!ok()) {
                            logger.error("Failed to upload ${statusCode}: ${text()}", null)
                            return@before
                        }
                    }

                    attachments.add(MessagePayload.Attachment(attachments.size.toString(), upload.name, initRes.upload_filename))
                }
            }

            payload.message.run {
                it.result = BehaviorSubject.l0(
                    Http.Request.newDiscordRNRequest("/channels/$channelId/messages", "POST").executeWithJson(
                        GsonUtils.gsonRestApi,
                        MessagePayload(
                            content.trimEnd(),
                            channelId.toString(),
                            if (messageReference == null) 0 else 19,
                            messageReference,
                            allowedMentions,
                            attachments,
                            nonce
                        )
                    ).json(GsonUtils.gsonRestApi, Message::class.java)
                )
            }
        }
    }

    override fun stop(context: Context) {}
}
