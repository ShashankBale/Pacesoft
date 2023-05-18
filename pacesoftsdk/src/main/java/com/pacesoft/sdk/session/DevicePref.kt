package com.pacesoft.sdk.session

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pacesoft.sdk.network.api.XUrl
import com.pacesoft.sdk.network.repo.pojo.user.XUser
import x.code.session._BasePref
import x.code.util.repo.XServerType
import java.lang.reflect.Type

class DevicePref(context: Context) : _BasePref(context, "pref_device") {


    private val _userId = "_userId"
    var userId: String?
        get() = getString(_userId)
        set(result) = putString(_userId, result)


    private val _clientId = "_clientId"
    var clientId: String?
        get() = getString(_clientId)
        set(result) = putString(_clientId, result)


    private val _serverTypeProd = "_serverTypeProd"
    var serverTypeProd: XServerType
        get() = XServerType.getEnum(getInt(_serverTypeProd, XServerType.PROD_SERVER.index))
        set(pEnum) = putInt(_serverTypeProd, pEnum.index)


    fun getMainBaseUrl() = XUrl.mainBaseUrl

    private val _ipAddress = "_ipAddress"
    var ipAddress: String?
        get() = getString(_ipAddress, "###.###.###.###")
        set(result) = putString(_ipAddress, result)

    /*
    private val _deviceId = "_deviceId"
    var deviceId: String?
        get() {
            val oldValue = getString(_deviceId, null)
            return if (oldValue == null) {
                val newValue = XPDevice.getDeviceId()
                putString(_deviceId, newValue)
                newValue
            } else {
                oldValue
            }
        }
        private set(result) = putString(_deviceId, result)
    */

    private val _userIdClientId = "_userIdClientId"
    var userIdClientId: HashMap<String, String>
        get() {
            val strJson = getString(_userIdClientId)
            val type: Type = object : TypeToken<HashMap<String, String?>?>() {}.type
            return Gson().fromJson(strJson, type) ?: hashMapOf()
        }
        set(input) {
            val strJson = Gson().toJson(input)
            putString(_userIdClientId, strJson)
        }


    private val _themeMode = "_themeMode"
    var themeMode: x.code.util.view.XTheme.Mode
        get() = x.code.util.view.XTheme.Mode.getEnum(
            getInt(
                _themeMode,
                x.code.util.view.XTheme.Mode.Default.num
            )
        )
        set(result) = putInt(_themeMode, (result).num)

    private val _fcmToken = "_fcmToken"
    var fcmToken: String?
        get() = getString(_fcmToken, "")
        set(result) = putString(_fcmToken, result)

    private val _xuser = "_xuser"
    var xuser: HashMap<String, XUser>?
        get() {
            val strJson = getString(_xuser)
            val type: Type = object : TypeToken<HashMap<String, XUser?>?>() {}.type
            return Gson().fromJson(strJson, type)
        }
        set(input) {
            val strJson = Gson().toJson(input)
            putString(_xuser, strJson)
        }


    fun getXUser(id: String): XUser? {
        val xuser = xuser?.get(id)
        return xuser
    }

    fun putXUser(id: String, item: XUser) {
        val temp: HashMap<String, XUser> = xuser ?: hashMapOf()
        temp[id] = item
        xuser = temp
    }


    /*Heartbeat variables*/
    private val _hbReferenceID = "_hbReferenceID"
    var hbReferenceId: Long
        get() = getLong(_hbReferenceID, 1L)
        set(result) = putLong(_hbReferenceID, result)

    private val _hbLastTs = "_hbLastTs"
    var hbLastTs: Long
        get() = getLong(_hbLastTs, 0L)
        set(result) = putLong(_hbLastTs, result)

    private val _hbRefreshIntervalTimer = "_hbRefreshIntervalTimer"
    var hbRefreshIntervalTimer: Long
        get() = getLong(_hbRefreshIntervalTimer, 3600_000L) //3600000=1hour
        set(result) = putLong(_hbRefreshIntervalTimer, result)

    //inMillis
    private val _authBlockTillTime = "_authBlockTillTime"
    var authBlockTillTime: Long
        get() = getLong(_authBlockTillTime, 0)
        set(result) = putLong(_authBlockTillTime, result)

}
