package com.pacesoft.sdk.network.repo.pojo

import com.google.gson.annotations.SerializedName

open class BaseApiResponse<out T>(
    @SerializedName("responseCode") val status: String? = null,
    @SerializedName("payload") val payload: T? = null,
    @SerializedName("responseText") val reason: String? = null
)
