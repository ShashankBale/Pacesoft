package com.pacesoft.sdk.network.heartbeat

import com.pacesoft.sdk.network.repo.pojo.heartbeat.HeartbeatRsp

interface HeartBeatServiceCallback {

    fun onResponse(heartbeatResponse: HeartbeatRsp?)

    fun onError(heartbeatResponse: HeartbeatRsp?, msg: String?)

}