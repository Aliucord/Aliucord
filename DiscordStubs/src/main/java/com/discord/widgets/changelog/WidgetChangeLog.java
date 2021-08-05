package com.discord.widgets.changelog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import androidx.fragment.app.Fragment;
import com.discord.app.AppFragment;

/* compiled from: WidgetChangeLog.kt */
public final class WidgetChangeLog extends AppFragment {
    public static final Companion Companion = new Companion();
    
    /* compiled from: WidgetChangeLog.kt */
    public static final class Companion {
        private Companion() {
        }

        public final void launch(Context context, String dateOrSubtitle, String revision, String videoOrImageUrl, String body) {
            
        }
    }

    public WidgetChangeLog() {
        super();
    }

    /* private final void configureFooter() {
        getBinding().g.setOnClickListener(new WidgetChangeLog$configureFooter$1(this));
        getBinding().f1695c.setOnClickListener(new WidgetChangeLog$configureFooter$2(this));
        getBinding().d.setOnClickListener(new WidgetChangeLog$configureFooter$3(this));
    } */

    public static final void launch(Context context, String dateOrSubtitle, String revision, String videoOrImageUrl, String body) {
        Companion.launch(context, dateOrSubtitle, revision, videoOrImageUrl, body);
    }
}
