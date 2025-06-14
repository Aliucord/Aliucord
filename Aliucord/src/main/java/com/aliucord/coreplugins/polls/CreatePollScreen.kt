@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")
package com.aliucord.coreplugins.polls

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import android.widget.LinearLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aliucord.*
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.GsonUtils
import com.aliucord.utils.GsonUtils.toJson
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.*
import com.aliucord.views.Button
import com.aliucord.widgets.BottomSheet
import com.aliucord.widgets.LinearLayout
import com.discord.api.message.poll.*
import com.discord.api.message.reaction.MessageReactionEmoji
import com.discord.models.domain.emoji.*
import com.discord.stores.StoreStream
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.drawable.DrawableCompat
import com.discord.utilities.textprocessing.node.EmojiNode
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.views.CheckedSetting
import com.discord.widgets.chat.input.emoji.EmojiPickerContextType
import com.discord.widgets.chat.input.emoji.WidgetEmojiPickerSheet
import com.lytefast.flexinput.R
import java.util.concurrent.ThreadLocalRandom

internal class CreatePollScreen : SettingsPage() {
    companion object {
        val p = DimenUtils.defaultPadding

        fun <T: View> T.margin(bottom: Boolean = true, top: Boolean = false, hori: Boolean = true): T {
            val params = if (layoutParams == null)
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            else
                LayoutParams(layoutParams)
            layoutParams = params.apply {
                if (top)
                    topMargin = p
                if (bottom)
                    bottomMargin = p
                if (hori) {
                    leftMargin = p
                    rightMargin = p
                }
            }
            return this
        }

        fun Emoji.asReactionEmoji() = when (this) {
            is ModelEmojiCustom -> MessageReactionEmoji(idStr, name, isAnimated)
            is ModelEmojiUnicode -> MessageReactionEmoji(null, surrogates, false)
            else -> throw IllegalStateException("Unknown emoji type ${this::class.java.name}")
        }

        private fun createTextInput(ctx: Context, placeholder: String, onAfterTextChanged: (Editable) -> Unit) =
            TextInput(ctx, placeholder).apply {
                editText.maxLines = 1
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
                    override fun afterTextChanged(s: Editable) {
                        onAfterTextChanged(s)
                    }
                })
            }
    }

    @Suppress("unused", "PrivatePropertyName")
    internal class Payload(private val poll: MessagePoll) {
        private val mobile_network_type = "wifi"
        private val signal_strength = ThreadLocalRandom.current().nextInt(1, 5) // TODO: Use real values maybe?
        private val content = ""
        // For nonce, there is NonceGenerator, but it seems to use time in the future. RN and Desktop doesn't do this,
        // so it also wasn't used here. Instead we just generate a random long and add it with current time
        private val nonce = SnowflakeUtils.fromTimestamp(System.currentTimeMillis()) + ThreadLocalRandom.current().nextLong((1 shl 23) - 1)
        private val tts = false
        private val flags = 0
    }

    internal enum class Duration(val text: String, val value: Int) {
        ONE_HOUR("1 hour", 1),
        FOUR_HOURS("4 hours", 4),
        EIGHT_HOURS("8 hours", 8),
        ONE_DAY("24 hours", 24),
        THREE_DAYS("3 days", 72),
        ONE_WEEK("1 week", 168),
        TWO_WEEKS("2 weeks", 336),
    }

    internal class DurationSelectorSheet(
        private val current: Duration,
        private val onSelected: (Duration) -> Unit
    ) : BottomSheet() {
        @SuppressLint("SetTextI18n")
        override fun onViewCreated(view: View, bundle: Bundle?) {
            super.onViewCreated(view, bundle)

            val ctx = requireContext()
            linearLayout.apply {
                ConstraintLayout(ctx, null, 0, R.i.UiKit_Sheet_Header).addTo(this) {
                    Guideline(ctx, null, 0, R.i.UiKit_Sheet_Guideline).addTo(this)
                    TextView(ctx, null, 0, R.i.UiKit_Sheet_Header_Title).addTo(this) {
                        setPadding(p, 0, 0, 0)
                        text = "Duration"
                    }
                }
                Divider(ctx).addTo(this)
                ScrollView(ctx, null, 0, R.i.UiKit_ViewGroup_ScrollView).addTo(this) {
                    LinearLayout(ctx).addTo(this) {
                        for (duration in Duration.values()) {
                            Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.RADIO, duration.text, null).addTo(this) {
                                if (current == duration)
                                    isChecked = true
                                setOnCheckedListener {
                                    onSelected(duration)
                                    dismiss()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    internal class AnswerInput(private val ctx: Context): ConstraintLayout(ctx) {
        companion object {
            val emojiButtonId = generateViewId()
            val textInputId = generateViewId()
            val deleteButtonId = generateViewId()
        }

        var onDelete: (() -> Unit)? = null
        var onOpenEmoji: (() -> Unit)? = null
        var onTextChange: ((Editable) -> Unit)? = null

        fun setText(text: CharSequence) {
            textInput.editText.setText(text)
        }

        fun setEmoji(emoji: Emoji?) {
            emojiButton.removeAllViews()
            if (emoji == null) {
                ImageView(ctx).addTo(emojiButton) {
                    setImageResource(R.e.ic_emoji_24dp)
                    val color = ColorCompat.getThemedColor(ctx, R.b.flexInputIconColor)
                    drawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                }
            } else {
                SimpleDraweeSpanTextView(ctx).addTo(emojiButton) {
                    layoutParams = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    EmojiNode.Companion!!.renderEmoji(this, emoji.asReactionEmoji(), true, DimenUtils.dpToPx(24))
                }
            }
        }

        fun setRemoveVisibility(visible: Boolean) {
            if (visible) {
                removeButton.visibility = VISIBLE
                textInput.layoutParams = (textInput.layoutParams as ConstraintLayout.LayoutParams).apply {
                    marginEnd = 0
                }
            } else {
                removeButton.visibility = GONE
                textInput.layoutParams = (textInput.layoutParams as ConstraintLayout.LayoutParams).apply {
                    marginEnd = p
                }
            }
        }

        private val emojiButton = FrameLayout(ctx).addTo(this) {
            ImageView(ctx).addTo(this) {
                setImageResource(R.e.ic_emoji_24dp)
                val color = ColorCompat.getThemedColor(ctx, R.b.flexInputIconColor)
                drawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            }

            id = emojiButtonId
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                topToTop = LayoutParams.PARENT_ID
                bottomToBottom = LayoutParams.PARENT_ID
                startToStart = LayoutParams.PARENT_ID
                endToStart = textInputId
                setPadding(p, 0, p, 0)
            }
            setOnClickListener {
                onOpenEmoji?.invoke()
            }
        }

        private val textInput =
            createTextInput(ctx, "Type your answer") { onTextChange?.invoke(it) }
                .addTo(this) {
                    id = textInputId
                    layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT).apply {
                        topToTop = LayoutParams.PARENT_ID
                        bottomToBottom = LayoutParams.PARENT_ID
                        startToEnd = emojiButtonId
                        endToStart = deleteButtonId
                    }
                }

        private val removeButton = ImageView(ctx).addTo(this) {
            id = deleteButtonId

            setImageResource(R.e.ic_delete_24dp)
            val color = ColorCompat.getThemedColor(ctx, R.b.flexInputIconColor)
            drawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, 0).apply {
                topToTop = LayoutParams.PARENT_ID
                bottomToBottom = LayoutParams.PARENT_ID
                endToEnd = LayoutParams.PARENT_ID
                startToEnd = textInputId
                setPadding(p, 0, p, 0)
            }
            setOnClickListener { onDelete?.invoke() }
        }
    }

    internal enum class RequestState {
        IDLE,
        REQUESTING,
        SUCCESS,
    }

    internal data class AnswerState(
        val answer: String = "",
        val emoji: Emoji? = null,
    )

    internal data class State(
        val question: String = "",
        val answers: List<AnswerState> = listOf(AnswerState(), AnswerState()),
        val duration: Duration = Duration.ONE_DAY,
        val isMultiselect: Boolean = false,

        val requestState: RequestState = RequestState.IDLE,
    )

    internal class CreatePollViewModel() : ViewModel() {
        var onStateUpdate: ((newState: State, previousState: State) -> Unit)? = null
            set(value) {
                field = value
                value?.invoke(state, state)
            }

        var state = State()
            private set(value) {
                if (field == value)
                    return
                val previousState = field
                field = value
                onStateUpdate?.invoke(value, previousState)
            }

        fun updateQuestionText(text: Editable) {
            state = state.copy(question = text.toString())
        }

        fun createAnswer() {
            if (state.answers.size >= 10)
                return Logger("Polls/CreatePollScreen").warn("newAnswer() called, but there's already max answers? Ignoring..")
            state = state.copy(answers = state.answers + listOf(AnswerState()))
        }

        fun deleteAnswer(index: Int) {
            state = state.copy(answers = state.answers.filterIndexed { idx, _ -> idx != index })
        }

        fun updateAnswerText(index: Int, text: Editable) {
            state = state.copy(answers = state.answers.mapIndexed { idx, answer ->
                if (idx == index)
                    answer.copy(answer = text.toString())
                else
                    answer
            })
        }

        fun showEmojiSelector(index: Int, fragmentManager: FragmentManager) {
            val guild = StoreStream.getGuildSelected()
            val emojiCtx = if (guild != null)
                EmojiPickerContextType.Guild(guild.selectedGuildId)
            else
                EmojiPickerContextType.Global.INSTANCE
            WidgetEmojiPickerSheet.Companion!!.show(fragmentManager, {
                state = state.copy(answers = state.answers.mapIndexed { idx, answer ->
                    if (idx == index)
                        answer.copy(emoji = it)
                    else
                        answer
                })
            }, emojiCtx, {})
        }

        fun updateIsMultiselect(isMultiselect: Boolean) {
            state = state.copy(isMultiselect = isMultiselect)
        }

        fun showDurationSelector(fragmentManager: FragmentManager) {
            DurationSelectorSheet(state.duration) {
                state = state.copy(duration = it)
            }.show(fragmentManager, DurationSelectorSheet::class.java.name)
        }

        fun buildPayload(): Payload = Payload(MessagePoll(
            question = MessagePollMedia(state.question.toString(), null),
            answers = state.answers.map {
                MessagePollAnswer(null, MessagePollMedia(it.answer.toString(), it.emoji?.asReactionEmoji()))
            },
            results = null,
            duration = state.duration.value,
            expiry = null,
            allowMultiselect = state.isMultiselect,
            layoutType = 1
        ))

        fun sendRequest(channelId: Long) {
            state = state.copy(requestState = RequestState.REQUESTING)
            Utils.threadPool.execute {
                val req = Http.Request.newDiscordRNRequest(
                    "/channels/${channelId}/messages",
                    "POST"
                ).setRequestTimeout(10000).executeWithJson(buildPayload())
                if (!req.ok()) {
                    Logger("Polls/Create").errorToast("Failed to create poll")
                    Logger("Polls/Create").error("${req.statusCode} ${req.statusMessage} ${req.text()}", null)
                    state = state.copy(requestState = RequestState.IDLE)
                }
                state = state.copy(requestState = RequestState.SUCCESS)
            }
        }
    }

    private fun addHeader(text: String) =
        TextView(requireContext(), null, 0, R.i.UiKit_Stage_SectionHeader).margin(top = true).addTo(linearLayout) {
            this.text = text
        }

    @SuppressLint("SetTextI18n")
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        if (view !is CoordinatorLayout)
            return

        val ctx = requireContext()
        val viewModel = ViewModelProvider(this).get(CreatePollViewModel::class.java)

        val chName = mostRecentIntent.getStringExtra("INTENT_CHANNEL_NAME")
        val channelId = mostRecentIntent.getLongExtra("INTENT_CHANNEL_ID", -1L)
        if (channelId == -1L)
            return this.appActivity.finish()

        setActionBarTitle("Create poll")
        setActionBarSubtitle(chName)

        linearLayout.run layout@{
            setPadding(0, 0, 0, 0)

            addHeader("Question")
            createTextInput(ctx, "Type your question") { viewModel.updateQuestionText(it) }
                .margin()
                .addTo(linearLayout) {
                    editText.setText(viewModel.state.question)
                }

            Divider(ctx).addTo(this)

            addHeader("Answers")
            val answerInputs = List(10) { index ->
                AnswerInput(ctx).margin(hori = false).addTo(this) {
                    viewModel.state.answers.getOrNull(index)?.let { state ->
                        setText(state.answer)
                    }
                    onDelete = { viewModel.deleteAnswer(index) }
                    onOpenEmoji = { viewModel.showEmojiSelector(index, childFragmentManager) }
                    onTextChange = { viewModel.updateAnswerText(index, it) }
                }
            }
            val addAnswerButton = Button(ctx).margin().addTo(this) {
                text = "Add answer"
                setOnClickListener { viewModel.createAnswer() }
            }

            Divider(ctx).addTo(this)

            addHeader("Options")
            val durationSelector = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.CHECK, "Duration", null).addTo(this) {
                l.c().visibility = View.GONE
                ImageView(ctx).addTo(l.b() as ConstraintLayout) {
                    val res = DrawableCompat.getThemedDrawableRes(ctx, R.b.ic_navigate_next)
                    setImageResource(res)
                    setPadding(0, 0, p / 4, 0)
                    layoutParams = ConstraintLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                        topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                        endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    }
                }
                l.b().setOnClickListener {
                    viewModel.showDurationSelector(childFragmentManager)
                }
            }

            val multiselectSwitch = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "Allow multiple answers", null).addTo(this) {
                setOnCheckedListener {
                    viewModel.updateIsMultiselect(it)
                }
            }

            val debugTextView = TextView(ctx).addTo(this)

            val createButton = Button(context).margin(top = true).addTo(this) {
                text = "Create Poll"
                setOnClickListener {
                    viewModel.sendRequest(channelId)
                }
            }

            viewModel.onStateUpdate = { state, previous ->
                when (state.requestState) {
                    RequestState.IDLE -> {
                        createButton.isEnabled =
                            state.question != "" && // Question is not empty
                            state.answers.find { it.answer == "" } == null // No empty answers
                    }
                    RequestState.REQUESTING -> createButton.isEnabled = false
                    RequestState.SUCCESS -> this@CreatePollScreen.appActivity.finish()
                }

                durationSelector.setSubtext(state.duration.text)
                multiselectSwitch.isChecked = state.isMultiselect
                addAnswerButton.visibility = if (state.answers.size >= 10)
                    View.GONE
                else
                    View.VISIBLE

                answerInputs.forEachIndexed { idx, input ->
                    val answerState = state.answers.getOrNull(idx)
                    if (answerState == null)
                        input.visibility = View.GONE
                    else {
                        input.visibility = View.VISIBLE
                        input.setEmoji(answerState.emoji)
                        input.setRemoveVisibility(state.answers.size != 1)

                        val currentSize = state.answers.size
                        val previousSize = previous.answers.size
                        if (currentSize != previousSize) {
                            if (currentSize > previousSize && idx == currentSize - 1)
                                input.requestFocus() // Request focus for newly added answer

                            input.setText(answerState.answer) // Update text on answer length change
                        }
                    }
                }
                debugTextView.text = GsonUtils.gson.toJson(viewModel.buildPayload())
            }
        }
    }
}
