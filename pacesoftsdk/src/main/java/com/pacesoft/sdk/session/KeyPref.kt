package com.pacesoft.sdk.session;

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pacesoft.sdk.module.CryptoPayload
import com.pacesoft.sdk.module.Cryptos
import com.pacesoft.sdk.util.XPConst
import com.pacesoft.sdk.util.XPConst.maxPreventKeySize
import x.code.session._BasePref
import x.code.util.log.delog
import x.code.util.view.text.XStr
import java.lang.reflect.Type

class KeyPref(context: Context) : _BasePref(context, "pref_key") {

    private fun getEncrypted(plain: String): String {
        //return plain
        return XpssInsta.xskb.encryptText(plain) ?: ""
    }

    private fun getDecrypt(text: String?): String? {
        //return text
        text ?: return null
        return XpssInsta.xskb.decryptText(text)
    }


    override fun getString(key: String, defValue: String?): String? {
        return try {
            val strEnc = super.getString(key, defValue)
            delog("KeyPref#getString#Enc $key", defValue ?: "#NULL#")

            val plain = getDecrypt(strEnc)
            delog("KeyPref#getString#Pln $key", plain ?: "#NULL#")

            return plain
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }

    override fun putString(key: String, value: String?) {
        try {
            delog("KeyPref#putString#Pln $key", value ?: "#NULL#")
            val strEnc = getEncrypted(value ?: "")

            delog("KeyPref#putString#Enc $key", strEnc)
            editor.putString(key, strEnc)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getDefaultPacesoftKeys() = Cryptos(
        iv = getDefaultIv(), //BuildConfig.iv_1,
        dk = getDefaultDk(), //BuildConfig.dk_1,
        tk = getDefaultTk(), //BuildConfig.tk_1,
    )

    private external fun getDefaultIv(): String
    private external fun getDefaultDk(): String
    private external fun getDefaultTk(): String
    external fun getDefaultApiKey(): String
    private external fun clearAmadisKey(context: Context)
    private external fun validAmadisKey(context: Context): Boolean

    private val _pacesoftCryptoKeys = "_psk"

    private fun setPscks(items: List<Cryptos>) {
        putString(_pacesoftCryptoKeys, Gson().toJson(items))
    }

    private fun getPscksRaw(): ArrayList<Cryptos> {
        val alItem = ArrayList<Cryptos>()
        val strPsckJson = getString(_pacesoftCryptoKeys, null)
        if (XStr.isEmpty(strPsckJson)) return arrayListOf() //return empty array

        val type: Type = object : TypeToken<List<Cryptos>?>() {}.type
        alItem.addAll(Gson().fromJson(strPsckJson, type))
        return alItem
    }

    //get specify key from IV
    fun getPsck(iv: String): Cryptos? {
        return try {
            val alItem = getPscksRaw()
            if (alItem.isEmpty())
                alItem.add(getDefaultPacesoftKeys())

            val firstOrNull = alItem.firstOrNull { it.iv == iv }
            firstOrNull
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    /*private fun getPsck(): List<Cryptos> {
        return try {
            val alItem = getPscksRaw()
            if (alItem.isNullOrEmpty()) return emptyList()

            val t1 = Calendar.getInstance().timeInMillis

            val alFilter = alItem.filter {
                val t2 = it.cts
                XCal.getDayDifference(t1, t2) <= XConst.MAX_CRYPTO_KEYS_DAYS_OF_EXPIRY
            }

            if (alItem.size != alFilter.size) //resetting to preference
                setPscks(alFilter)

            alFilter
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }*/

    private fun getOrDefPsck(): ArrayList<Cryptos> {
        val alItem = getPscksRaw()
        if (alItem.isEmpty()) alItem.add(getDefaultPacesoftKeys())
        return alItem
    }

    fun getLatestPsck(): Cryptos {
        val alItem = getOrDefPsck()
        return alItem[alItem.lastIndex]
    }

    fun addPsck(pItem: CryptoPayload?) {
        val item = Cryptos.convert(pItem) ?: return
        val alItems = getOrDefPsck()
        alItems.add(item) //add new key to last index

        //Clearing old keys
        if (alItems.size > maxPreventKeySize)
            alItems.subList(0, alItems.lastIndex - maxPreventKeySize + 1).clear()

        //Save to Local Storage
        setPscks(alItems)
    }

    //TOD0 : NEED TO FIX THIS
    fun removeLatestPsck() {
        try {
            val alItem = getPscksRaw()
            if (alItem.isEmpty()) return
            alItem.removeAt(alItem.lastIndex)

            //Save to Local Storage
            setPscks(alItem)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cleanPacesoftCryptoKeys() {
        try {
            val rsp = getString(_pacesoftCryptoKeys, null)
            val type: Type = object : TypeToken<List<Cryptos>?>() {}.type
            val alItem: ArrayList<Cryptos>? = Gson().fromJson(rsp, type)
            if (alItem.isNullOrEmpty()) return
            if (alItem.size < XPConst.maxPreventKeySize) return
            alItem.subList(0, alItem.lastIndex - XPConst.maxPreventKeySize + 1).clear()
            setPscks(alItem)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearAllPacesoftCryptoKeys() {
        setPscks(listOf())
    }


    /*******************************************
     ********************************************
     *************** Amadis Keys ****************
     ********************************************
     *******************************************/

    fun isValidAmadisKey(): Boolean {
        return validAmadisKey(XpssInsta.context)
    }

    fun clearAllKeys() {
        clearAllPacesoftCryptoKeys()
        clearAmadisKey(XpssInsta.context)
    }
}