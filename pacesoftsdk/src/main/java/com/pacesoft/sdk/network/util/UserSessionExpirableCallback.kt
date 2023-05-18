package com.pacesoft.sdk.network.util

interface UserSessionExpirableCallback {
        fun onUserSessionOut(strSessionExpiryMsg: String, includeSessionExpiredView: Boolean = true)
        fun showServerDown(apiName: String)
    }
