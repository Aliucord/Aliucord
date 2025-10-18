package com.aliucord.coreplugins.polls.creation

import android.content.Context
import android.content.Intent
import android.text.*
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import com.aliucord.Utils
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.utils.ViewUtils.checkbox
import com.aliucord.utils.ViewUtils.layout
import com.aliucord.utils.ViewUtils.setDefaultMargins
import com.aliucord.views.*
import com.discord.api.message.reaction.MessageReactionEmoji
import com.discord.models.domain.emoji.*
import com.discord.utilities.drawable.DrawableCompat
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R

internal class PollCreateScreen : SettingsPage() {
    companion object {
        fun launch(ctx: Context, channelName: String, channelId: Long) {
            val intent = Intent()
                .putExtra("com.discord.intent.extra.EXTRA_CHANNEL_NAME", channelName)
                .putExtra("com.discord.intent.extra.EXTRA_CHANNEL_ID", channelId)
            Utils.openPage(ctx, PollCreateScreen::class.java, intent)
        }

        fun Emoji.asReactionEmoji() = when (this) {
            is ModelEmojiCustom -> MessageReactionEmoji(idStr, name, isAnimated)
            is ModelEmojiUnicode -> MessageReactionEmoji(null, surrogates, false)
            else -> throw IllegalStateException("Unknown emoji type ${this::class.java.name}")
        }

        fun createTextInput(ctx: Context, maxLength: Int, placeholder: String, onAfterTextChanged: (Editable) -> Unit) =
            TextInput(ctx, placeholder).apply {
                editText.maxLines = 1
                editText.filters = arrayOf(InputFilter.LengthFilter(maxLength))
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
                    override fun afterTextChanged(s: Editable) {
                        onAfterTextChanged(s)
                    }
                })
            }
    }

    enum class RequestState {
        IDLE,
        REQUESTING,
        SUCCESS,
    }

    data class AnswerModel(
        val answer: String = "",
        val emoji: Emoji? = null,
    )

    data class Model(
        val question: String = "",
        val answers: List<AnswerModel> = listOf(AnswerModel(), AnswerModel()),
        val duration: Duration = Duration.ONE_DAY,
        val isMultiselect: Boolean = false,
        val requestState: RequestState = RequestState.IDLE,
    )

    private fun addHeader(text: String) =
        TextView(requireContext(), null, 0, R.i.UiKit_Stage_SectionHeader).setDefaultMargins(top = true).addTo(linearLayout) {
            this.text = text
        }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        if (view !is CoordinatorLayout) return

        val ctx = requireContext()
        val viewModel = ViewModelProvider(this).get(PollCreateViewModel::class.java)

        val channelName = mostRecentIntent.getStringExtra("com.discord.intent.extra.EXTRA_CHANNEL_NAME")
        val channelId = mostRecentIntent.getLongExtra("com.discord.intent.extra.EXTRA_CHANNEL_ID", -1L)
        if (channelId == -1L) {
            this.appActivity.finish()
            return
        }

        setActionBarTitle("Create poll")
        setActionBarSubtitle(channelName)

        linearLayout.run layout@{
            setPadding(0, 0, 0, 0)

            addHeader("Question")
            createTextInput(ctx, 300, "Type your question") { viewModel.updateQuestionText(it) }
                .setDefaultMargins()
                .addTo(linearLayout) {
                    editText.setText(viewModel.model.question)
                }

            Divider(ctx).addTo(this)

            addHeader("Answers")
            val answerInputs = List(10) { index ->
                AnswerInput(ctx).setDefaultMargins(left = false, right = false).addTo(this) {
                    viewModel.model.answers.getOrNull(index)?.let { model ->
                        setText(model.answer)
                    }
                    onDelete = { viewModel.deleteAnswer(index) }
                    onOpenEmoji = { viewModel.showEmojiSelector(index, childFragmentManager) }
                    onTextChange = { viewModel.updateAnswerText(index, it) }
                }
            }
            val addAnswerButton = Button(ctx).setDefaultMargins().addTo(this) {
                text = "Add answer"
                setOnClickListener { viewModel.createAnswer() }
            }

            Divider(ctx).addTo(this)

            addHeader("Options")
            val durationSelector = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.CHECK, "Duration", null).addTo(this) {
                checkbox.visibility = View.GONE
                ImageView(ctx).addTo(layout) {
                    val res = DrawableCompat.getThemedDrawableRes(ctx, R.b.ic_navigate_next)
                    setImageResource(res)
                    setPadding(0, 0, 2.dp, 0)
                    layoutParams = ConstraintLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                        topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                        endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    }
                }
                layout.setOnClickListener {
                    viewModel.showDurationSelector(childFragmentManager)
                }
            }

            val multiselectSwitch = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "Allow multiple answers", null).addTo(this) {
                setOnCheckedListener {
                    viewModel.updateIsMultiselect(it)
                }
            }

            val createButton = Button(ctx).setDefaultMargins(top = true).addTo(this) {
                text = "Create Poll"
                setOnClickListener {
                    viewModel.sendRequest(channelId)
                }
            }

            viewModel.onModelUpdate = { model, previous ->
                when (model.requestState) {
                    RequestState.IDLE -> {
                        createButton.isEnabled =
                            model.question != "" && // Question is not empty
                            model.answers.find { it.answer == "" } == null // No empty answers
                    }
                    RequestState.REQUESTING -> createButton.isEnabled = false
                    RequestState.SUCCESS -> this@PollCreateScreen.appActivity.finish()
                }

                durationSelector.setSubtext(model.duration.text)
                multiselectSwitch.isChecked = model.isMultiselect
                addAnswerButton.visibility = if (model.answers.size >= 10) {
                    View.GONE
                }
                else {
                    View.VISIBLE
                }

                answerInputs.forEachIndexed { idx, input ->
                    val answerModel = model.answers.getOrNull(idx)
                    if (answerModel == null) {
                        input.visibility = View.GONE
                    } else {
                        input.visibility = View.VISIBLE
                        input.setEmoji(answerModel.emoji)
                        input.setRemoveVisibility(model.answers.size != 1)

                        val currentSize = model.answers.size
                        val previousSize = previous.answers.size
                        if (currentSize != previousSize) {
                            if (currentSize > previousSize && idx == currentSize - 1)
                                input.requestFocus() // Request focus for newly added answer

                            input.setText(answerModel.answer) // Update text on answer length change
                        }
                    }
                }
            }
        }
    }
}
