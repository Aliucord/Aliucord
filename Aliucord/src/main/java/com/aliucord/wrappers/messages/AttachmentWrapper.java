/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.wrappers.messages;

import com.discord.api.message.attachment.MessageAttachment;
import com.discord.api.message.attachment.MessageAttachmentType;

/**
 * Wraps the obfuscated {@link MessageAttachment} class to provide nice method names and require only one central
 * update if method names change after an update
 */
@SuppressWarnings({"unused", "deprecation"})
public class AttachmentWrapper {
    private final MessageAttachment attachment;

    public AttachmentWrapper(MessageAttachment attachment) {
        this.attachment = attachment;
    }

    public final String getFilename() {
        return getFilename(attachment);
    }

    public final Integer getHeight() {
        return getHeight(attachment);
    }

    public final String getProxyUrl() {
        return getProxyUrl(attachment);
    }

    public final long getSize() {
        return getSize(attachment);
    }

    public final MessageAttachmentType getType() {
        return getType(attachment);
    }

    public final String getUrl() {
        return getUrl(attachment);
    }

    public final Integer getWidth() {
        return getWidth(attachment);
    }



    public static String getFilename(MessageAttachment attachment) {
        return attachment.a();
    }

    public static Integer getHeight(MessageAttachment attachment) {
        return attachment.b();
    }

    public static String getProxyUrl(MessageAttachment attachment) {
        return attachment.c();
    }

    public static long getSize(MessageAttachment attachment) {
        return attachment.d();
    }

    public static MessageAttachmentType getType(MessageAttachment attachment) {
        return attachment.e();
    }

    public static String getUrl(MessageAttachment attachment) {
        return attachment.f();
    }

    public static Integer getWidth(MessageAttachment attachment) {
        return attachment.g();
    }
}
