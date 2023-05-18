package com.pacesoft.sdk.network.repo.pojo.auth.otpVerification


import com.google.gson.annotations.SerializedName

data class AuthLoginOtpVerificationReq(
   /* @SerializedName("mobileNumber") val phoneNumber: String,
    @SerializedName("serialNumber") val serialNumber: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("intermediaryToken") val intermediaryToken: String*/
    @SerializedName("IsMobileInterface") val isMobileInterface: Boolean,
    @SerializedName("UserId") val phoneNumber: String,
    @SerializedName("DeviceId") val deviceNumber: String,
    @SerializedName("Otp") val otp: String

)

/*
{
  "mobileNumber": 0,
  "serialNumber": "string",
  "otp": 0,
  "intermediaryToken": "string"
}

{
"PhoneNumber":"string" required,
"DeviceId":"string",
"Otp":"string" required
}
}
* */