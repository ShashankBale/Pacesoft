package com.pacesoft.sdk.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.pacesoft.sdk.network.heartbeat.XHeartbeatService
import x.code.app.XCodeApp
import x.code.util.log.elog
import java.util.*

object PaceSoftSdk {

    private val tag = "PaceSoftSdk"

    lateinit var app: Application
    lateinit var appId: String
    //lateinit var mXzDefend: XZDefend
    lateinit var callback: PaceSoftCallback

    val ctx: Context by lazy { app.applicationContext }

    fun init(
        application: Application,
        appId: String,
        callback: PaceSoftCallback,
    ) {
        this.app = application
        this.appId = appId
        this.callback = callback

        XCodeApp.app = application
        val ctx = application.applicationContext

        //initZDefend()
        XHeartbeatService.startHeartbeatService(ctx)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)
    }


    private val lifecycleEventObserver = LifecycleEventObserver { source, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> elog(tag, "#onCreate")
            Lifecycle.Event.ON_START -> onStart()
            Lifecycle.Event.ON_RESUME -> elog(tag, "#onResume")
            Lifecycle.Event.ON_PAUSE -> elog(tag, "#onPause")
            Lifecycle.Event.ON_STOP -> onStop()
            Lifecycle.Event.ON_DESTROY -> elog(tag, "#onDestroy") //Never gets call
            Lifecycle.Event.ON_ANY -> elog(tag, "#onAny")
        }
    }

    fun onStart() {
        elog(tag, "#onStart")
        /*if (::mXzDefend.isInitialized)
            mXzDefend.getActiveThreat()*/
    }

    fun onStop() {
        elog(tag, "#onStop")
    }

    private fun initZDefend() {
        /*mXzDefend = XZDefend(ctx)
        mXzDefend.initializeZDefend()
        mXzDefend.startDetection()*/
    }

    fun isAppInit() = PaceSoftSdk::app.isInitialized
}