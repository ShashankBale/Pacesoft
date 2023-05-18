package com.pacesoft.sdk.ingenico

import com.usdk.apiservice.aidl.constants.RFDeviceName
import com.usdk.apiservice.aidl.pinpad.DeviceName

object IngenicoDeviceConfig {

    val TAG: String = "IngenicoDeviceConfig"
    var PINPAD_DEVICE_NAME = DeviceName.IPP
    var RF_DEVICE_NAME = RFDeviceName.INNER
    var REGION_ID = 0
    var KAP_NUM = 0

    /** Card search timeout  */
    var TIMEOUT = 20
    const val KEYID_PIN = 10
}