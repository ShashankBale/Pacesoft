package com.pacesoft.sdk.network.api

import com.pacesoft.sdk.session.XpssInsta
import x.code.util.XBuild

object XUrl {
    private var serverType = x.code.util.repo.XServerType.PROD_SERVER

    /** URL BASE POINT */
    var mainBaseUrl = ""
    private set

    private var saleTxnBaseUrl = ""
    private var websiteBaseUrl = ""


    init {
        initBaseUrl()
    }

    fun setServerType(serverType: x.code.util.repo.XServerType) {
        XUrl.serverType = serverType
        XpssInsta.onUserLoggedOut()
        initBaseUrl()
    }

    private fun initBaseUrl() {
        //In case if someone forget to change the serverType=PROD and release the app on PlayStore.
        val serverType = if (!XBuild.isInternalTesting())
            x.code.util.repo.XServerType.PROD_SERVER
        else
            XpssInsta.devicePref.serverTypeProd

        when (serverType) {
            x.code.util.repo.XServerType.DEV_SERVER -> {
                mainBaseUrl = "https://api-dev.pacegateway.com/"
                saleTxnBaseUrl = "https://api-dev.pacegateway.com/"
                websiteBaseUrl = "https://pacesoft.net/"
            }
            x.code.util.repo.XServerType.QA_SERVER -> {
                mainBaseUrl = "https://api-qa.pacegateway.com/"
                saleTxnBaseUrl = "https://api-qa.pacegateway.com/"
                websiteBaseUrl = "https://pacesoft.net/"
            }
            x.code.util.repo.XServerType.UAT_SERVER -> {
                mainBaseUrl = "https://api-uat.pacegateway.com/"
                saleTxnBaseUrl = "https://api-uat.pacegateway.com/"
                websiteBaseUrl = "https://pacesoft.net/"
            }
            x.code.util.repo.XServerType.PROD_SERVER -> {
                mainBaseUrl = "https://api.pacegateway.com/"
                saleTxnBaseUrl = "https://api.pacegateway.com/"
                websiteBaseUrl = "https://pacesoft.net/"
            }
        }
    }

    /*Auth*/
    private const val sendOtpUep: String = "pro/login/getotp"//"login"
    private const val verifyOtpUep: String = "pro/login/validateotp"//"validate"
    private const val userProfileUep: String = "api/user/profile"
    private const val deviceOnBoardingUep: String = "pro/device/boarding"

    /*Common*/
    private const val messageUep: String = "api/user/message"

    /*Customer*/


    /*Merchant*/
    private const val salePaymentUep: String = "trn/transaction/Sale" //"api/terminal/sale"
    private const val merchantTxns: String = "rep/transactions/Summary"
    private const val merchantTxnDetail: String = "rep/transactions/GetAll"
    private const val heartbeat: String = "pro/Heartbeat"

    fun messageUrl(): String = mainBaseUrl + messageUep
    fun loginUrl(): String = mainBaseUrl + sendOtpUep
    fun verifyOtpUrl(): String = mainBaseUrl + verifyOtpUep
    fun userProfileUrl(): String = mainBaseUrl + userProfileUep
    fun salePaymentUrl(): String = saleTxnBaseUrl + salePaymentUep
    fun merchantTransUrl(): String = saleTxnBaseUrl + merchantTxns
    fun merchantTransDetailUrl(): String = saleTxnBaseUrl + merchantTxnDetail
    fun heartbeatUrl(): String = mainBaseUrl + heartbeat
    fun deviceOnBoardingUrl(): String = mainBaseUrl + deviceOnBoardingUep


    /*Urls*/
    fun tncUrl() = websiteBaseUrl + "terms-of-use/"
    fun privacyPolicyUrl() = websiteBaseUrl + "privacy-policy/"
    fun getHost() = "dev.pacegateway.com"
}

/*
- Version on top
- Api error and app error
- Terminal Configuration
- Report : Payment method name -> Instrument type
- Report : everything is coming except Card issuer name.

* */