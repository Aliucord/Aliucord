@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package com.aliucord.patcher

import de.robv.android.xposed.XC_MethodHook.MethodHookParam

inline operator fun MethodHookParam.component1() = this
inline operator fun <T> MethodHookParam.component2(): T = args[0] as T
inline operator fun <T> MethodHookParam.component3(): T = args[1] as T
inline operator fun <T> MethodHookParam.component4(): T = args[2] as T
inline operator fun <T> MethodHookParam.component5(): T = args[3] as T
inline operator fun <T> MethodHookParam.component6(): T = args[4] as T
inline operator fun <T> MethodHookParam.component7(): T = args[5] as T
inline operator fun <T> MethodHookParam.component8(): T = args[6] as T
inline operator fun <T> MethodHookParam.component9(): T = args[7] as T
inline operator fun <T> MethodHookParam.component10(): T = args[8] as T
inline operator fun <T> MethodHookParam.component11(): T = args[9] as T
inline operator fun <T> MethodHookParam.component12(): T = args[10] as T
