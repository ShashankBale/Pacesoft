package com.pacesoft.sdk.network.heartbeat

import com.pacesoft.sdk.network.repo.HeartbeatApiRepo
import com.pacesoft.sdk.network.repo.pojo.heartbeat.DeviceHeartbeatReq
import com.pacesoft.sdk.session.XpssInsta
import com.pacesoft.sdk.session.Xskb
import com.pacesoft.sdk.vm.BaseVm
import x.code.util.device.XDevice
import x.code.util.log.elog

class XHeartbeatApi : BaseVm() {

    val mRepo = HeartbeatApiRepo()

    fun getReqObj(): DeviceHeartbeatReq? {
        if (Xskb.isXsbkInit == false) return null

        var reqId = XpssInsta.devicePref.hbReferenceId
        if (reqId == 999999999L) {
            XpssInsta.devicePref.hbReferenceId = 0L
        }
        reqId += 1
        val hbReqId = String.format("%09d", reqId);

        val location = DeviceHeartbeatReq.Metadata.Location("0.0", "0.0")

        val security = DeviceHeartbeatReq.Metadata.Security(
            rooted = "NO",
            screenRecording = "No",
            activityHijacking = "No",
            clickJacking = "No"
        )

        val terminalInfo = DeviceHeartbeatReq.Metadata.TerminalInfo(
            ip = XpssInsta.devicePref.ipAddress ?: "",
            terminalId = "",
            terminalName = "",
            terminalAppVersion = getVersionName(),
            terminalOsVersion = XDevice.osVer
        )

        val deviceInfo = DeviceHeartbeatReq.Metadata.DeviceInfo(
            ip = XpssInsta.devicePref.ipAddress ?: "",
            deviceId = XpssInsta.userId,
            deviceName = XDevice.model,
            appVersion = getVersionName(),
            osVersion = XDevice.osVer
        )

        val metadata = DeviceHeartbeatReq.Metadata(
            location = location,
            security = security,
            terminalInfo = terminalInfo,
            deviceInfo = deviceInfo
        )

        val hbReq = DeviceHeartbeatReq(
            action = "HeartBeat",
            heartbeatId = hbReqId,
            merchantId = XpssInsta.devicePref.clientId ?: "",
            storeId = "",
            effective = "",
            metadata = metadata,
            deviceStatus = ""
        )
        return hbReq
    }

    fun apiInit_4_Heartbeat(reqObj: DeviceHeartbeatReq) {
        val (req, err) = getApiReq(reqObj)
        if (err != null) elog("XHeartbeatApi", "Error:$err")
        else if (req != null) mRepo.apiInit_4_DeviceHeartbeat(req = req)
    }

    private fun getVersionName(): String {
        val pInfo =
            XpssInsta.context.packageManager.getPackageInfo(XpssInsta.context.packageName, 0)
        return pInfo.versionName
    }
}