package com.pacesoft.sdk.network.repo.pojo.auth.login


import com.google.gson.annotations.SerializedName

/*data class AuthLoginReq(
    @SerializedName("mobileNumber") val phoneNumber: String,
    @SerializedName("serialNumber") val serialNumber: String,
)*/

data class AuthLoginReq(
    @SerializedName("UserId") val phoneNumber: String,
    @SerializedName("DeviceId") val deviceId: String
)

/*
{
  "mobileNumber": "919664282122",
  "serialNumber": "TEST123"
}
* */