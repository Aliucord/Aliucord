package com.aliucord.coreplugins.polls

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.coreplugins.polls.PollChatView.State
import com.discord.api.utcdatetime.UtcDateTime
import kotlin.random.Random

internal class PollChatInfoTextAdapter(private val ctx: Context, private val infoText: TextView) {
    private companion object {
        const val MINUTE = 60
        const val HOUR = MINUTE * 60
        const val DAY = HOUR * 24
    }
    private var state: State = State.FINALISED
    private var voteCount: Int = 0
    private var expiry: UtcDateTime? = null

    private var currentLoopId: Int? = null
    private val shouldRun
        get() = (state != State.FINALISED) && (state != State.CLOSED)

    private fun getTimeString(): CharSequence? = expiry?.let {
        val diffInSeconds = ((it.g() - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
        val formatted =
            if (diffInSeconds >= DAY)
                "${diffInSeconds / DAY}d"
            else if (diffInSeconds >= HOUR)
                "${diffInSeconds / HOUR}h"
            else if (diffInSeconds >= MINUTE)
                "${diffInSeconds / MINUTE}m"
            else
                "${diffInSeconds}s"

        "$formatted left"
    }

    @SuppressLint("SetTextI18n")
    private fun refresh() {
        val expiryText = if (state == State.FINALISED)
            "Poll closed"
        else if (state == State.CLOSED)
            "Poll closing"
        else
            getTimeString()

        val append = expiryText?.let { "  •  $expiryText" } ?: ""
        infoText.text = "$voteCount vote${if (voteCount != 1) "s" else ""}$append"
    }

    fun updateData(state: State, voteCount: Int, expiry: UtcDateTime?) {
        this.state = state
        this.voteCount = voteCount
        this.expiry = expiry

        start()
    }

    fun start() {
        val loopId = Random.nextInt()
        currentLoopId = loopId
        Utils.threadPool.execute {
            do {
                refresh()
                Thread.sleep(1000)
            } while (shouldRun && loopId == currentLoopId)
        }
    }

    fun stop() {
        currentLoopId = null
    }
}

