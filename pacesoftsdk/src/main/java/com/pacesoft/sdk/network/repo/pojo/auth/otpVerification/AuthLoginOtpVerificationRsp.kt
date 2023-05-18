package com.pacesoft.sdk.network.repo.pojo.auth.otpVerification

import com.google.gson.annotations.SerializedName
import com.pacesoft.sdk.module.BasePsApiRspBody

class AuthLoginOtpVerificationRsp : BasePsApiRspBody() {
    @SerializedName("IsClient")
    val isClient: Boolean? = null

    @SerializedName("DeviceId")
    val deviceId: String? = null

    @SerializedName("Status")
    val status: Boolean? = null

    @SerializedName("ClientDetails")
    val clientDetails: ClientDetails? = null

    data class ClientDetails(
        //@SerializedName("RoleId") val roleId: Int? = null,
        @SerializedName("ClientId") val clientId: Long? = null,
        @SerializedName("ClientName") val clientName: String? = null,
        @SerializedName("ClientEmail") val clientEmail: String? = null,
        //@SerializedName("ClientPhonenumber") val clientPhoneNumber: Any? = null,
        //@SerializedName("TerminalID") val terminalID: Any? = null,
        //@SerializedName("TerminalIP") val terminalIP: Any? = null,
        //@SerializedName("TerminalName") val terminalName: Any? = null,
        //@SerializedName("TerminalAppVersion") val terminalAppVersion: Any? = null,
        //@SerializedName("TerminalOsVersion") val terminalOsVersion: Any? = null
    )
}


/*
{
  "IsClient": true,
  "DeviceId": "GLS31019192",
  "Crypto": {
    "IV": "o10w5dUoVPHrAcMVa/Fk9g==",
    "DK": "F/vPod1e5o4nOoPbBHVYoegM7a3//Wiz0uJh1gc1LjU=",
    "TK": "3ImeeU3WOeD2wL/MmEykeY7mRBo7GFM33WrN5lIns+g=",
    "ApiKey": "64dd0134-c912-484b-b230-12db308f2fed"
  },
  "Status": true,
  "ClientDetails": {
    "RoleId": 0,
    "ClientId": 1376,
    "ClientName": "Bob's Coffee Shop",
    "ClientEmail": "bob.tornato.pacesoft@mailinator.com",
    "ClientPhonenumber": null,
    "TerminalID": null,
    "TerminalIP": null,
    "TerminalName": null,
    "TerminalAppVersion": null,
    "TerminalOsVersion": null
  }
}
* */