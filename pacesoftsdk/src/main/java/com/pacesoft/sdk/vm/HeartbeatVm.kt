package com.pacesoft.sdk.vm

import com.pacesoft.sdk.network.repo.HeartbeatApiRepo

class HeartbeatVm : BaseUserVm() {
    val mRepo = HeartbeatApiRepo()


/*
    val ldHeartBeatResponse = MutableLiveData<Resource<UserHeartbeatRsp?>>()

    fun apiInit_4_CustomerGeoFence(alGeoFencing: List<UserHeartbeatReq.GeoFencingDetail>?) {
        val req = UserHeartbeatReq(
            action = ACTION_CUST_GEOFENCE,
            beaconInRange = null,
            declinePay = null,
            geoFencingDetails = alGeoFencing,
            pay = null,
            requestId = getRequestId(),
            requestPayToCustomer = null,
            transactionStatus = null,
            userTypeId = getUserId(),
            userType = USER_TYPE_CUSTOMER
        )
        val headerMap = getHeaders(true)
        mRepo.apiHeartbeat(req, this, headerMap)
        ldHeartBeatResponse.postValue(Resource.loading())
    }

    fun apiInit_4_CustomerPay(track2Data: String, cardNumber: String, expiredDate: String) {
        val saleTransRequest = getSaleTransRequest(cardNumber, expiredDate, track2Data)
        val gson = Gson()
        val saleTransReqString = gson.toJson(saleTransRequest)
        val pay = UserHeartbeatReq.Pay(
            customerId = getUserId(),
            saleRequestJSON = saleTransReqString
        )
        val heartbeatRequest = UserHeartbeatReq(
            action = ACTION_CUST_PAY,
            beaconInRange = null,
            declinePay = null,
            geoFencingDetails = null,
            pay = pay,
            requestId = getRequestId(),
            requestPayToCustomer = null,
            transactionStatus = null,
            userTypeId = getUserId(),
            userType = USER_TYPE_CUSTOMER
        )
        val headerMap = getHeaders(true)
        mRepo.apiHeartbeat(heartbeatRequest, this, headerMap)
        ldHeartBeatResponse.postValue(Resource.loading())
    }

    fun apiInit_4_CustomerDecline(reason: String) {

        val declinePay: UserHeartbeatReq.DeclinePay = UserHeartbeatReq.DeclinePay(
            customerId = getUserId(),
            reasonForDecline = reason
        )
        val req = UserHeartbeatReq(
            action = ACTION_CUST_PAY_DECLINE,
            beaconInRange = null,
            declinePay = declinePay,
            geoFencingDetails = null,
            pay = null,
            requestId = getRequestId(),
            requestPayToCustomer = null,
            transactionStatus = null,
            userTypeId = getUserId(),
            userType = USER_TYPE_CUSTOMER
        )

        val headerMap = getHeaders(true)
        mRepo.apiHeartbeat(req, this, headerMap)
        ldHeartBeatResponse.postValue(Resource.loading())
    }

    fun apiInit_4_MerchantBeaconInRange(alBeaconInRange: List<UserHeartbeatReq.BeaconInRange>) {

        val req = UserHeartbeatReq(
            action = ACTION_M_BEACON_IN_RANGE,
            beaconInRange = alBeaconInRange,
            declinePay = null,
            geoFencingDetails = null,
            pay = null,
            requestId = getRequestId(),
            requestPayToCustomer = null,
            transactionStatus = null,
            userTypeId = getUserId(),
            userType = USER_TYPE_MERCHANT
        )

        val headerMap = getHeaders(true)
        mRepo.apiHeartbeat(req, this, headerMap)
        ldHeartBeatResponse.postValue(Resource.loading())
    }


    fun apiInit_4_MerchantRequestToPay(amount: String, customerId: String) {
        val requestPayToCustomer = UserHeartbeatReq.RequestPayToCustomer(
                amount = amount,
                customerId = customerId
            )

        val req = UserHeartbeatReq(
            action = ACTION_M_CR2P,
            beaconInRange = null,
            declinePay = null,
            geoFencingDetails = null,
            pay = null,
            requestId = getRequestId(),
            requestPayToCustomer = requestPayToCustomer,
            transactionStatus = null,
            userTypeId = getUserId(),
            userType = USER_TYPE_MERCHANT
        )

        val headerMap = getHeaders(true)
        mRepo.apiHeartbeat(req, this, headerMap)
        ldHeartBeatResponse.postValue(Resource.loading())
    }


    fun apiInitMerchantTransactionStatus(customerId: String) {

        val txnStatus = UserHeartbeatReq.TransactionStatus(customerId = customerId)

        val req = UserHeartbeatReq(
            action = ACTION_M_CR2P_TXN_STATUS,
            beaconInRange = null,
            declinePay = null,
            geoFencingDetails = null,
            pay = null,
            requestId = getRequestId(),
            requestPayToCustomer = null,
            transactionStatus = txnStatus,
            userTypeId = getUserId(),
            userType = USER_TYPE_MERCHANT
        )

        val headerMap = getHeaders(true)
        mRepo.apiHeartbeat(req, this, headerMap)
        ldHeartBeatResponse.postValue(Resource.loading())
    }


    fun apiDump_GeoFence(rsp: Response<UserHeartbeatRsp?>?, eApi: Throwable? = null) {
        val resource = (getResource2(rsp, eApi) as Resource<UserHeartbeatRsp?>)
        ldHeartBeatResponse.postValue(resource)
    }


    private fun getSaleTransRequest(
        cardNumber: String,
        expiredDate: String,
        track2Data: String
    ): SaleTransReq {

        val billTo = SaleTransReq.Account.BillTo(
            firstName = "", //firstName, //number are not allowed in FN
            lastName = "", //lastName, //number are not allowed in LN
            address = SaleTransReq.Address(),
            phoneNumber = "",
            emailAddress = ""
        )

        val account = SaleTransReq.Account(
            billTo = billTo,
            pan = cardNumber,
            expirationDate = expiredDate,
            cvv = null,
            trackData = track2Data,
            pinBlock = null,
            par = null,
            version = null,
            token = null,
            batchId = null,
            emvData = null
        )

        val saleTxnReq = SaleTransReq(
            currencyCode = "USD",
            amount = XInsta.getTerminalCartAmountSum(),
            tipAmount = 0,
            shipTo = SaleTransReq.ShipTo(),
            account = account,
            descriptor = null,
            type = 4,
            accounttoken = null
        )

        return saleTxnReq

    }*/

}