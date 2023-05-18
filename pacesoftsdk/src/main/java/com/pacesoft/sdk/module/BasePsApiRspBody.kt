package com.pacesoft.sdk.module

import com.google.gson.annotations.SerializedName

open class BasePsApiRspBody(
    @SerializedName("Crypto") val cryptoPayload: CryptoPayload? = null,
)
