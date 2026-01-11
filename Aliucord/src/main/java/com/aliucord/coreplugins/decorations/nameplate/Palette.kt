package com.aliucord.coreplugins.decorations.nameplate

import android.graphics.drawable.GradientDrawable
import androidx.core.graphics.ColorUtils
import com.aliucord.Logger
import com.discord.stores.StoreStream

import androidx.annotation.ColorInt

internal enum class NameplatesPalette(@ColorInt val dark: Int, @ColorInt val light: Int) {
    None(0, 0),
    Crimson(0x900007, 0xE7040F),
    Berry(0x893A99, 0xB11FCF),
    Sky(0x0080B7, 0x56CCFF),
    Teal(0x086460, 0x7DEED7),
    Forest(0x2D5401, 0x6AA624),
    BubbleGum(0xDC3E97, 0xF957B3),
    Violet(0x730BC8, 0x972FED),
    Cobalt(0x0131C2, 0x4278FF),
    Clover(0x047B20, 0x63CD5A),
    Lemon(0xF6CD12, 0xFED400),
    White(0xFFFFFF, 0xFFFFFF),
    ;

    companion object {
        fun from(color: String): NameplatesPalette {
            return when (color) {
                "none" -> None
                "crimson" -> Crimson
                "berry" -> Berry
                "sky" -> Sky
                "teal" -> Teal
                "forest" -> Forest
                "bubble_gum" -> BubbleGum
                "violet" -> Violet
                "cobalt" -> Cobalt
                "clover" -> Clover
                "lemon" -> Lemon
                "white" -> White
                else -> {
                    Logger("Decorations/Palette").warn("Unknown nameplate color $color")
                    None
                }
            }
        }
    }

    fun drawable(): GradientDrawable {
        val color = if (StoreStream.getUserSettingsSystem().theme == "light") light else dark

        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                0,
                ColorUtils.setAlphaComponent(color, 150)
            )
        )
    }
}
