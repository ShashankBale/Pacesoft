package com.pacesoft.sdk.session

import android.content.Context
import com.pacesoft.sdk.ingenico.IngenicoSDK
import com.pacesoft.sdk.app.PaceSoftSdk
import com.pacesoft.sdk.util.device.XPDevice
import x.code.util.view.text.XStr

object XpssInsta {

    val userPref: UserPref by lazy {
        UserPref(PaceSoftSdk.ctx)
    }

    val devicePref: DevicePref by lazy {
        DevicePref(PaceSoftSdk.ctx)
    }

    val keysPref: KeyPref by lazy {
        KeyPref(PaceSoftSdk.ctx)
    }

    val context: Context by lazy { PaceSoftSdk.ctx }

    val xskb: Xskb by lazy {
        Xskb()
    }

    val ingenicoSDK: IngenicoSDK by lazy {
        IngenicoSDK()
    }

    var userId: String = ""
        private set
        get() {
            return if (field == "" && userPref.isAuthorized()) {
                onUserCreated()
                devicePref.userId ?: ""
            } else
                field
        }

    var clientId: String = ""
        private set
        get() {
            return if (field == "" && userPref.isAuthorized()) {
                onUserCreated()
                devicePref.clientId ?: ""
            } else
                field
        }

    var apiKey: String = ""
        private set
        get() {
            return if (field == "") {
                userPref.apiKey
            } else
                field
        }

    var deviceId: String = ""
        private set
        get() {
            return if (XStr.isEmpty(field)) {
                //XDevice.deviceId
                //devicePref.deviceId ?: ""
                XPDevice.getDeviceId()
            } else
                field
        }

    fun onUserCreated() {
        userId = devicePref.userId ?: ""
        clientId = devicePref.clientId ?: ""
        apiKey = userPref.apiKey ?: ""
    }

    fun onUserDestroyActivity() {
        userId = ""
        apiKey = ""
    }

    fun onUserLoggedOut() {
        onUserDestroyActivity()
        userPref.clearUserPref()
        keysPref.clearAllKeys()
    }

    fun isAuthorized() = userPref.isAuthorized()

    fun isNotAuthorized() = !userPref.isAuthorized()


}