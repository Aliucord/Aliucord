package com.aliucord.injector

import android.annotation.SuppressLint
import android.content.Context
import dalvik.system.BaseDexClassLoader
import java.io.File
import java.io.FileOutputStream

/**
 * Adds a dex file/container to the front of the global classloader classpath.
 * This causes any conflicting classes defined in the dex to override everything further down the list.
 *
 * This private api seems to be stable, thanks to Facebook who use it in the Facebook app
 */
@SuppressLint("DiscouragedPrivateApi")
internal fun addDexToClasspath(dexFile: File, classLoader: ClassLoader) {
    // BaseDexClassLoader#pathList -> DexPathList
    // https://android.googlesource.com/platform/libcore/+/58b4e5dbb06579bec9a8fc892012093b6f4fbe20/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java#59
    val pathList = BaseDexClassLoader::class.java.getDeclaredField("pathList")
        .apply { isAccessible = true }
        .get(classLoader)
    // DexPathList#addDexPath(String dexPath, File optimizedDirectory)
    val addDexPath = pathList.javaClass.getDeclaredMethod("addDexPath", String::class.java, File::class.java)
        .apply { isAccessible = true }
    addDexPath.invoke(pathList, dexFile.absolutePath, null)
}

/**
 * Try to prevent method inlining by deleting the usage profile used by AOT compilation
 * https://source.android.com/devices/tech/dalvik/configure#how_art_works
 */
internal fun pruneArtProfile(ctx: Context) {
    val profile = File("/data/misc/profiles/cur/0/${ctx.packageName}/primary.prof")
    if (profile.exists() && profile.length() > 0) {
        try {
            // Clear file contents
            FileOutputStream(profile).close()
        } catch (e: Throwable) {
            Logger.w("Failed to clear ART usage profile", e)
        }
    }
}
