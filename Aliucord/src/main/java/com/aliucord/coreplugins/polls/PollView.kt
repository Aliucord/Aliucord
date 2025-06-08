package com.aliucord.coreplugins.polls

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.ContextThemeWrapper
import android.widget.TextView
import com.aliucord.Http
import com.aliucord.Logger
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.Button
import com.aliucord.views.DangerButton
import com.aliucord.widgets.LinearLayout
import com.discord.utilities.color.ColorCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.lytefast.flexinput.R
import java.util.Calendar

@SuppressLint("SetTextI18n")
internal class PollView(private val ctx: Context) : MaterialCardView(ctx) {
    private lateinit var title: TextView
    private lateinit var subtext: TextView
    private lateinit var infoText: TextView
    private lateinit var answersContainer: PollAnswersContainerView
    private lateinit var voteButton: MaterialButton
    private lateinit var showResultsButton: MaterialButton
    private lateinit var goBackButton: MaterialButton
    private lateinit var removeVoteButton: MaterialButton

    private var answersKey: String? = null
    private var state: PollViewState = PollViewState.VOTING
        set(value) {
            val previous = field
            field = value
            onStateChange(previous)
        }

    data class PutPollPayload(@Suppress("PropertyName") val answer_ids: List<String>)

    private var voteHandler: ((Boolean) -> Unit)? = null

    internal enum class PollViewState {
        VOTING,
        SHOW_RESULT,
        VOTED,
        CLOSED,
        FINALISED
    }

    init {
        setCardBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorBackgroundSecondary))
        radius = DimenUtils.defaultCardRadius.toFloat()

        val p: Int = DimenUtils.defaultPadding

        LinearLayout(ctx).addTo(this) {
            title = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Label).addTo(this)
            subtext = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).addTo(this) {
                setPadding(p, p / 2, p, 0)
            }
            answersContainer = PollAnswersContainerView(ctx).addTo(this) {
                setPadding(0, p, 0, p / 2)
            }
            LinearLayout(ctx).addTo(this) {
                setPadding(p, 0, p, 0)
                orientation = LinearLayout.HORIZONTAL

                voteButton = Button(ctx).addTo(this) {
                    text = "Vote"
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
                        state = PollViewState.SHOW_RESULT
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
                    state = PollViewState.VOTING
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
    }

    internal fun onStateChange(previousState: PollViewState) {
        if (state != previousState) {
            voteButton.isEnabled = true
            showResultsButton.isEnabled = true
            removeVoteButton.isEnabled = true
        }
        when (state) {
            PollViewState.VOTING -> {
                voteButton.visibility = VISIBLE
                showResultsButton.visibility = VISIBLE
                goBackButton.visibility = GONE
                removeVoteButton.visibility = GONE
            }
            PollViewState.SHOW_RESULT -> {
                voteButton.visibility = GONE
                showResultsButton.visibility = GONE
                goBackButton.visibility = VISIBLE
                removeVoteButton.visibility = GONE
            }
            PollViewState.VOTED -> {
                voteButton.visibility = GONE
                showResultsButton.visibility = GONE
                goBackButton.visibility = GONE
                removeVoteButton.visibility = VISIBLE
            }
            PollViewState.CLOSED,
            PollViewState.FINALISED -> {
                voteButton.visibility = GONE
                showResultsButton.visibility = GONE
                goBackButton.visibility = GONE
                removeVoteButton.visibility = GONE
            }
        }

        answersContainer.updateState(state, previousState == PollViewState.VOTING)
    }

    internal fun configure(entry: PollChatEntry) {
        val data = entry.poll
        val expired = (data.expiry?.g() ?: Long.MAX_VALUE) < Calendar.getInstance().timeInMillis

        val newState = if (data.results?.isFinalized == true)
            PollViewState.FINALISED
        else if (expired)
            PollViewState.CLOSED
        else if (data.results?.answerCounts?.find { it.meVoted && it.count != 0 } != null)
            PollViewState.VOTED
        else if (state == PollViewState.SHOW_RESULT)
            PollViewState.SHOW_RESULT
        else
            PollViewState.VOTING

        if (answersKey != entry.key) {
            answersKey = entry.key
            answersContainer.configure(data)
        }

        state = newState

        data.results?.let { answersContainer.updateCounts(it, state) }

        title.text = data.question.text

        if (data.allowMultiselect)
            subtext.text = "Select one or more answers"
        else
            subtext.text = "Select one answer"

        val totalCount = data.results?.answerCounts?.sumOf { it.count } ?: 0
        val expiryText = if (expired)
            "Poll closed"
        else if (data.expiry != null)
            DateUtils.getRelativeTimeSpanString(data.expiry!!.g())
        else
            null
        val append = expiryText?.let { "  •  $expiryText" } ?: ""
        infoText.text = "$totalCount vote${if (totalCount != 1) "s" else ""}$append"

        // https://canary.discord.com/api/v9/channels/424566511412183052/polls/1381111656385482844/answers/@me
        voteHandler = { isVoting ->
            Thread {
                val req = Http.Request.newDiscordRNRequest(
                    "/channels/${entry.message.channelId}/polls/${entry.message.id}/answers/@me",
                    "PUT"
                ).setRequestTimeout(10000).executeWithJson(PutPollPayload(
                    if (isVoting)
                        this.answersContainer.getCheckedAnswers()
                            .map { it.toString() }
                            .toList()
                    else
                        listOf()
                ))

                if (!req.ok()) {
                    Logger("Polls").errorToast("Failed to submit poll vote", null)
                    voteButton.isEnabled = true
                    showResultsButton.isEnabled = true
                    removeVoteButton.isEnabled = true
                }
            }.start()
        }
    }
}
