package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.instead
import com.aliucord.utils.ReflectUtils
import com.aliucord.utils.RxUtils
import com.aliucord.utils.RxUtils.subscribe
import com.discord.databinding.WidgetChatListBinding
import com.discord.stores.StoreStream
import com.discord.utilities.rx.ObservableExtensionsKt
import com.discord.widgets.chat.list.WidgetChatList
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.model.WidgetChatListModel
import rx.Observable
import java.lang.reflect.Field
import java.lang.reflect.Method

internal class JumpToMessageFix : CorePlugin(Manifest("JumpToMessageFix")) {
    init {
        manifest.description = "Fixes message links not jumping to correct message."
    }

    val bindingGetter: Method = WidgetChatList::class.java.getDeclaredMethod("getBinding").apply { isAccessible = true }
    val channelIdField: Field = WidgetChatListAdapter.HandlerOfUpdates::class.java.getDeclaredField("channelId").apply { isAccessible = true }
    val itemAnimatorField: Field = WidgetChatList::class.java.getDeclaredField("defaultItemAnimator").apply { isAccessible = true }

    override fun start(context: Context) {
        // Modified reimplementation of original function that concatenates
        // channel loading and message jumping Observables to avoid synchronization issues
        patcher.instead<WidgetChatList>("onViewBoundOrOnResume") {
            val adapter = WidgetChatList.`access$getAdapter$p`(this)
            val binding = bindingGetter(this) as WidgetChatListBinding
            itemAnimatorField[this] = binding.b.itemAnimator

            adapter.setHandlers()
            adapter.onResume()

            val channelObservable = ObservableExtensionsKt.ui(ObservableExtensionsKt.computationLatest(WidgetChatListModel.Companion!!.get()), this, adapter)
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

            val scrollObservable = ObservableExtensionsKt.ui(StoreStream.Companion!!.messagesLoader.scrollTo,this,null)
            val scrollSubscriber = RxUtils.createActionSubscriber<Long>(
                onNext = { messageId -> WidgetChatList.`access$scrollTo`(this, messageId) }, 
                onError = logger::error
            )
            scrollObservable.subscribe(scrollSubscriber)

            Observable.m(channelObservable, scrollObservable) // Observable.concat()
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
