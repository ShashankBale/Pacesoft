package com.pacesoft.sdk.network.repo

import com.pacesoft.sdk.network.api.XApi
import com.pacesoft.sdk.network.repo.service.ApiInterface
import x.code.util.log.elog

open class BaseApiRepo {
    var mService: ApiInterface = XApi.getApiServiceInterface()

    fun printUrl(strUrl : String) {
        elog("PACESOFT_API_URL", strUrl)
    }
}