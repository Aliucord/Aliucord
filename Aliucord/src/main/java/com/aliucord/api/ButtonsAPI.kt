package com.aliucord.api

import android.content.Context

import com.aliucord.Logger
import com.aliucord.utils.ReflectUtils
import com.aliucord.coreplugins.ButtonsAPI

import com.discord.api.botuikit.*
import com.discord.models.message.Message
import com.discord.stores.StoreStream

import java.util.*

@Suppress("UNCHECKED_CAST")
object ButtonsAPI {

    /**
     * Adds a button component to the message.
     * @param label   The label of the button.
     * @param style   The style of the button. {@link ButtonStyle}
     * @param onPress Callback for when the button is pressed, passing the message as an argument.
     */
    @JvmStatic
    fun Message.addButton(label: String, style: ButtonStyle, onPress: (Message) -> Unit) {
        var components = this.components
        val id = -CommandsAPI.generateId()

        if(components == null) {
            components = ArrayList<Component>()
            try {
                ReflectUtils.setField(this, "components", components)
            } catch (_ : Throwable) {}
        }

        try{
            val buttonComponent = ReflectUtils.allocateInstance(ButtonComponent::class.java) as ButtonComponent
            ReflectUtils.setField(buttonComponent, "label", label)
            ReflectUtils.setField(buttonComponent, "style", style)
            ReflectUtils.setField(buttonComponent, "disabled", false)
            ReflectUtils.setField(buttonComponent, "type", ComponentType.BUTTON)
            ReflectUtils.setField(buttonComponent, "customId", "${-CommandsAPI.ALIUCORD_APP_ID}-${id}")

            var actionRow = ReflectUtils.allocateInstance(ActionRowComponent::class.java) as ActionRowComponent
            ReflectUtils.setField(actionRow, "components", ArrayList<Any>())
            ReflectUtils.setField(actionRow, "type", ComponentType.ACTION_ROW)

            if(components.size == 0) components.add(actionRow)
            actionRow = components.get(components.size - 1) as ActionRowComponent
            val actionRowItems = ReflectUtils.getField(actionRow, "components") as ArrayList<Component>
            actionRowItems.add(buttonComponent)

            ReflectUtils.setField(actionRow, "components", actionRowItems)
            ButtonsAPI.actions.put(id.toString(), onPress)

            StoreStream.`access$getDispatcher$p`(StoreStream.getPresences().stream).schedule { 
                StoreStream.`access$handleMessageUpdate`(StoreStream.getPresences().stream, this.synthesizeApiMessage())
            }
        } catch (t: Throwable) {
            Logger("ButtonsAPI").error("Failed to create button component", t)
        }
    }

}