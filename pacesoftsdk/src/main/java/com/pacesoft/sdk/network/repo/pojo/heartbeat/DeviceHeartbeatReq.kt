package com.pacesoft.sdk.network.repo.pojo.heartbeat


import com.google.gson.annotations.SerializedName

data class DeviceHeartbeatReq(
    @SerializedName("Action") val action: String,
    @SerializedName("HeartbeatId") val heartbeatId: String,
    @SerializedName("MerchantId") val merchantId: String,
    @SerializedName("StoreId") val storeId: String,
    @SerializedName("Effective") val effective: String,
    @SerializedName("Metadata") val metadata: Metadata,
    @SerializedName("DeviceStatus") val deviceStatus: String
) {
    data class Metadata(
        @SerializedName("Location") val location: Location,
        @SerializedName("Security") val security: Security,
        @SerializedName("TerminalInfo") val terminalInfo: TerminalInfo,
        @SerializedName("DeviceInfo") val deviceInfo: DeviceInfo
    ) {
        data class Location(
            @SerializedName("Latitude") val latitude: String,
            @SerializedName("Longitude") val longitude: String
        )

        data class Security(
            @SerializedName("Rooted") val rooted: String,
            @SerializedName("ScreenRecording") val screenRecording: String,
            @SerializedName("ActivityHijacking") val activityHijacking: String,
            @SerializedName("ClickJacking") val clickJacking: String
        )

        data class TerminalInfo(
            @SerializedName("Ip") val ip: String,
            @SerializedName("TerminalId") val terminalId: String,
            @SerializedName("TerminalName") val terminalName: String,
            @SerializedName("TerminalAppVersion") val terminalAppVersion: String,
            @SerializedName("TerminalOsVersion") val terminalOsVersion: String
        )

        data class DeviceInfo(
            @SerializedName("Ip") val ip: String,
            @SerializedName("DeviceId") val deviceId: String,
            @SerializedName("DeviceName") val deviceName: String,
            @SerializedName("AppVersion") val appVersion: String,
            @SerializedName("OsVersion") val osVersion: String
        )
    }
}


/*
{
  "Action": "",
  "HeartbeatId": "",
  "MerchantId": "",
  "StoreId": "",
  "Effective": "",
  "Metadata": {
    "OSVersion": "33",
    "Location": {
      "Latitude": "",
      "Longitude": ""
    },
    "Security": {
      "Rooted": "",
      "ScreenRecording": "",
      "ActivityHijacking": "",
      "ClickJacking": ""
    },
    "TerminalInfo": {
      "Ip": "",
      "TerminalId": "",
      "TerminalName": "",
      "TerminalAppVersion": "",
      "TerminalOsVersion": ""
    },
    "DeviceInfo": {
      "Ip": "",
      "DeviceId": "",
      "DeviceName": "",
      "AppVersion": "",
      "OsVersion": ""
    }
  },
  "DeviceStatus": ""
}
* */