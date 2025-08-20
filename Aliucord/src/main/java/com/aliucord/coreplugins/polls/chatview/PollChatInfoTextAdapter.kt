package com.aliucord.coreplugins.polls.chatview

import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.coreplugins.polls.chatview.PollChatView.State
import kotlin.random.Random

internal class PollChatInfoTextAdapter(private val infoText: TextView) {
    private companion object {
        const val MINUTE = 60
        const val HOUR = MINUTE * 60
        const val DAY = HOUR * 24
    }

    private var currentLoopId: Int? = null
    private var model: PollChatView.Model? = null

    private fun getTimeString(): CharSequence? = model?.expiry?.let {
        val diffInSeconds = ((it - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
        val formatted = when {
            diffInSeconds >= DAY -> "${diffInSeconds / DAY}d"
            diffInSeconds >= HOUR -> "${diffInSeconds / HOUR}h"
            diffInSeconds >= MINUTE -> "${diffInSeconds / MINUTE}m"
            else -> "${diffInSeconds}s"
        }

        "$formatted left"
    }

    private fun refresh() {
        val model = model ?: return
        val expiryText = when {
            model.finalised -> "Poll closed"
            model.state == State.CLOSED -> "Poll closing"
            else -> getTimeString()
        }

        val append = expiryText?.let { "  •  $expiryText" }.orEmpty()
        infoText.text = "${model.totalVotes} vote${if (model.totalVotes != 1) "s" else ""}$append"
    }

    fun configure(model: PollChatView.Model) {
        this.model = model
        start()
        if (model.state == State.CLOSED) {
            stop()
        }
    }

    fun start() {
        val loopId = Random.nextInt()
        currentLoopId = loopId

        Utils.threadPool.execute {
            do {
                Utils.mainThread.post { refresh() }
                Thread.sleep(1000)
            } while (loopId == currentLoopId)
        }
    }

    fun stop() {
        currentLoopId = null
    }
}
