package com.pacesoft.sdk.network.repo

import com.pacesoft.sdk.module.SecurityHelperResult
import com.pacesoft.sdk.module.XApiRequest
import com.pacesoft.sdk.network.api.XUrl
import com.pacesoft.sdk.network.repo.pojo.auth.otpVerification.AuthLoginOtpVerificationRsp
import com.pacesoft.sdk.session.XpssInsta
import com.pacesoft.sdk.util.text.XCrypto
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import x.code.util.gson

class HeartbeatApiRepo : BaseApiRepo() {

    /*fun apiHeartbeat(
        req: HeartbeatReq,
        vm: HeartbeatVm, headerMap: HashMap<String, String?>
    ): Disposable? {
        val url = XUrl.heartbeatUrl()
        return mService.heartbeat(
            headerMap,
            url = url,
            req = req,
        )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                vm.apiDump_GeoFence(it)
            }, {
                when (it) {
                    is SocketTimeoutException -> {
                        vm.triggerSocketTimeoutException(url)
                        vm.apiDump_GeoFence(null, it)
                    }
                    else ->
                        vm.apiDump_GeoFence(null, it)
                }
            })
    }*/


    fun apiInit_4_DeviceHeartbeat(
        req: XApiRequest
    ): Disposable? {
        val url = XUrl.heartbeatUrl()
        printUrl(url)

        return mService.deviceHeartbeatReq(
            req.getHeaders(),
            url = url,
            req = req.body,
        )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                val rspBody = it.body()
                if (rspBody != null) {
                    //Convert encrypted message to AuthLoginOtpVerificationRsp
                    val result: SecurityHelperResult = XCrypto.getDecryptedData(
                        strToDecrypt = rspBody.message ?: "",
                        iv = req.crypto.iv,
                        key = req.crypto.dk,
                    )

                    //Convert json string to Object
                    val decryptedPayload: AuthLoginOtpVerificationRsp = gson.fromJson(
                        result.strJsonDecryptedData,
                        AuthLoginOtpVerificationRsp::class.java
                    )

                    XpssInsta.userPref.addPsck(decryptedPayload.cryptoPayload)
                }
            }, {
                /*
                when (it) {
                    is SocketTimeoutException -> {
                        vm.triggerSocketTimeoutException(url)
                        vm.apiDump_4_TerminalHeartbeat(null, it)
                    }
                    else ->
                        vm.apiDump_4_TerminalHeartbeat(null, it)
                }
                */
            })
    }
}