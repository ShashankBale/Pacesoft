package com.pacesoft.sdk.network.repo.pojo.heartbeat


import com.google.gson.annotations.SerializedName
import com.pacesoft.sdk.module.CryptoPayload

data class HeartbeatRsp(
    @SerializedName("Status")
    val status: String?,
    @SerializedName("Crypto")
    val crypto:CryptoPayload
)