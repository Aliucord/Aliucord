package com.aliucord.coreplugins

import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.view.MotionEvent
import android.view.View
import com.aliucord.Utils
import com.aliucord.coreplugins.decorations.JumpToMessageSettings
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.before
import com.aliucord.patcher.instead
import com.aliucord.utils.RxUtils
import com.aliucord.utils.RxUtils.subscribe
import com.discord.databinding.WidgetChatListBinding
import com.discord.stores.StoreChat
import com.discord.stores.StoreStream
import com.discord.utilities.rx.ObservableExtensionsKt
import com.discord.widgets.chat.list.WidgetChatList
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.model.WidgetChatListModel
import rx.Observable
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicReference


internal class JumpToMessageFix : CorePlugin(Manifest("JumpToMessageFix")) {
    init {
        manifest.description = "Fixes message links not jumping to correct message."
        settingsTab = SettingsTab(JumpToMessageSettings.Sheet::class.java, SettingsTab.Type.BOTTOM_SHEET)
    }

    val bindingGetter: Method = WidgetChatList::class.java.getDeclaredMethod("getBinding").apply { isAccessible = true }
    val channelIdField: Field = WidgetChatListAdapter.HandlerOfUpdates::class.java.getDeclaredField("channelId").apply { isAccessible = true }
    val itemAnimatorField: Field = WidgetChatList::class.java.getDeclaredField("defaultItemAnimator").apply { isAccessible = true }
    val highlightedMessageView: AtomicReference<View?> = AtomicReference()

    override fun start(context: Context) {
        // Modified reimplementation of original function that concatenates
        // channel loading and message jumping Observables to avoid synchronization issues
        patcher.instead<WidgetChatList>("onViewBoundOrOnResume") {

            val adapter = WidgetChatList.`access$getAdapter$p`(this)
            val binding = bindingGetter(this) as WidgetChatListBinding
            itemAnimatorField[this] = binding.b.itemAnimator

            adapter.setHandlers()
            adapter.onResume()

            val channelObservable =
                ObservableExtensionsKt.ui(ObservableExtensionsKt.computationLatest(WidgetChatListModel.Companion!!.get()), this, adapter)
            val channelSubscriber = RxUtils.createActionSubscriber<WidgetChatListModel>(
                onNext = { widgetModel ->
                    // Prevent auto scroller from getting executed *after* jumping action
                    // else it would instantly scroll back to bottom
                    val handler = WidgetChatListAdapter.`access$getHandlerOfUpdates$p`(adapter)
                    // id from chat list model doesn't seem reliable
                    channelIdField[handler] = StoreStream.Companion!!.channelsSelected.id
                    WidgetChatListAdapter.`access$setTouchedSinceLastJump$p`(adapter, true)
                    WidgetChatList.`access$configureUI`(this, widgetModel)
                },
                onError = logger::error
            )
            channelObservable.subscribe(channelSubscriber)

            val scrollObservable = ObservableExtensionsKt.ui(StoreStream.Companion!!.messagesLoader.scrollTo, this, null)
            val scrollSubscriber = RxUtils.createActionSubscriber<Long>(
                onNext = { messageId -> WidgetChatList.`access$scrollTo`(this, messageId) },
                onError = logger::error
            )
            scrollObservable.subscribe(scrollSubscriber)

            Observable.m(channelObservable, scrollObservable) // Observable.concat()
        }
        // For auto-expanding blocked messages on jump
        patcher.before<WidgetChatListAdapter.ScrollToWithHighlight>("run") {
            if (JumpToMessageSettings.autoExpandBlockedMessages) {
                val storeChat = StoreStream.Companion!!.chat
                // the public getter method in StoreChat returns a snapshot list instead of live
                val expandedBlockedMessages = StoreChat.`access$getExpandedBlockedMessageGroups$p`(storeChat)
                if (messageId !in expandedBlockedMessages) {
                    expandedBlockedMessages.add(messageId)
                }
                storeChat.markChanged()
            }
        }
        // Custom message highlighting implementation. Message won't de-highlight until user taps on chat.
        patcher.instead<WidgetChatListAdapter.ScrollToWithHighlight>("animateHighlight", View::class.java) { param ->
            val view = param.args[0] as View
            val highlightDrawable = Utils.getResId("drawable_bg_highlight", "drawable")
            view.setBackgroundResource(highlightDrawable)
            val transitionDrawable = view.background as TransitionDrawable
            transitionDrawable.startTransition(500)
            highlightedMessageView.set(view)
        }
        patcher.before<WidgetChatListAdapter.HandlerOfTouches>("onTouch", View::class.java, MotionEvent::class.java) {
            val messageView = highlightedMessageView.getAndSet(null) ?: return@before
            val transitionDrawable = messageView.background as TransitionDrawable
            transitionDrawable.reverseTransition(500)
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
