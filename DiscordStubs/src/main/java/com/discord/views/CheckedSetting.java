package com.discord.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import rx.functions.Action1;

@SuppressWarnings({"unused"})
public final class CheckedSetting extends RelativeLayout implements Checkable {
    public enum ViewType {
        CHECK, RADIO, SWITCH;

        private static final ViewType[] VALUES = values();
    }

    public interface b {
        /** text */
        TextView a();
        /** root */
        @SuppressWarnings("MethodNameSameAsClassName")
        View b();
        /** subtext */
        TextView f();
    }

    public b k;

    public boolean isChecked() { return false; }
    public void toggle() {}

    public CheckedSetting(Context context, AttributeSet attrs) { super(context, attrs, 0); }

    public void setChecked(boolean checked) {}
    public void setOnCheckedListener(Action1<Boolean> listener) {}

    public final void setText(CharSequence text) {}
    public final void setSubtext(CharSequence text) {}

    /** setOnClick (?) */
    public void e(View.OnClickListener listener) {}
    /** setType (?) */
    public void f(ViewType type) {}
}
