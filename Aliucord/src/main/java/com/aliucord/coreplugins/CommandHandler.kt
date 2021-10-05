/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import android.widget.TextView
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.discord.api.message.MessageTypes
import com.discord.databinding.WidgetChatInputAutocompleteItemBinding
import com.discord.models.commands.*
import com.discord.models.message.Message
import com.discord.models.user.CoreUser
import com.discord.stores.*
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.widgets.chat.input.`WidgetChatInput$configureSendListeners$2`
import com.discord.widgets.chat.input.autocomplete.ApplicationCommandAutocompletable
import com.discord.widgets.chat.input.autocomplete.adapter.AutocompleteItemViewHolder
import com.discord.widgets.chat.input.models.ApplicationCommandData
import com.discord.widgets.chat.input.models.ApplicationCommandValue
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.chat.list.sheet.WidgetApplicationCommandBottomSheetViewModel
import top.canyie.pine.callback.MethodReplacement

@Suppress("UNCHECKED_CAST")
internal class CommandHandler : Plugin() {
  init {
    Manifest().run {
      name = "CommandHandler"
      initialize(this)
    }
  }

  override fun load(context: Context) {
    Patcher.addPatch(BuiltInCommands::class.java, "getBuiltInCommands", emptyArray(), PinePatchFn {
      val list = it.result.run { if (this == null) return@PinePatchFn else this as MutableList<ApplicationCommand?> }
      val addList = CommandsAPI.commands.values
      if (!list.containsAll(addList))
        with(if (list is ArrayList<ApplicationCommand?>) list else ArrayList(list).apply { it.result = this }) {
          removeAll(addList)
          addAll(addList)
        }
    })

    val storeApplicationCommands = StoreApplicationCommands::class.java
    Patcher.addPatch(storeApplicationCommands, "getApplications", emptyArray(), PinePatchFn {
      val list = it.result.run { if (this == null) return@PinePatchFn else this as MutableList<Application?> }
      val acApp = CommandsAPI.getAliucordApplication()
      if (!list.contains(acApp))
        with(if (list is ArrayList<Application?>) list else ArrayList(list).apply { it.result = this }) {
          if (size == 0) add(acApp) else add(size - 1, acApp)
        }
    })

    Patcher.addPatch(storeApplicationCommands, "getApplicationMap", emptyArray(), PinePatchFn {
      val map = it.result.run { if (this == null) return@PinePatchFn else this as MutableMap<Long?, Application?> }
      if (!map.containsKey(CommandsAPI.ALIUCORD_APP_ID))
        with(if (map is LinkedHashMap<Long?, Application?>) map else LinkedHashMap(map).apply { it.result = this }) {
          this[CommandsAPI.ALIUCORD_APP_ID] = CommandsAPI.getAliucordApplication()
        }
    })

    Patcher.addPatch(storeApplicationCommands, "handleGuildApplicationsUpdate", arrayOf(List::class.java), PinePrePatchFn {
      val list = it.result.run { if (this == null) return@PinePrePatchFn else this as MutableList<Application?> }
      if (!list.contains(CommandsAPI.getAliucordApplication()))
        with(if (list is ArrayList<Application?>) list else ArrayList(list).apply { it.args[0] = this }) {
          add(CommandsAPI.getAliucordApplication())
        }
    })

    Patcher.addPatch(StoreLocalMessagesHolder::class.java, "messageCacheTryPersist", emptyArray(), MethodReplacement.DO_NOTHING)

    // needed to reimplement this to:
    // 1. don't send command result if not needed
    // 2. fully support arguments in built-in subcommands
    // 3. clear input after executing command
    Patcher.addPatch(
      `WidgetChatInput$configureSendListeners$2`::class.java.getDeclaredMethod("invoke", List::class.java, ApplicationCommandData::class.java, Function1::class.java),
      PinePrePatchFn {
        val data = it.args[1] as ApplicationCommandData? ?: return@PinePrePatchFn
        val command = data.applicationCommand.also { c -> if (c == null || c !is RemoteApplicationCommand || !c.builtIn) return@PinePrePatchFn }
        val values = data.values ?: return@PinePrePatchFn
        val commandArgs = LinkedHashMap<String, Any?>(values.size).apply { addValues(this, values) }
        val execute = command.execute ?: return@PinePrePatchFn
        commandArgs["__this"] = it.thisObject as `WidgetChatInput$configureSendListeners$2`
        commandArgs["__args"] = it.args
        execute(commandArgs)
        it.result = true
      }
    )

    // Show Plugin name instead of 'Aliucord' in the command list
    val autocompleteItemViewHolder = AutocompleteItemViewHolder::class.java
    val bindingField = autocompleteItemViewHolder.getDeclaredField("binding").apply { isAccessible = true }
    Patcher.addPatch(
      autocompleteItemViewHolder.getDeclaredMethod("bindCommand", ApplicationCommandAutocompletable::class.java),
      PinePatchFn {
        val cmd = (it.args[0] as ApplicationCommandAutocompletable).command.run { if (this is ApplicationSubCommand) rootCommand else this }
          .apply { if (!builtIn) return@PinePatchFn }
        val plugin = CommandsAPI.commandsAndPlugins[cmd.name] ?: return@PinePatchFn
        val binding = bindingField[it.thisObject] as WidgetChatInputAutocompleteItemBinding
        binding.f.text = plugin.uppercase()
      }
    )

    Patcher.addPatch(Message::class.java, "isLocalApplicationCommand", arrayOf(), PinePrePatchFn {
      with(it.thisObject as Message) {
        val type = type ?: return@PinePrePatchFn
        if (isLoading && type != MessageTypes.LOCAL_APPLICATION_COMMAND && type != MessageTypes.LOCAL_APPLICATION_COMMAND_SEND_FAILED)
          it.result = true
      }
    })

    // don't mark Aliucord command messages as
    Patcher.addPatch(
      WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("processMessageText", SimpleDraweeSpanTextView::class.java, MessageEntry::class.java),
      PinePatchFn {
        val message = (it.args[1] as MessageEntry).message ?: return@PinePatchFn
        if (message.isLocal && CoreUser(message.author).id == -1L) with(it.args[0] as TextView) {
          if (alpha != 1.0f) alpha = 1.0f
        }
      }
    )

    Patcher.addPatch(WidgetApplicationCommandBottomSheetViewModel::class.java, "requestInteractionData", arrayOf(), PinePrePatchFn {
      with(it.thisObject as WidgetApplicationCommandBottomSheetViewModel) {
        if (applicationId != -1L) return@PinePrePatchFn
        val state = CommandsAPI.interactionsStore[interactionId]
        if (state != null) WidgetApplicationCommandBottomSheetViewModel.`access$handleStoreState`(this, state)
        it.result = null
      }
    })
  }

  private fun addValues(map: LinkedHashMap<String, Any?>, values: List<ApplicationCommandValue>) {
    for (v in values) {
      val name = v.name
      val value = v.value
      val options = v.options
      if (value == null && options != null) {
        val optionsMap = LinkedHashMap<String, Any?>()
        addValues(optionsMap, options)
        map[name] = optionsMap
      } else map[name] = value
    }
  }

  override fun start(context: Context) {}
  override fun stop(context: Context) {}
}
