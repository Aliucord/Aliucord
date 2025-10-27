package com.aliucord.patcher

import com.aliucord.api.PatcherAPI
import com.aliucord.api.Unpatch
import de.robv.android.xposed.XC_MethodHook

private typealias HookCallback<T> = T.(XC_MethodHook.MethodHookParam) -> Unit
private typealias InsteadHookCallback<T> = T.(XC_MethodHook.MethodHookParam) -> Any?

/**
 * Replaces a constructor of a class.
 * @param paramTypes parameters of the method. Useful for patching individual overloads
 * @param callback callback for the patch
 * @return The [Unpatch] object of the patch
 * @see [XC_MethodHook.beforeHookedMethod]
 */
inline fun <reified T> PatcherAPI.instead(vararg paramTypes: Class<*>, crossinline callback: InsteadHookCallback<T>): Unpatch {
    return patch(T::class.java.getDeclaredConstructor(*paramTypes), object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            try {
                param.result = callback(param.thisObject as T, param)
            } catch (th: Throwable) {
                logger.error("Exception while replacing constructor of ${param.method.declaringClass}", th)
            }
        }
    })
}

/**
 * Replaces a method of a class.
 * @param methodName name of the method to patch
 * @param paramTypes parameters of the method. Useful for patching individual overloads
 * @param callback callback for the patch
 * @return The [Unpatch] object of the patch
 * @see [XC_MethodHook.beforeHookedMethod]
 */
inline fun <reified T> PatcherAPI.instead(methodName: String, vararg paramTypes: Class<*>, crossinline callback: InsteadHookCallback<T>): Unpatch {
    return patch(T::class.java.getDeclaredMethod(methodName, *paramTypes), object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            try {
                param.result = callback(param.thisObject as T, param)
            } catch (th: Throwable) {
                logger.error("Exception while replacing ${param.method.declaringClass.name}.${param.method.name}", th)
            }
        }
    })
}

/**
 * Adds a [PreHook] to a constructor of a class.
 * @param paramTypes parameters of the constructor. Useful for patching individual overloads
 * @param callback callback for the patch
 * @return The [Unpatch] object of the patch
 * @see [XC_MethodHook.beforeHookedMethod]
 */
inline fun <reified T> PatcherAPI.before(vararg paramTypes: Class<*>, crossinline callback: HookCallback<T>): Unpatch {
    return patch(T::class.java.getDeclaredConstructor(*paramTypes), object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            try {
                callback(param.thisObject as T, param)
            } catch (th: Throwable) {
                logger.error("Exception while pre-hooking constructor of ${param.method.declaringClass}", th)
            }
        }
    })
}

/**
 * Adds a [PreHook] to a method of a class.
 * @param methodName name of the method to patch
 * @param paramTypes parameters of the method. Useful for patching individual overloads
 * @param callback callback for the patch
 * @return The [Unpatch] object of the patch
 * @see [XC_MethodHook.beforeHookedMethod]
 */
inline fun <reified T> PatcherAPI.before(methodName: String, vararg paramTypes: Class<*>, crossinline callback: HookCallback<T>): Unpatch {
    return patch(T::class.java.getDeclaredMethod(methodName, *paramTypes), object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            try {
                callback(param.thisObject as T, param)
            } catch (th: Throwable) {
                logger.error("Exception while pre-hooking ${param.method.declaringClass.name}.${param.method.name}", th)
            }
        }
    })
}

/**
 * Adds a [Hook] to a constructor of a class.
 * @param paramTypes parameters of the constructor. Useful for patching individual overloads
 * @param callback callback for the patch
 * @return the [Unpatch] object of the patch
 * @see [XC_MethodHook.afterHookedMethod]
 */
inline fun <reified T> PatcherAPI.after(vararg paramTypes: Class<*>, crossinline callback: HookCallback<T>): Unpatch {
    return patch(T::class.java.getDeclaredConstructor(*paramTypes), object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            try {
                callback(param.thisObject as T, param)
            } catch (th: Throwable) {
                logger.error("Exception while hooking constructor of ${param.method.declaringClass}", th)
            }
        }
    })
}

/**
 * Adds a [Hook] to a method of a class.
 * @param methodName name of the method to patch
 * @param paramTypes parameters of the method. Useful for patching individual overloads
 * @param callback callback for the patch
 * @return the [Unpatch] object of the patch
 * @see [XC_MethodHook.afterHookedMethod]
 */
inline fun <reified T> PatcherAPI.after(methodName: String, vararg paramTypes: Class<*>, crossinline callback: HookCallback<T>): Unpatch {
    return patch(T::class.java.getDeclaredMethod(methodName, *paramTypes), object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            try {
                callback(param.thisObject as T, param)
            } catch (th: Throwable) {
                logger.error("Exception while hooking ${param.method.declaringClass.name}.${param.method.name}", th)
            }
        }
    })
}
