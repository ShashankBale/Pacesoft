package com.pacesoft.sdk.network.repo.pojo

import com.google.gson.annotations.SerializedName

data class PublicIPAddress(
    @SerializedName("address") val address:String
)