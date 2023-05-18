package com.pacesoft.sdk.network.repo.service

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*
import com.pacesoft.sdk.module.PsApiReq
import com.pacesoft.sdk.module.PsApiRsp
import com.pacesoft.sdk.network.repo.pojo.heartbeat.UserHeartbeatReq
import com.pacesoft.sdk.network.repo.pojo.heartbeat.UserHeartbeatRsp

interface ApiInterface {


    @POST
    fun heartbeat(
        @HeaderMap header: Map<String, String?>,
        @Url url: String,
        @Body req: UserHeartbeatReq
    ): Observable<Response<UserHeartbeatRsp?>>


    @POST
    fun deviceHeartbeatReq(
        @HeaderMap header: Map<String, String?>,
        @Url url: String,
        @Body req: PsApiReq
    ): Observable<Response<PsApiRsp>>


    //V2 API Login
    @Headers("Content-Type: application/json")
    @POST
    fun authLogin(
        @HeaderMap header: Map<String, String?>,
        @Url url: String,
        @Body req: PsApiReq
    ): Observable<Response<PsApiRsp>>

    @Headers("Content-Type: application/json")
    @POST
    fun authValidateOtpVerification(
        @HeaderMap header: Map<String, String?>,
        @Url url: String,
        @Body req: PsApiReq
    ): Observable<Response<PsApiRsp>>

    @Headers("Content-Type: application/json")
    @POST
    fun deviceOnBoarding(
        @HeaderMap header: Map<String, String?>,
        @Url url: String,
        @Body req: PsApiReq
    ): Observable<Response<PsApiRsp>>


    @POST
    fun terminalV2Sale(
        @HeaderMap header: Map<String, String?>,
        @Url url: String,
        @Body req: PsApiReq
    ): Observable<Response<PsApiRsp>>


    @Headers("Content-Type: application/json")
    @POST
    fun merchantTransListReq(
        @HeaderMap header: Map<String, String?>,
        @Url url: String,
        @Body req: PsApiReq
    ): Observable<Response<PsApiRsp>>
}
