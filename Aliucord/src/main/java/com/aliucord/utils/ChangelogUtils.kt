package com.aliucord.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes
import c.a.d.j
import com.discord.widgets.changelog.WidgetChangeLog

object ChangelogUtils {
    @JvmStatic
    fun show(
        context: Context,
        version: String,
        media: String?,
        body: String,
        vararg footerActions: FooterAction
    ) {
        val bundle = Bundle()
        bundle.putString("INTENT_EXTRA_VERSION", version)
        bundle.putString("INTENT_EXTRA_REVISION", "1")
        bundle.putString("INTENT_EXTRA_VIDEO", media)
        bundle.putString("INTENT_EXTRA_BODY", body)
        bundle.putParcelableArray("INTENT_EXTRA_FOOTER_ACTIONS", footerActions)
        j.d(context, WidgetChangeLog::class.java, Intent().putExtras(bundle))
    }

    class FooterAction : Parcelable {
        val drawableResourceId: Int
        val url: String

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(parcel: Parcel, i: Int) {
            parcel.writeInt(drawableResourceId)
            parcel.writeString(url)
        }

        private constructor(`in`: Parcel) {
            drawableResourceId = `in`.readInt()
            url = `in`.readString()!!
        }

        constructor(@DrawableRes drawableResourceId: Int, url: String) {
            this.drawableResourceId = drawableResourceId
            this.url = url
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<FooterAction?> =
                object : Parcelable.Creator<FooterAction?> {
                    override fun createFromParcel(`in`: Parcel): FooterAction {
                        return FooterAction(`in`)
                    }

                    override fun newArray(size: Int): Array<FooterAction?> {
                        return arrayOfNulls(size)
                    }
                }
        }
    }
}