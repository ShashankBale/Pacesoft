package com.pacesoft.sdk.network.heartbeat

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import com.pacesoft.sdk.session.XpssInsta
import kotlinx.coroutines.delay
import x.code.util.XConst
import x.code.util.XCoroutines
import x.code.util.log.elog

class XHeartbeatService : Service() {

    companion object {
        var sInstance: XHeartbeatService? = null

        var sIsHeartbeatServiceRunning = false

        fun startHeartbeatService(context: Context) {
            try {
                val intent = Intent(context, XHeartbeatService::class.java)
                intent.action = IntentAction.START.name
                //"Starting the service in >=26 Mode"
                context.startForegroundService(intent)
                //return intent
            } catch (e: Exception) {
                e.printStackTrace()
                //android.app.ForegroundServiceStartNotAllowedException: startForegroundService() not allowed due to mAllowStartForeground false: service com.pacesoft/.sdk.network.heartbeat.XHeartbeatService

                //Let restart the service after X sec, if this is giving a problem, specially
                XCoroutines.default {
                    //We are restarting service after 10sec
                    delay(10000L)
                    XCoroutines.main {
                        startHeartbeatService(context)
                        //dtoast("Service Started")
                    }
                }
            }
        }

        fun stopHeartbeatService(context: Context): Intent {
            val intent = Intent(context, XHeartbeatService::class.java)
            context.stopService(intent)
            return intent
        }
    }

    private val mTag = "XHeartbeatService"
    private val mTagServiceLifecycle = "LifeCyc#XHeartbeatService"

    //private var mPmWakeLock: PowerManager.WakeLock? = null

    private fun eLogLifeCyc(msg: String) {
        elog(mTagServiceLifecycle, msg)
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this
        eLogLifeCyc("#onCreate()")

        startForeground(
            XConst.NOTIFICATION_HEARTBEAT_ID,
            XHeartBeatNotification(this).getNotificationObj()
        )
    }

    override fun onBind(intent: Intent): IBinder? {
        eLogLifeCyc("#onBind(), Some component want to bind with the service")
        // We don't provide binding, so return null
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        eLogLifeCyc("#onStartCommand(), executed with startId: $startId")

        if (intent == null) {
            elog(mTag, "intent is null. It has been probably restarted by the system.")
            // by returning this we make sure the service is restarted if the system kills the service
            return START_STICKY
        }

        elog(mTag, "using an intent with action ${intent.action}")
        when (intent.action) {
            IntentAction.START.name -> xStartService()
            IntentAction.STOP.name -> xStopService()
            //IntentAction.DECLINE.name -> sendDeclineRequest()

            else -> elog(mTag, "This should never happen. No action in the received intent")
        }

        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        val stopService = super.stopService(name)
        eLogLifeCyc("#stopService($name) server got stopped")
        sInstance = null
        return stopService
    }

    override fun onDestroy() {
        super.onDestroy()
        eLogLifeCyc("#onDestroy() server got destroy")
        sInstance = null
        //detachObservers()
        //Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        eLogLifeCyc("#onTaskRemoved()")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ctx = applicationContext ?: return

            val restartServiceIntent = Intent(ctx, XHeartbeatService::class.java).also {
                it.setPackage(packageName)
            }

            val restartServicePendingIntent = PendingIntent.getService(
                this,
                1,
                restartServiceIntent,
                PendingIntent.FLAG_MUTABLE
            )

            ctx.getSystemService(Context.ALARM_SERVICE)

            val alarmService = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent
            )
        }
    }

    private fun xStartService() {
        if (sIsHeartbeatServiceRunning) return
        elog(mTag, "Starting the foreground service task")
        sIsHeartbeatServiceRunning = true

        // we need this lock so our service gets not affected by Doze Mode
        /*
        mPmWakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HeartBeat::lock").apply {
                acquire()
            }
        }
        */

        //initBeaconX()

        // we're starting a loop in a coroutine
        XCoroutines.default {
            while (sIsHeartbeatServiceRunning) {
                xProcessServiceIntention()
                delay(XpssInsta.devicePref.hbRefreshIntervalTimer + 100)
            }
        }
    }

    private suspend fun xProcessServiceIntention() {
        val t1 = XpssInsta.devicePref.hbLastTs + XpssInsta.devicePref.hbRefreshIntervalTimer
        val t2 = System.currentTimeMillis()
        elog("HEARTBEAT_TIMER", "$t1,$t2, diff=${t2 - t1}, condition=${t2 > t1}")
        if (t2 > t1) {
            XpssInsta.devicePref.hbLastTs = t1
            delay(3000L) //3sec, because Native C code is not initialize sometime throws No implementation found for void com.pacesoft.sdk.session.Xskb.setKey(byte[]) (tried Java_com_pacesoft_sdk_session_Xskb_setKey and Java_com_pacesoft_sdk_session_Xskb_setKey___3B)
            apiInit_4_DeviceHeartbeat()
            elog("HEARTBEAT_TIMER", "$t1,$t2, diff=${t2 - t1}, Timer set to $t1")
        }
    }

    private fun xStopService() {
        elog(mTag, "Stopping the foreground service")
        try {
            /*
            mPmWakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            */
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            elog(mTag, "Service stopped without being started: ${e.message}")
        }

        sIsHeartbeatServiceRunning = false
    }


    private fun apiInit_4_DeviceHeartbeat() {
        val api = XHeartbeatApi()
        val reqObj = api.getReqObj() ?: return
        api.apiInit_4_Heartbeat(reqObj)
    }


    /*
    val callback: HeartBeatServiceCallback by lazy {
        val cb = object : HeartBeatServiceCallback {
            override fun onResponse(heartbeatResponse: HeartbeatRsp?) {
                //dtoast("Response :" + heartbeatResponse?.responseText ?: "")

                if (heartbeatResponse != null) {
                    //private var cid = ""
                    //cid = heartbeatResponse.payDetails.customerId.toString()
                    //pushNotification(heartbeatResponse.payDetails)
                } else {
                    val notification: Notification = createNotification()
                    val mNotificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    mNotificationManager.notify(
                        XConst.NOTIFICATION_HEARTBEAT_ID,
                        notification
                    )
                }
            }

            override fun onError(heartbeatResponse: HeartbeatRsp?, msg: String?) {
                Toast.makeText(applicationContext, "Response Error :" + msg, Toast.LENGTH_LONG)
                    .show()
            }
        }

        cb
    }
    */

}
