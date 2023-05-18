package com.pacesoft.sdk.vm

import androidx.lifecycle.MutableLiveData
import com.pacesoft.sdk.module.PsApiRsp
import com.pacesoft.sdk.module.XApiRequest
import com.pacesoft.sdk.network.repo.AuthApiRepo
import com.pacesoft.sdk.network.repo.pojo.auth.deviceOnBoarding.DeviceOnBoardingReq
import com.pacesoft.sdk.network.repo.pojo.auth.deviceOnBoarding.DeviceOnBoardingRsp
import com.pacesoft.sdk.network.repo.pojo.auth.login.AuthLoginReq
import com.pacesoft.sdk.network.repo.pojo.auth.login.AuthLoginRsp
import com.pacesoft.sdk.network.repo.pojo.auth.otpVerification.AuthLoginOtpVerificationReq
import com.pacesoft.sdk.network.repo.pojo.auth.otpVerification.AuthLoginOtpVerificationRsp
import com.pacesoft.sdk.session.XpssInsta
import x.code.util.repo.Resource
import okhttp3.internal.toHeaderList
import retrofit2.Response
import x.code.util.number.Numb
import java.util.concurrent.TimeUnit

class AuthVm : BaseVm() {
    val mRepo = AuthApiRepo

    private fun isLockOutDurationInHeaders(rsp: Response<PsApiRsp>?) {
        val headers = rsp?.headers()?.toHeaderList()
        if (headers != null) {
            val headersMap: MutableMap<String, String> = HashMap(headers.size)
            for (header in headers) {
                headersMap[header.name.utf8().lowercase()] = header.value.utf8()
            }

            val value: String? = headersMap["lockoutduration"]
            value ?: return

            val inMinutes = Numb.parseLong(value, 0L)
            val inMillis = TimeUnit.MINUTES.toMillis(inMinutes)
            val lockTillTime = System.currentTimeMillis() + inMillis
            XpssInsta.devicePref.authBlockTillTime = lockTillTime
        }
    }

    /**Auth Login*/
    val ldAuthLogin = MutableLiveData<Resource<AuthLoginRsp?>>()
    fun apiInit_4_Login(reqBody: AuthLoginReq) {
        val (req, err) = getApiReq(reqBody)
        if (err != null) ldAuthLogin.postValue(Resource.error(err))
        else if (req != null) {
            mRepo.apiInit_4_Login(req, vm = this)
            ldAuthLogin.postValue(Resource.loading())
        }
    }

    fun apiDump_4V2_Login(
        req: XApiRequest,
        rsp: Response<PsApiRsp>?,
        eApi: Throwable? = null
    ) {
        isLockOutDurationInHeaders(rsp)
        ldAuthLogin.postValue(
            getApiRsp(
                req = req,
                rsp = rsp,
                eApi = eApi,
                bodyType = AuthLoginRsp::class.java
            ) as Resource<AuthLoginRsp>
        )
    }


    /**Auth Login OTP Verification*/

    val ldLoginOtpVerification = MutableLiveData<Resource<AuthLoginOtpVerificationRsp?>>()
    fun apiInit_4V2_LoginOtpVerification(reqBody: AuthLoginOtpVerificationReq) {
        val ld = ldLoginOtpVerification
        val (req, err) = getApiReq(reqBody)
        if (err != null) ld.postValue(Resource.error(err))
        else if (req != null) {
            mRepo.apiInit_4_LoginOtpVerification(req, vm = this)
            ld.postValue(Resource.loading())
        }
    }

    fun apiDump_4V2_LoginOtpVerification(
        req: XApiRequest,
        rsp: Response<PsApiRsp>?,
        eApi: Throwable? = null
    ) {
        isLockOutDurationInHeaders(rsp)
        ldLoginOtpVerification.postValue(
            getApiRsp(
                req = req,
                rsp = rsp,
                eApi = eApi,
                bodyType = AuthLoginOtpVerificationRsp::class.java
            ) as Resource<AuthLoginOtpVerificationRsp>
        )
    }

    val ldDeviceOnBoarding_ = MutableLiveData<Resource<DeviceOnBoardingRsp?>>()

    fun apiInit_4V2_deviceOnBoarding(req: DeviceOnBoardingReq) {
        val ld = ldDeviceOnBoarding_
        val (req, err) = getApiReq(req)
        if (err != null) ld.postValue(Resource.error(err))
        else if (req != null) {
            mRepo.apiInit_4_DeviceOnBoarding(req, vm = this)
            ld.postValue(Resource.loading())
        }
    }

    fun apiDump_4V2_deviceOnBoarding(
        req: XApiRequest,
        rsp: Response<PsApiRsp>?,
        eApi: Throwable? = null
    ) {
        ldDeviceOnBoarding_.postValue(
            getApiRsp(
                req = req,
                rsp = rsp,
                eApi = eApi,
                bodyType = DeviceOnBoardingRsp::class.java
            ) as Resource<DeviceOnBoardingRsp>
        )
    }
}