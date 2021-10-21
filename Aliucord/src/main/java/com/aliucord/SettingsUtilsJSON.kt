package com.aliucord

import com.aliucord.PluginManager.logger
import com.aliucord.utils.GsonUtils
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type
import java.util.*

/** Utility class to store and retrieve preferences  */

class SettingsUtilsJSON(plugin: String) {


    val settingsPath = Constants.BASE_PATH + "/settings/"
    val settingsFile = Constants.BASE_PATH + "/settings/" + plugin + ".json"
    var settings: JSONObject = JSONObject()
    val keyPrefix = "AC_" + plugin + "_"

    init {
        val dir = File(settingsPath)

        if (!dir.exists()) {
            dir.mkdir()
        }

        val file = File(settingsFile)

        if (file.exists()) {
            val read: String = FileReader(file).readText()
            if (read != "") settings = JSONObject(read)

        }



        try {


            if (SettingsUtils.getBool(keyPrefix + "migratedToJson", false)) {
                getPreferenceSettings()?.forEach {

                    val keyName = it.key.replace(keyPrefix, "").trim()
                    if (keyName == "migratedToJson") return@forEach
                    it.value?.let { it1 -> settings.put(keyName, it1) }
                    SettingsUtils.remove(it.key)
                    SettingsUtils.setBool(keyPrefix + "migratedToJson", true)
                }
                writeData()
                logger.info("'$plugin' Settings Are Migrated")

            }
             } catch (e: Exception) {
            logger.info("'$plugin' Settings couldn't migrated")
            logger.error(e)
        }


    }

    fun getPreferenceSettings(): Map<String, *>? {
        return SettingsUtils.getAllSettings(keyPrefix)
    }

    fun writeData() {
        if (settings.length() > 0) {

            val file = File(settingsFile)
            if (!file.exists()) file.createNewFile()

            val writer = FileWriter(file)
            writer.write(settings.toString(4))
            writer.flush()
            writer.close()
        }

    }

    /**
     * Resets All Settings
     * @return true if succesful, else false
     */
    fun resetSettings(): Boolean {
        settings = JSONObject()
        return File(settingsFile).delete()
    }

    /**
     * Toggles Boolean and returns it
     * @param key Key of the value
     * @param defVal Default Value if setting doesnt exist
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
    fun remove(key: String): Boolean {
        val bool = settings.remove(key) != null
        writeData()
        return bool
    }

    /**
     * Check if Key exists in settings
     * @param key Key of the value
     * @return True if found, else false
     */
    fun exists(key: String): Boolean {
        return settings.has(key)
    }

    /**
     * Get a boolean from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getBool(key: String, defValue: Boolean): Boolean {
        return if (settings.has(key)) settings.getBoolean(key) else defValue
    }

    /**
     * Set a boolean item
     * @param key Key of the item
     * @param val Value
     */
    fun setBool(key: String, `val`: Boolean) {
        settings.put(key, `val`)
        writeData()
    }

    /**
     * Get an int from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getInt(key: String, defValue: Int): Int {
        return if (settings.has(key)) settings.getInt(key) else defValue
    }

    /**
     * Set an int item
     * @param key Key of the item
     * @param val Value
     */
    fun setInt(key: String, `val`: Int) {
        settings.put(key, `val`)
        writeData()
    }

    /**
     * Get a float from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getFloat(key: String, defValue: Float): Float {
        return if (settings.has(key)) settings.get(key) as Float else defValue
    }

    /**
     * Set a float item
     * @param key Key of the item
     * @param val Value
     */
    fun setFloat(key: String, `val`: Float) {
        settings.put(key, `val`)
        writeData()
    }

    /**
     * Get a long from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getLong(key: String, defValue: Long): Long {
        return if (settings.has(key)) settings.getLong(key) else defValue
    }

    /**
     * Set a long item
     * @param key Key of the item
     * @param val Value
     */
    fun setLong(key: String, `val`: Long) {
        settings.put(key, `val`)
        writeData()
    }

    /**
     * Get a [String] from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getString(key: String, defValue: String?): String? {
        return if (settings.has(key)) settings.getString(key) else defValue
    }

    /**
     * Set a [String] item
     * @param key Key of the item
     * @param val Value
     */
    fun setString(key: String, `val`: String?) {
        settings.put(key, `val`)
        writeData()
    }

    private val cache: MutableMap<String, Any> = HashMap()

    /**
     * Get an [Object] from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun <T> getObject(key: String, defValue: T): T {
        //return settings.get(key) as T ,this works but why change while other one is working
       return getObject(key, defValue, defValue!!::class.java)
    }

    /**
     * Get an [Object] from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @param type Type of the object
     * @return Value if found, else the defValue
     */
    fun <T> getObject(key: String, defValue: T, type: Type?): T {
        val cached = cache[key]
        if (cached != null) try {
            return cached as T
        } catch (ignored: Throwable) {
        }
        val json = getString(key, null) ?: return defValue
        val t: T = GsonUtils.fromJson(json, type)
        return t ?: defValue
    }

    /**
     * Set an [Object] item
     * @param key Key of the item
     * @param val Value
     */
    fun setObject(key: String, `val`: Any) {
        //settings.put(key,`val`)
        //writeData()
        cache.put(key, `val`)
        setString(key, GsonUtils.toJson(`val`))
    }
}