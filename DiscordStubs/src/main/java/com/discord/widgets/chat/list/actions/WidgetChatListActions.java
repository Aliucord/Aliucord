package com.discord.widgets.chat.list.actions;

import com.discord.app.AppBottomSheet;
import com.discord.databinding.WidgetChatListActionsBinding;
import com.discord.models.domain.ModelMessage;

@SuppressWarnings("unused")
public final class WidgetChatListActions extends AppBottomSheet {
    public int getContentViewResId() { return 0; }

    public static final class Model {
        public final ModelMessage getMessage() { return null; }
    }
    
    private WidgetChatListActionsBinding getBinding() { return new WidgetChatListActionsBinding(); }
}
