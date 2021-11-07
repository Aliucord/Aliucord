package com.aliucord.views

import android.content.Context
import android.view.ContextThemeWrapper
import com.google.android.material.button.MaterialButton
import com.lytefast.flexinput.R

/** Red MaterialButton
 *
 * Creates a red Discord styled button.
 * @param context [Context]
 */
class DangerButton
    (context: Context?) :
    MaterialButton(ContextThemeWrapper(context, R.i.UiKit_Material_Button_Red), null, 0)
