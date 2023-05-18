package x.code.session

import android.content.Context
import android.content.SharedPreferences

abstract class _BasePref(context: Context, prefName: String) {
    val pref: SharedPreferences
    val editor: SharedPreferences.Editor

    init {
        pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        editor = pref.edit()
    }


    fun clearAll() {
        editor.clear()
        editor.commit()
    }

    /*
    * GETTER GENERIC
    * */
    fun getBool(KEY: String, defValue: Boolean = false): Boolean {
        return try {
            pref.getBoolean(KEY, defValue)
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }

    fun getInt(KEY: String, defValue: Int = -1): Int {
        return try {
            pref.getInt(KEY, defValue)
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }

    fun getFloat(KEY: String, defValue: Float = -1f): Float {
        return try {
            pref.getFloat(KEY, defValue)
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }

    fun getLong(KEY: String, defValue: Long = -1): Long {
        return try {
            pref.getLong(KEY, defValue)
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }

    open fun getString(KEY: String, defValue: String? = ""): String? {
        return try {
            pref.getString(KEY, defValue) ?: defValue
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }


    /*
    * SETTER / PUTTER GENERIC
    * */

    fun putBool(KEY: String, VALUE: Boolean) {
        try {
            editor.putBoolean(KEY, VALUE)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun putInt(KEY: String, VALUE: Int) {
        try {
            editor.putInt(KEY, VALUE)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun putFloat(KEY: String, VALUE: Float) {
        try {
            editor.putFloat(KEY, VALUE)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun putLong(KEY: String, VALUE: Long) {
        try {
            editor.putLong(KEY, VALUE)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun putString(KEY: String, VALUE: String?) {
        try {
            editor.putString(KEY, VALUE)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}