package com.aliucord.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.aliucord.Utils
import com.discord.widgets.changelog.WidgetChangeLog

object ChangelogUtils {
    /**
     * Show ChangeLog modal
     *
     * @param context Context
     * @param version Version shown in the header
     * @param media Url to an image or a video that is displayed above body, null for nothing
     * @param body Changelog text in markdown (with custom discord rules)
     * @param footerActions Icons shown in the footer
     */
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
        Utils.openPage(context, WidgetChangeLog::class.java, Intent().putExtras(bundle))
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

        private constructor(input: Parcel) {
            drawableResourceId = input.readInt()
            url = input.readString()!!
        }

        constructor(@DrawableRes drawableResourceId: Int, url: String) {
            this.drawableResourceId = drawableResourceId
            this.url = url
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<FooterAction?> =
                object : Parcelable.Creator<FooterAction?> {
                    override fun createFromParcel(input: Parcel): FooterAction {
                        return FooterAction(input)
                    }

                    override fun newArray(size: Int): Array<FooterAction?> {
                        return arrayOfNulls(size)
                    }
                }
        }
    }
}