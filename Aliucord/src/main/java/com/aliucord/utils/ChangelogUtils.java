package com.aliucord.utils;

import android.content.Context;
import android.content.Intent;
import android.os.*;

import androidx.annotation.DrawableRes;

import com.discord.widgets.changelog.WidgetChangeLog;

import c.a.d.j;

public class ChangelogUtils {
    public static class FooterAction implements Parcelable {
        private final int drawableResourceId;
        private final String url;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(drawableResourceId);
            parcel.writeString(url);
        }

        public static final Parcelable.Creator<FooterAction> CREATOR = new Parcelable.Creator<>() {
            public FooterAction createFromParcel(Parcel in) {
                return new FooterAction(in);
            }

            public FooterAction[] newArray(int size) {
                return new FooterAction[size];
            }
        };

        private FooterAction(Parcel in) {
            drawableResourceId = in.readInt();
            url = in.readString();
        }

        public FooterAction(@DrawableRes int drawableResourceId, String url) {
            this.drawableResourceId = drawableResourceId;
            this.url = url;
        }

        public int getDrawableResourceId() {
            return drawableResourceId;
        }

        public String getUrl() {
            return url;
        }
    }

    public static void show(Context context, String version, String media, String body, FooterAction... footerActions) {
        Bundle bundle = new Bundle();
        bundle.putString("INTENT_EXTRA_VERSION", version);
        bundle.putString("INTENT_EXTRA_REVISION", "1");
        bundle.putString("INTENT_EXTRA_VIDEO", media);
        bundle.putString("INTENT_EXTRA_BODY", body);
        bundle.putParcelableArray("INTENT_EXTRA_FOOTER_ACTIONS", footerActions);
        j.d(context, WidgetChangeLog.class, new Intent().putExtras(bundle));
    }
}
