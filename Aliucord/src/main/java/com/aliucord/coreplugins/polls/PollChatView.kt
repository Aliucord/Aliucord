package com.aliucord.coreplugins.polls

import android.annotation.SuppressLint
import android.content.Context
import android.view.ContextThemeWrapper
import android.widget.TextView
import com.aliucord.*
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.Button
import com.aliucord.views.DangerButton
import com.aliucord.widgets.LinearLayout
import com.discord.api.utcdatetime.UtcDateTime
import com.discord.utilities.color.ColorCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.lytefast.flexinput.R
import java.util.Calendar
import kotlin.random.Random

@SuppressLint("SetTextI18n")
internal class PollChatView(private val ctx: Context) : MaterialCardView(ctx) {
    private class InfoTextAdapter(private val ctx: Context, private val infoText: TextView) {
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

    private data class PutPollPayload(@Suppress("PropertyName") val answer_ids: List<Int>)

    internal enum class State {
        VOTING,
        SHOW_RESULT,
        VOTED,
        CLOSED,
        FINALISED
    }

    private lateinit var title: TextView
    private lateinit var subtext: TextView
    private lateinit var infoText: TextView
    private lateinit var answersContainer: PollChatAnswersContainerView
    private lateinit var voteButton: MaterialButton
    private lateinit var showResultsButton: MaterialButton
    private lateinit var goBackButton: MaterialButton
    private lateinit var removeVoteButton: MaterialButton

    private var answersKey: String? = null
    private var state: State = State.VOTING
        set(value) {
            val previous = field
            field = value
            updateState(previous)
        }
    private val infoTextAdapter: InfoTextAdapter
    private var voteHandler: ((Boolean) -> Unit)? = null

    init {
        setCardBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorBackgroundSecondary))
        radius = DimenUtils.defaultCardRadius.toFloat()

        val p: Int = DimenUtils.defaultPadding

        LinearLayout(ctx).addTo(this) {
            title = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Label).addTo(this)
            subtext = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).addTo(this) {
                setPadding(p, p / 2, p, 0)
            }
            answersContainer = PollChatAnswersContainerView(ctx).addTo(this) {
                setPadding(0, p, 0, p / 2)
                onHasCheckedChange = {
                    voteButton.isEnabled = it
                }
            }
            LinearLayout(ctx).addTo(this) {
                setPadding(p, 0, p, 0)
                orientation = LinearLayout.HORIZONTAL

                voteButton = Button(ctx).addTo(this) {
                    text = "Vote"
                    isEnabled = false
                    setOnClickListener {
                        isEnabled = false
                        showResultsButton.isEnabled = false
                        voteHandler?.invoke(true)
                    }
                }
                showResultsButton = MaterialButton(
                    ContextThemeWrapper(context, R.i.UiKit_Material_Button_Secondary), null, 0
                ).addTo(this) {
                    text = "Show results"
                    layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                        marginStart = p / 2
                    }
                    setOnClickListener {
                        state = State.SHOW_RESULT
                    }
                }
            }
            goBackButton = MaterialButton(
                ContextThemeWrapper(context, R.i.UiKit_Material_Button_Secondary), null, 0
            ).addTo(this) {
                visibility = GONE
                text = "Go back to vote"
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    marginStart = p
                }
                setOnClickListener {
                    state = State.VOTING
                }
            }
            removeVoteButton = DangerButton(ctx).addTo(this) {
                visibility = GONE
                text = "Remove vote"
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    marginStart = p
                }
                setOnClickListener {
                    isEnabled = false
                    voteHandler?.invoke(false)
                }
            }
            infoText = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).addTo(this) {
                setPadding(p, p / 2, p, p)
            }
        }

        infoTextAdapter = InfoTextAdapter(ctx, this.infoText)
    }

    internal fun updateState(previousState: State) {
        if (state != previousState) {
            voteButton.isEnabled = answersContainer.hasChecked
            showResultsButton.isEnabled = true
            removeVoteButton.isEnabled = true
        }
        when (state) {
            State.VOTING -> {
                voteButton.visibility = VISIBLE
                showResultsButton.visibility = VISIBLE
                goBackButton.visibility = GONE
                removeVoteButton.visibility = GONE
                subtext.visibility = VISIBLE
            }
            State.SHOW_RESULT -> {
                voteButton.visibility = GONE
                showResultsButton.visibility = GONE
                goBackButton.visibility = VISIBLE
                removeVoteButton.visibility = GONE
                subtext.visibility = VISIBLE
            }
            State.VOTED -> {
                voteButton.visibility = GONE
                showResultsButton.visibility = GONE
                goBackButton.visibility = GONE
                removeVoteButton.visibility = VISIBLE
                subtext.visibility = VISIBLE
            }
            State.CLOSED,
            State.FINALISED -> {
                voteButton.visibility = GONE
                showResultsButton.visibility = GONE
                goBackButton.visibility = GONE
                removeVoteButton.visibility = GONE
                subtext.visibility = GONE
            }
        }

        answersContainer.updateState(state, previousState == State.VOTING)
    }

    internal fun configure(entry: PollChatEntry) {
        val data = entry.poll
        val expired = (data.expiry?.g() ?: Long.MAX_VALUE) < Calendar.getInstance().timeInMillis

        val newState = if (data.results?.isFinalized == true)
            State.FINALISED
        else if (expired)
            State.CLOSED
        else if (data.results?.answerCounts?.find { it.meVoted && it.count != 0 } != null)
            State.VOTED
        else if (state == State.SHOW_RESULT)
            State.SHOW_RESULT
        else
            State.VOTING

        if (answersKey != entry.key) {
            answersKey = entry.key
            answersContainer.configure(data)
        }

        state = newState

        data.results?.let { answersContainer.updateCounts(it, state) }

        title.text = data.question.text

        subtext.text = if (data.allowMultiselect)
            "Select one or more answers"
        else
            "Select one answer"

        val totalCount = data.results?.answerCounts?.sumOf { it.count } ?: 0
        infoTextAdapter.updateData(state, totalCount, data.expiry!!)

        voteHandler = { isVoting ->
            Utils.threadPool.execute {
                val req = Http.Request.newDiscordRNRequest(
                    "/channels/${entry.message.channelId}/polls/${entry.message.id}/answers/@me",
                    "PUT"
                ).setRequestTimeout(10000).executeWithJson(PutPollPayload(
                    if (isVoting)
                        this.answersContainer.getCheckedAnswers().toList()
                    else
                        listOf()
                ))

                if (!req.ok()) {
                    Logger("Polls").errorToast("Failed to submit poll vote")
                    Logger("Polls").error("${req.statusCode} ${req.statusMessage} ${req.text()}", null)
                    voteButton.isEnabled = true
                    showResultsButton.isEnabled = true
                    removeVoteButton.isEnabled = true
                }
            }
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == GONE)
            infoTextAdapter.stop()
        else
            infoTextAdapter.start()
    }
}
