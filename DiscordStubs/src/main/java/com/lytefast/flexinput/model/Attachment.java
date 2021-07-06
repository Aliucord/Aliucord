package com.lytefast.flexinput.model;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.view.inputmethod.InputContentInfoCompat;

@SuppressWarnings("unused")
public class Attachment<T> implements Parcelable {
    public static final Parcelable.Creator<Attachment<?>> CREATOR = new Creator<Attachment<?>>() {
        public Attachment<?> createFromParcel(Parcel source) { return null; }
        public Attachment<?>[] newArray(int size) { return null; }
    };

    public int describeContents() { return 0; }
    public void writeToParcel(Parcel dest, int flags) {}

    public final Uri getUri() { return Uri.parse(""); }
    public final long getId() { return 0; }
    public final String getDisplayName() { return ""; }
    public final T getData() { return getData(); }

    public Attachment(long id, Uri uri, String displayName, T data) { }
    public Attachment(Parcel parcel) { }

    public static Attachment<Uri> toAttachment(Uri uri, ContentResolver contentResolver) { return new Attachment<>(null); }
    public static Attachment<InputContentInfoCompat> toAttachment(InputContentInfoCompat inputContentInfoCompat, ContentResolver contentResolver, boolean z2, String defaultName) { return new Attachment<>(null); }
}
