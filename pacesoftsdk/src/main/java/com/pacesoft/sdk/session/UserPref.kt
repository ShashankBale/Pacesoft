package com.pacesoft.sdk.session

import android.content.Context
import com.pacesoft.sdk.module.CryptoPayload
import x.code.session._BasePref
import x.code.util.view.text.XStr

class UserPref(context: Context) : _BasePref(context, "pref_user") {

    fun clearUserPref() {
        clearAll()
    }

    private val _isLoggedIn = "_isLoggedIn"
    var isLoggedIn: Boolean
        get() = getBool(_isLoggedIn)
        private set(value) = putBool(_isLoggedIn, value)

    private val _clientId = "_clientId"
    var clientId: Long
        get() = getLong(_clientId)
        private set(value) = putLong(_clientId, value)

    private val _clientName = "_clientName"
    var clientName: String
        get() = getString(_clientName, "") ?: ""
        set(result) = putString(_clientName, result)

    private val _clientEmail = "_clientEmail"
    var clientEmail: String
        get() = getString(_clientEmail, "") ?: ""
        set(result) = putString(_clientEmail, result)

    private val _apiKey = "_apiKey"
    var apiKey: String
        get() = getString(_apiKey, null) ?: XpssInsta.keysPref.getDefaultApiKey()
        set(result) = putString(_apiKey, result)

    fun addPsck(cp: CryptoPayload?) {
        cp ?: return
        XpssInsta.keysPref.addPsck(cp)

        if (XStr.isNotEmpty(cp.apiKey))
            putString(_apiKey, cp.apiKey)
    }


    private val _isMerchant = "_isMerchant"
    var isMerchant: Boolean
        get() = getBool(_isMerchant, false)
        set(result) = putBool(_isMerchant, result)

    fun createUserLoginSession(
        pUserId: String,
        pClientId: Long,
        pClientName: String,
        pClientEmail: String,
    ) {
        try {
            this.isLoggedIn = true
            XpssInsta.devicePref.userId = pUserId
            clientId = pClientId
            clientName = pClientName
            clientEmail = pClientEmail
            //this.sessionId = pSessionId
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isAuthorized(): Boolean {
        try {
            if (isLoggedIn
                //&& lastLoginDate == AppDateUtil.getCurrentLoginFormat()
                && XStr.isNotEmpty(XpssInsta.devicePref.userId)
            //&& XStr.isNotEmpty(sessionId)
            ) return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}