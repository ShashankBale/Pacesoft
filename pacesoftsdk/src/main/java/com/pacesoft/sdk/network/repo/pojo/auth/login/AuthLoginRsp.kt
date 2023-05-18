package com.pacesoft.sdk.network.repo.pojo.auth.login

import com.google.gson.annotations.SerializedName
import com.pacesoft.sdk.module.BasePsApiRspBody

class AuthLoginRsp : BasePsApiRspBody() {
    @SerializedName("Status")
    val status: Boolean? = null
}