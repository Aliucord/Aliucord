
package com.aliucord

import android.content.Context
import com.aliucord.Utils.appContext
import android.content.SharedPreferences
import com.aliucord.SettingsUtilsJSON
import com.aliucord.utils.GsonUtils
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type
import java.util.HashMap

import java.io.InputStream
/** Utility class to store and retrieve preferences  */

class SettingsUtilsJSON(plugin:String) {
    val logger = Logger("test")

    val settingsPath = Constants.BASE_PATH +"/settings/"
    val settingsFile=Constants.BASE_PATH +"/settings/"+plugin +".json"
    var settings: JSONObject? = null


    init {
        var dir = File(settingsPath)
        if (!dir.exists()){
            dir.mkdir()
        }
        var file = File(settingsFile)

        if (!file.exists())file.createNewFile()




        var read :String= FileReader(file).readText()
        if (read!="")settings = GsonUtils.fromJson(read,JSONObject::class.java) else settings= JSONObject()



    }

    fun writeData(){
        var file = File(settingsFile)
        var writer = FileWriter(file)
        writer.write(GsonUtils.toJson(settings))
        writer.flush()
        writer.close()
    }


    /**
     * Get a boolean from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */

    fun getBool(key: String?, defValue: Boolean): Boolean {
        return if (settings!!.has(key)) settings!!.getBoolean(key) else defValue
    }

    /**
     * Set a boolean item
     * @param key Key of the item
     * @param val Value
     */
    fun setBool(key: String?, `val`: Boolean) {
        settings!!.put(key,`val`)
        writeData()
    }

    /**
     * Get an int from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getInt(key: String?, defValue: Int): Int {
        return if (settings!!.has(key)) settings!!.getInt(key) else defValue
    }

    /**
     * Set an int item
     * @param key Key of the item
     * @param val Value
     */
    fun setInt(key: String?, `val`: Int) {
        settings!!.put(key,`val`)
        writeData()
    }

    /**
     * Get a float from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getFloat(key: String?, defValue: Float): Float {
        //TODO take a look to that later
        return if (settings!!.has(key)) settings!!.get(key) as Float else defValue
    }

    /**
     * Set a float item
     * @param key Key of the item
     * @param val Value
     */
    fun setFloat(key: String?, `val`: Float) {
        settings!!.put(key,`val`)
        writeData()
    }

    /**
     * Get a long from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getLong(key: String?, defValue: Long): Long {
        return if (settings!!.has(key)) settings!!.getLong(key) else defValue
    }

    /**
     * Set a long item
     * @param key Key of the item
     * @param val Value
     */
    fun setLong(key: String?, `val`: Long) {
        settings!!.put(key,`val`)
        writeData()
    }

    /**
     * Get a [String] from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun getString(key: String?, defValue: String?): String? {
        return if (settings!!.has(key)) settings!!.getString(key) else defValue
    }

    /**
     * Set a [String] item
     * @param key Key of the item
     * @param val Value
     */
    fun setString(key: String?, `val`: String?) {
        settings!!.put(key,`val`)
        writeData()
    }

    private val cache: MutableMap<String, Any> = HashMap()

    /**
     * Get an [Object] from the preferences
     * @param key Key of the value
     * @param defValue Default value
     * @return Value if found, else the defValue
     */
    fun <T> getObject(key: String, defValue: T): T? {
        //remove ? from T?
    return null
    //return getObject(key, defValue, defValue.javaClass)
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
        cache.put(key, `val`)
        setString(key, GsonUtils.toJson(`val`))
    }
}