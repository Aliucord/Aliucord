package com.aliucord.api;

import android.view.View;

import com.aliucord.utils.ReflectUtils;
import com.discord.api.botuikit.*;
import com.discord.models.message.Message;
import com.discord.stores.StoreStream;

import java.util.*;

public class ButtonsAPI {
    private static final Map<Integer, View.OnClickListener> listeners = new HashMap<>();
    private static int lastListenerID = 0;

    /**
     * Adds ButtonComponent to Message object
     *
     * @param message         Message you want to add button.
     * @param buttonComponent ButtonComponent object, You can create it using ButtonsAPI.createButton().
     */
    public static void addButton(Message message, ButtonComponent buttonComponent) {
        var components = message.getComponents();
        if (components == null) {
            components = new ArrayList<>();
            try {
                ReflectUtils.setField(message, "components", components);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // DONT CARE;
            }
        }
        components.add(buttonComponent);
        StoreStream.access$getDispatcher$p(StoreStream.getPresences().getStream()).schedule(() -> {
            StoreStream.access$handleMessageUpdate(StoreStream.getPresences().getStream(), message.synthesizeApiMessage());
            return null;
        });
    }

    /**
     * Creates ButtonComponent object, To use this call ButtonsAPI.addButton()
     *
     * @param label           Label of the Button.
     * @param buttonStyle     Type of the Button.
     * @param onClickListener Listener that will get called when button is clicked
     * @return ButtonComponent object, if some error happens returns null
     */
    public static ButtonComponent createButton(String label, ButtonStyle buttonStyle, View.OnClickListener onClickListener) {
        try {
            var id = ButtonsAPI.generateListenerID(onClickListener);
            var component = ReflectUtils.allocateInstance(ButtonComponent.class);
            ReflectUtils.setField(component, "label", label);
            ReflectUtils.setField(component, "disabled", false);
            ReflectUtils.setField(component, "type", ComponentType.BUTTON);
            ReflectUtils.setField(component, "style", buttonStyle);
            ReflectUtils.setField(component, "url", "aliucord://" + id);
            return component;
        } catch (Exception e) {
            return null;
        }
    }

    private static synchronized int generateListenerID(View.OnClickListener listener) {
        listeners.put(lastListenerID, listener);
        return lastListenerID++;
    }

    /**
     * Calls listener with id
     *
     * @param id ID of the listener.
     * @param v  View that is clicked.
     */
    public static void callListenerWithID(int id, View v) {
        if (listeners.containsKey(id)) listeners.get(id).onClick(v);
    }
}
