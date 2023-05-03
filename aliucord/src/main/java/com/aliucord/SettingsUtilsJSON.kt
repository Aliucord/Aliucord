package com.aliucord

import com.aliucord.PluginManager.logger
import com.aliucord.settings.*
import com.aliucord.utils.GsonUtils.fromJson
import com.aliucord.utils.GsonUtils.gson
import com.aliucord.utils.GsonUtils.toJson
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.reflect.Type
import java.math.BigDecimal
import java.util.*

@Suppress("unused")
/** Utility class to store and retrieve preferences  */
class SettingsUtilsJSON(plugin: String) {
    private val settingsPath = Constants.SETTINGS_PATH + "/"
    private val settingsFile = "$settingsPath$plugin.json"
    private val cache: MutableMap<String, Any> = HashMap()
    private val settings: JSONObject by lazy {
        val file = File(settingsFile)
        if (file.exists()) {
            val read = file.readText()
            if (read != "") return@lazy JSONObject(read)
        }
        JSONObject()
    }

    init {
        val dir = File(settingsPath)
        if (!dir.exists() && !dir.mkdir()) throw RuntimeException("Failed to create settings dir")
    }

    private fun writeData() {
        if (settings.length() > 0) {
            val file = File(settingsFile)
            try {
                file.writeText(settings.toString(4))
            } catch (e: Throwable) {
                logger.error("Failed to save settings", e)
            }
        }
    }

    /**
     * Resets All Settings
     * @return true if successful, else false
     */
    fun resetFile() = File(settingsFile).delete()

    /**
     * Toggles Boolean and returns it
     * @param key Key of the value
     * @param defVal Default Value if setting doesn't exist
     * @return Toggled boolean
     */
    fun toggleBool(key: String, defVal: Boolean): Boolean {
        getBool(key, !defVal).also {
            setBool(key, !it)
            return !it
        }
    }

    /**
     * Removes Item from settings
     * @param key Key of the value
     * @return True if removed, else false
     */
    @Synchronized
    fun remove(key: String): Boolean {
        val bool = settings.remove(key) != null
        writeData()
        return bool
    }

    /**
     * Gets All Keys from settings
     * @return List of all keys
     */
    fun getAllKeys(): List<String> {
        val iterator: Iterator<String> = settings.keys()
        val copy: MutableList<String> = ArrayList()
        while (iterator.hasNext()) copy.add(iterator.next())
        return copy
    }

    /**
     * Check if Key exists in settings
     * @param key Key of the value
     * @return True if found, else false
     */
    fun exists(key: String): Boolean = settings.has(key)

    /**
     * Get a boolean from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getBool(key: String, defValue: Boolean) = if (settings.has(key)) settings.getBoolean(key) else defValue

    /**
     * Set a boolean item
     * @param key Key of the item
     * @param value Value
     */
    fun setBool(key: String, value: Boolean) = putObject(key, value)

    /**
     * Get an int from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getInt(key: String, defValue: Int) =
        if (settings.has(key)) settings.getInt(key) else defValue

    @Synchronized
    private fun putObject(key: String, value: Any?) {
        settings.put(key, value)
        writeData()
    }

    /**
     * Set an int item
     * @param key Key of the item
     * @param value Value
     */
    fun setInt(key: String, value: Int) = putObject(key, value)

    /**
     * Get a float from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getFloat(key: String, defValue: Float) =
        if (settings.has(key)) BigDecimal.valueOf(settings.getDouble(key)).toFloat() else defValue

    /**
     * Set a float item
     * @param key Key of the item
     * @param value Value
     */
    fun setFloat(key: String, value: Float) = putObject(key, value)

    /**
     * Get a long from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getLong(key: String, defValue: Long) = if (settings.has(key)) settings.getLong(key) else defValue

    /**
     * Set a long item
     * @param key Key of the item
     * @param value Value
     */
    fun setLong(key: String, value: Long) = putObject(key, value)

    /**
     * Get a [String] from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getString(key: String, defValue: String?) =
        if (settings.has(key)) settings.getString(key) else defValue

    /**
     * Set a [String] item
     * @param key Key of the item
     * @param value Value
     */
    fun setString(key: String, value: String?) = putObject(key, value)

    /**
     * Get a [JSONObject] item
     * @param key Key of the item
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getJSONObject(key: String, defValue: JSONObject?) =
        if (settings.has(key)) settings.getJSONObject(key) else defValue

    /**
     * Set a [JSONObject] item
     * @param key Key of the item
     * @param value Value
     */
    fun setJSONObject(key: String, value: JSONObject) = putObject(key, value)

    /**
     * Get an [Object] from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun <T> getObject(key: String, defValue: T): T = getObject(key, defValue, defValue!!::class.java)

    /**
     * Get an [Object] from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @param type Type of the object
     * @return Value if found, else the defValue
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getObject(key: String, defValue: T, type: Type?): T {
        val cached = cache[key]
        if (cached != null) try {
            return cached as T
        } catch (ignored: Throwable) {
        }
        val t: T? = if (settings.has(key)) gson.fromJson(settings.getString(key), type) else null
        return t ?: defValue
    }

    /**
     * Set an [Object] item
     * @param key Key of the item
     * @param value Value
     */
    fun setObject(key: String, value: Any) {
        cache[key] = value
        val stringJson = gson.toJson(value)
        putObject(key, if (stringJson.startsWith("{")) JSONObject(stringJson) else JSONArray(stringJson))
    }
}
