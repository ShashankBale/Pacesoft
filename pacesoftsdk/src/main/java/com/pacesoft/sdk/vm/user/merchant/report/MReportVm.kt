package com.pacesoft.sdk.vm.user.merchant.report

import androidx.lifecycle.MutableLiveData
import com.pacesoft.sdk.module.PsApiRsp
import com.pacesoft.sdk.module.XApiRequest
import com.pacesoft.sdk.network.repo.UserMerchantApiRepo
import com.pacesoft.sdk.network.repo.pojo.auth.merchantTransaction.MerchantTxnReq
import com.pacesoft.sdk.network.repo.pojo.auth.merchantTransaction.MerchantTxnRsp
import com.pacesoft.sdk.network.repo.pojo.auth.merchantTransaction.MerchantTxnSummaryRsp
import com.pacesoft.sdk.vm.BaseVm
import x.code.util.repo.Resource
import retrofit2.Response

class MReportVm : BaseVm() {
    val mRepo = UserMerchantApiRepo

    /**Merchant Trans List*/
    val ldMerchantTransList =
        MutableLiveData<Resource<Pair<MerchantTxnReq, MerchantTxnSummaryRsp?>>>()

    fun apiInit_4_MerchantTrasactionList(reqOg: MerchantTxnReq) {
        val ld = ldMerchantTransList
        val (req, err) = getApiReq(reqOg)
        if (err != null) ld.postValue(Resource.error(err))
        else if (req != null) {
            mRepo.apiInit_4_MerchantTrasactionList(req = req, vm = this)
            ld.postValue(Resource.loading())
        }
    }

    fun apiDump_4_MerchantTrasactionList(
        req: XApiRequest,
        rsp: Response<PsApiRsp>?,
        eApi: Throwable? = null
    ) {
        val resOg = getApiRsp(
            req = req,
            rsp = rsp,
            eApi = eApi,
            bodyType = MerchantTxnSummaryRsp::class.java
        )
        val data: Pair<MerchantTxnReq, MerchantTxnSummaryRsp?> =
            Pair(req.bodyOg as MerchantTxnReq, resOg.data as MerchantTxnSummaryRsp?)
        val resource = Resource.success(data)
        ldMerchantTransList.postValue(resource)
    }


    val ldMerchantTransDetail = MutableLiveData<Resource<MerchantTxnRsp?>>()
    fun apiInit_4_MerchantTrasactionDetail(reqBody: MerchantTxnReq) {
        val ld = ldMerchantTransDetail
        val (req, err) = getApiReq(reqBody)
        if (err != null) ld.postValue(Resource.error(err))
        else if (req != null) {
            mRepo.apiInit_4_MerchantTrasactionDetail(req, vm = this)
            ld.postValue(Resource.loading())
        }
    }

    fun apiDump_4_MerchantTrasactionDetail(
        req: XApiRequest,
        rsp: Response<PsApiRsp>?,
        eApi: Throwable? = null
    ) {
        ldMerchantTransDetail.postValue(
            getApiRsp(
                req = req,
                rsp = rsp,
                eApi = eApi,
                bodyType = MerchantTxnRsp::class.java
            ) as Resource<MerchantTxnRsp>
        )
    }
}