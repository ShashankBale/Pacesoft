package com.pacesoft.sdk.network.repo

import com.pacesoft.sdk.module.XApiRequest
import com.pacesoft.sdk.network.api.XUrl
import com.pacesoft.sdk.vm.AuthVm
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

object AuthApiRepo : BaseApiRepo() {

    fun apiInit_4_Login(
        req: XApiRequest,
        vm: AuthVm
    ): Disposable? {
        val url = XUrl.loginUrl()
        printUrl(url)
        return mService.authLogin(
            header = req.getHeaders(),
            url = url,
            req = req.body,
        )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                //send data to ViewModel object
                vm.apiDump_4V2_Login(req, it)
            }, {
                when (it) {
                    is SocketTimeoutException -> {
                        vm.triggerSocketTimeoutException(url)
                        vm.apiDump_4V2_Login(req, null, it)
                    }
                    else ->
                        vm.apiDump_4V2_Login(req, null, it)
                }
            })
    }

    fun apiInit_4_LoginOtpVerification(
        req: XApiRequest,
        vm: AuthVm,
    ): Disposable? {
        val url = XUrl.verifyOtpUrl()
        printUrl(url)
        return mService.authValidateOtpVerification(
            header = req.getHeaders(),
            url = url,
            req = req.body,
        )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                vm.apiDump_4V2_LoginOtpVerification(req, it)
            }, {
                when (it) {
                    is SocketTimeoutException -> {
                        vm.triggerSocketTimeoutException(url)
                        vm.apiDump_4V2_LoginOtpVerification(req, null, it)
                    }
                    else ->
                        vm.apiDump_4V2_LoginOtpVerification(req, null, it)
                }
            })
    }


    fun apiInit_4_DeviceOnBoarding(
        req: XApiRequest,
        vm: AuthVm,
    ): Disposable? {
        val url = XUrl.deviceOnBoardingUrl()
        printUrl(url)

        return mService.deviceOnBoarding(
            header = req.getHeaders(),
            url = url,
            req = req.body,
        )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                vm.apiDump_4V2_deviceOnBoarding(req, it)
            }, {
                when (it) {
                    is SocketTimeoutException -> {
                        vm.triggerSocketTimeoutException(url)
                        vm.apiDump_4V2_deviceOnBoarding(req, null, it)
                    }
                    else ->
                        vm.apiDump_4V2_deviceOnBoarding(req, null, it)
                }
            })
    }

}