package com.aliucord.coreplugins.polls.chatview

import android.annotation.SuppressLint
import android.content.Context
import android.view.ContextThemeWrapper
import android.widget.TextView
import com.aliucord.*
import com.aliucord.coreplugins.polls.PollsStore
import com.aliucord.coreplugins.polls.details.PollDetailsScreen
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.Button
import com.aliucord.views.DangerButton
import com.aliucord.widgets.LinearLayout
import com.discord.utilities.color.ColorCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.lytefast.flexinput.R
import rx.Subscription
import java.util.Calendar

@SuppressLint("SetTextI18n")
internal class PollChatView(private val ctx: Context) : MaterialCardView(ctx) {
    internal enum class State {
        VOTING,
        SHOW_RESULT,
        VOTED,
        CLOSED,
        FINALISED;

        fun visibleIf(vararg types: State) = if (this in types) VISIBLE else GONE
    }

    private data class PollVotePayload(@Suppress("PropertyName") val answer_ids: List<Int>)

    private val logger = Logger("Polls")

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
    private val infoTextAdapter: PollChatInfoTextAdapter
    private var voteHandler: ((Boolean) -> Unit)? = null

    private class SubscriptionHandler {
        private var storeSubscription: Subscription? = null

        private var targetChannel: Long? = null
        private var targetMessage: Long? = null
        private var onNext: ((HashMap<Int, PollsStore.VoterSnapshot>?) -> Unit)? = null

        fun unsubscribe() {
            storeSubscription?.unsubscribe()
        }

        fun subscribe() {
            unsubscribe()
            if (targetChannel == null || targetMessage == null || onNext == null)
                return
            PollsStore.subscribeOnMain(targetChannel!!, targetMessage!!, onNext!!)
        }

        fun configure(channelId: Long, messageId: Long, onNext: (HashMap<Int, PollsStore.VoterSnapshot>?) -> Unit) {
            targetChannel = channelId
            targetMessage = messageId
            this.onNext = onNext
        }
    }

    private val subscriptionHandler = SubscriptionHandler()

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

        infoTextAdapter = PollChatInfoTextAdapter(this.infoText)
    }

    internal fun updateState(previousState: State?) {
        if (state != previousState) {
            voteButton.isEnabled = answersContainer.hasChecked
            showResultsButton.isEnabled = true
            removeVoteButton.isEnabled = true
        }
        subtext.visibility = state.visibleIf(State.VOTING, State.SHOW_RESULT, State.VOTED)
        voteButton.visibility = state.visibleIf(State.VOTING)
        showResultsButton.visibility = state.visibleIf(State.VOTING)
        goBackButton.visibility = state.visibleIf(State.SHOW_RESULT)
        removeVoteButton.visibility = state.visibleIf(State.VOTED)

        answersContainer.updateState(state, previousState == State.VOTING)
    }

    internal fun configure(entry: PollChatEntry) {
        val data = entry.poll
        val expired = (data.expiry?.g() ?: Long.MAX_VALUE) < Calendar.getInstance().timeInMillis

        if (answersKey != entry.key) {
            answersKey = entry.key
            answersContainer.configure(data)
        }

        state = if (data.results?.isFinalized == true)
            State.FINALISED
        else if (expired)
            State.CLOSED
        else if (data.results?.answerCounts?.find { it.meVoted && it.count != 0 } != null)
            State.VOTED
        else if (state == State.SHOW_RESULT)
            State.SHOW_RESULT
        else
            State.VOTING

        title.text = data.question.text

        subtext.text = if (data.allowMultiselect)
            "Select one or more answers"
        else
            "Select one answer"

        infoText.setOnClickListener {
            PollDetailsScreen.create(ctx, entry.message.channelId, entry.message.id)
        }

        voteHandler = { isVoting ->
            Utils.threadPool.execute {
                val request = runCatching {
                    Http.Request.newDiscordRNRequest(
                        "/channels/${entry.message.channelId}/polls/${entry.message.id}/answers/@me",
                        "PUT"
                    ).setRequestTimeout(10000).executeWithJson(PollVotePayload(
                        if (isVoting)
                            this.answersContainer.getCheckedAnswers().toList()
                        else
                            listOf()
                    ))
                }
                val result = request.getOrNull()
                if (result?.ok() != true) {
                    logger.errorToast("Failed to submit poll vote")
                    if (result != null)
                        logger.error("${result.statusCode} ${result.statusMessage} ${result.text()}", null)
                    else
                        logger.error(request.exceptionOrNull())
                    Utils.mainThread.post { updateState(null) }
                }
            }
        }

        subscriptionHandler.configure(entry.message.channelId, entry.message.id) {
            if (it == null) {
                subscriptionHandler.unsubscribe()
                return@configure
            }

            if (state in listOf(State.CLOSED, State.FINALISED))
                subscriptionHandler.unsubscribe()
            else {
                state = if (it.values.any { it.meVoted })
                    State.VOTED
                else if (state == State.SHOW_RESULT)
                    State.SHOW_RESULT
                else
                    State.VOTING
            }
            answersContainer.updateCounts(it, state)
            infoTextAdapter.updateData(state, it.values.sumOf { it.count }, data.expiry!!)
        }

        subscriptionHandler.subscribe()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == GONE) {
            infoTextAdapter.stop()
            subscriptionHandler.unsubscribe()
        }
        else {
            infoTextAdapter.start()
            subscriptionHandler.subscribe()
        }
    }
}
