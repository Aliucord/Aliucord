package com.aliucord.coreplugins.polls.chatview

import android.content.Context
import android.view.ContextThemeWrapper
import android.widget.TextView
import com.aliucord.coreplugins.polls.details.PollDetailsScreen
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.*
import com.aliucord.widgets.LinearLayout
import com.discord.api.message.reaction.MessageReactionEmoji
import com.discord.utilities.color.ColorCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.lytefast.flexinput.R
import java.util.Calendar

internal class PollChatView(private val ctx: Context) : MaterialCardView(ctx) {
    internal enum class State {
        VOTING,
        PEEKING_RESULTS,
        VOTED,
        CLOSED;
    }

    data class AnswerModel(
        val id: Int,
        val text: String,
        val emoji: MessageReactionEmoji?,
        val meVoted: Boolean,
        val votes: Int,
        val checked: Boolean,
    )

    data class Model(
        val channelId: Long,
        val messageId: Long,
        val question: String,
        val multiselect: Boolean,
        val expiry: Long?,
        val finalised: Boolean,
        val peekingResults: Boolean,
        val submittingVote: Boolean,
        val answers: List<AnswerModel>,
    ) {
        val state = when {
            (expiry ?: Long.MAX_VALUE) < Calendar.getInstance().timeInMillis -> {
                State.CLOSED
            }
            answers.any { it.meVoted && it.votes != 0 } -> {
                State.VOTED
            }
            peekingResults -> {
                State.PEEKING_RESULTS
            }
            else -> {
                State.VOTING
            }
        }

        val totalVotes get() = answers.sumOf { it.votes }
    }

    private lateinit var title: TextView
    private lateinit var subtext: TextView
    private lateinit var infoText: TextView
    private lateinit var answersContainer: LinearLayout
    private lateinit var answerViews: List<PollChatAnswerView>
    private lateinit var voteButton: MaterialButton
    private lateinit var showResultsButton: MaterialButton
    private lateinit var goBackButton: MaterialButton
    private lateinit var removeVoteButton: MaterialButton

    private val infoTextAdapter: PollChatInfoTextAdapter
    private var viewModel: PollChatViewModel? = null

    init {
        setCardBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorBackgroundSecondary))
        radius = DimenUtils.defaultCardRadius.toFloat()

        val p: Int = DimenUtils.defaultPadding

        LinearLayout(ctx).addTo(this) {
            title = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Label).addTo(this)
            subtext = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).addTo(this) {
                setPadding(p, p / 2, p, 0)
            }
            answersContainer = LinearLayout(ctx).addTo(this) {
                setPadding(0, p, 0, p / 2)
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
                        viewModel?.addVotes()
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
                        viewModel?.peekResults()
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
                    viewModel?.unpeekResults()
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
                    viewModel?.removeVotes()
                }
            }
            infoText = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).addTo(this) {
                setPadding(p, p / 2, p, p)
            }
        }

        infoTextAdapter = PollChatInfoTextAdapter(this.infoText)
    }

    @OptIn(ExperimentalStdlibApi::class)
    internal fun configure(entry: PollChatEntry) {
        viewModel = PollChatViewModelFactory.create(entry, ::configureUI)
        val model = viewModel!!.model

        answersContainer.removeAllViews()
        Divider(ctx).addTo(answersContainer)

        answerViews = buildList {
            for (answer in model.answers) {
                val view = PollChatAnswerView.build(
                    ctx,
                    model.channelId,
                    model.messageId,
                    answer.id,
                    model.multiselect,
                    onClickListener = {
                        viewModel!!.toggleVote(answer.id)
                    },
                )

                add(view)

                view.addTo(answersContainer)
                Divider(ctx).addTo(answersContainer)
            }
        }

        configureUI(model, false)
    }

    private fun configureUI(model: Model, isUpdate: Boolean) {
        val state = model.state

        title.text = model.question
        subtext.text = if (model.multiselect) {
            "Select one or more answers"
        } else {
            "Select one answer"
        }
        infoText.setOnClickListener {
            PollDetailsScreen.launch(ctx, model.channelId, model.messageId)
        }

        voteButton.isEnabled = !model.submittingVote && model.answers.any { it.checked }
        showResultsButton.isEnabled = !model.submittingVote
        removeVoteButton.isEnabled = !model.submittingVote

        subtext.visibility = if (state != State.CLOSED) VISIBLE else GONE
        voteButton.visibility = if (state == State.VOTING) VISIBLE else GONE
        showResultsButton.visibility = if (state == State.VOTING) VISIBLE else GONE
        goBackButton.visibility = if (state == State.PEEKING_RESULTS) VISIBLE else GONE
        removeVoteButton.visibility = if (state == State.VOTED) VISIBLE else GONE

        infoTextAdapter.configure(model)
        answerViews.forEach { it.configureUI(model, !isUpdate && !model.submittingVote) }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == GONE) {
            infoTextAdapter.stop()
            viewModel?.unsubscribe()
        } else {
            infoTextAdapter.start()
            viewModel?.subscribe()
        }
    }
}
