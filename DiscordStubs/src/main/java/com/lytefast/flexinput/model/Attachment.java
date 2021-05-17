package com.lytefast.flexinput.model;

import android.os.Parcel;
import android.os.Parcelable;

@SuppressWarnings("unused")
public class Attachment<T> implements Parcelable {
    public static final Parcelable.Creator<Attachment<?>> CREATOR = null;

    public int describeContents() { return 0; }
    public void writeToParcel(Parcel dest, int flags) {}
}
