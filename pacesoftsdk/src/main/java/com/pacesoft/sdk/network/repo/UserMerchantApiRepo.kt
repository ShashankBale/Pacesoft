package com.pacesoft.sdk.network.repo

import com.pacesoft.sdk.module.XApiRequest
import com.pacesoft.sdk.network.api.XUrl
import com.pacesoft.sdk.vm.user.merchant.report.MReportVm
import com.pacesoft.sdk.vm.user.merchant.terminal.MTerminalVm
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

object UserMerchantApiRepo : BaseApiRepo() {

    fun apiInit_4_MerchantTrasactionList(
        //req: MerchantTransListReq,
        req: XApiRequest,
        //startDate : Long,
        //endDate : Long,
        vm: MReportVm,
        //headerMap: HashMap<String, String?>
    ): Disposable? {
        val url = XUrl.merchantTransUrl()
        printUrl(url)

        //createdAt>=2022-08-05,createdAt<=2022-08-11
        /*  var dateRange = "createdAt>=#STARTDATE#,createdAt<=#ENDDATE#"
          dateRange = dateRange.replace("#STARTDATE#", XCal.convert2String(XCal.fd1, startDate))
          dateRange = dateRange.replace("#ENDDATE#", XCal.convert2String(XCal.fd1, endDate))

          //"createdAt%3E%3D2022-08-05%2CcreatedAt%3C%3D2022-08-11"
          val urlEncDateRange = URLEncoder.encode(dateRange, "UTF-8")
          */
        return mService.merchantTransListReq(
            /* headerMap = headerMap,
             url = url,
             sortOrder = -1,
             take = 20,
             sortColumn = "openAt",
             term = urlEncDateRange,
             searchText = null
             //req = req,
         */
            header = req.getHeaders(),
            url = url,
            req = req.body,
        )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                vm.apiDump_4_MerchantTrasactionList(req, it)
            }, {
                when (it) {
                    is SocketTimeoutException -> {
                        vm.triggerSocketTimeoutException(url)
                        vm.apiDump_4_MerchantTrasactionList(req, null, it)
                    }
                    else ->
                        vm.apiDump_4_MerchantTrasactionList(req, null, it)
                }
            })
    }


    fun apiInit_4_MerchantTrasactionDetail(
        req: XApiRequest,
        //startDate : Long,
        //endDate : Long,
        vm: MReportVm,
        //headerMap: HashMap<String, String?>
    ): Disposable? {
        val url = XUrl.merchantTransDetailUrl()
        printUrl(url)

        //createdAt>=2022-08-05,createdAt<=2022-08-11
        /*  var dateRange = "createdAt>=#STARTDATE#,createdAt<=#ENDDATE#"
          dateRange = dateRange.replace("#STARTDATE#", XCal.convert2String(XCal.fd1, startDate))
          dateRange = dateRange.replace("#ENDDATE#", XCal.convert2String(XCal.fd1, endDate))

          //"createdAt%3E%3D2022-08-05%2CcreatedAt%3C%3D2022-08-11"
          val urlEncDateRange = URLEncoder.encode(dateRange, "UTF-8")
          */
        return mService.merchantTransListReq(
            /* headerMap = headerMap,
             url = url,
             sortOrder = -1,
             take = 20,
             sortColumn = "openAt",
             term = urlEncDateRange,
             searchText = null
             //req = req,
         */
            header = req.getHeaders(),
            url = url,
            req = req.body,
        )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                vm.apiDump_4_MerchantTrasactionDetail(req, it)
            }, {
                when (it) {
                    is SocketTimeoutException -> {
                        vm.triggerSocketTimeoutException(url)
                        vm.apiDump_4_MerchantTrasactionDetail(req, null, it)
                    }
                    else ->
                        vm.apiDump_4_MerchantTrasactionDetail(req, null, it)
                }
            })
    }

    fun apiInit_4V2_Sale(
        req: XApiRequest,
        vm: MTerminalVm,
    ): Disposable? {
        val url = XUrl.salePaymentUrl()
        printUrl(url)
        return mService.terminalV2Sale(
            header = req.getHeaders(),
            url = url,
            req = req.body,
        )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                vm.apiDump_4V2_Sale(req, it)
            }, {
                when (it) {
                    is SocketTimeoutException -> {
                        vm.triggerSocketTimeoutException(url)
                        vm.apiDump_4V2_Sale(req, null, it)
                    }
                    else ->
                        vm.apiDump_4V2_Sale(req, null, it)
                }
            })
    }
}

