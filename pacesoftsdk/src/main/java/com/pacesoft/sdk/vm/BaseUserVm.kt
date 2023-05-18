package com.pacesoft.sdk.vm

import com.pacesoft.sdk.session.XpssInsta

open class BaseUserVm : BaseVm() {
    protected val USER_TYPE_CUSTOMER: String = "Customer"
    protected val USER_TYPE_MERCHANT = "Merchant"

    protected val ACTION_CUST_GEOFENCE = "GeoFencingDetails"
    protected val ACTION_CUST_PAY = "Pay"
    protected val ACTION_CUST_PAY_DECLINE = "DeclinePay"

    protected val ACTION_M_BEACON_IN_RANGE = "BeaconinRange"
    protected val ACTION_M_CR2P = "RequestPayToCustomer"
    protected val ACTION_M_CR2P_TXN_STATUS = "TransactionStatus"


    protected fun getUserId(): String {
        return XpssInsta.userId
    }

    protected fun getRequestId(): String {
        val userId = XpssInsta.userId
        return if (userId.trim().isNotEmpty())
            userId.substring((userId.length.minus(3)), userId.length) + System.currentTimeMillis()
        else
            System.currentTimeMillis().toString()
    }
}
