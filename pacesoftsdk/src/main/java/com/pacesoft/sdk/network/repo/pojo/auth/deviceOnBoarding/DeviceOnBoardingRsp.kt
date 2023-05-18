package com.pacesoft.sdk.network.repo.pojo.auth.deviceOnBoarding

import com.google.gson.annotations.SerializedName
import com.pacesoft.sdk.module.BasePsApiRspBody

class DeviceOnBoardingRsp : BasePsApiRspBody() {
    @SerializedName("ClientId")
    val clientId: String? = null
}