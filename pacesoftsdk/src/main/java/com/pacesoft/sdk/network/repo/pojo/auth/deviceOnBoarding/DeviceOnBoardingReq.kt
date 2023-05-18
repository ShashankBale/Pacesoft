package com.pacesoft.sdk.network.repo.pojo.auth.deviceOnBoarding


import com.google.gson.annotations.SerializedName

data class DeviceOnBoardingReq(
    @SerializedName("DeviceId") val deviceId: String,
    @SerializedName("MacAddress") val macAddress: String,
    @SerializedName("Manufacturer") val manufacturer: String,
    @SerializedName("Model") val model: String,
    @SerializedName("OSVersion") val oSVersion: String,
    @SerializedName("PhoneNumber") val phoneNumber: String,
    @SerializedName("SerialNumber") val serialNumber: String,
    @SerializedName("IpAddress") val ipAddress: String,
    @SerializedName("DeviceName") val deviceName: String,
    @SerializedName("AppVersion") val appVersion: String,
)


/*
{
  "DeviceId": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAESC6U//u1nFAkJXMAQAk/bQ4DfX2zKLMrr3Uwvkakf2ruMeJTvtP+K0OEqI/XT0iFLP8uhEEvAkhti929Kig+Cg==",
  "MacAddress": "00:00:00:00:00",
  "Manufacturer": "samsung",
  "Model": "SM-G990U",
  "OSVersion": "33",
  "PhoneNumber": "+918369274899",
  "SerialNumber": "12345",
  "IpAddress": "1.1.1.1",
  "DeviceName": "SM-G990U",
  "AppVersion": "1.0.0.230221"
}
* */