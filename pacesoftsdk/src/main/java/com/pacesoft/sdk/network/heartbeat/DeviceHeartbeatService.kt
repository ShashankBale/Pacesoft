package com.pacesoft.sdk.network.heartbeat

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Log
import android.widget.Toast
import com.pacesoft.sdk.R
import com.pacesoft.sdk.module.XApiRequest
import com.pacesoft.sdk.network.repo.HeartbeatApiRepo
import com.pacesoft.sdk.network.repo.pojo.heartbeat.*
import com.pacesoft.sdk.session.XpssInsta
import com.pacesoft.sdk.util.text.XCrypto
import kotlinx.coroutines.delay
import x.code.util.XConst
import x.code.util.XCoroutines
import x.code.util.device.XDevice
import x.code.util.gson
import x.code.util.log.elog

/*
class DeviceHeartbeatService : Service(), HeartBeatServiceCallback {

    companion object {
        var instance: DeviceHeartbeatService? = null


        fun startHeartbeatService(context: Context): Intent {
            val intent = Intent(context, DeviceHeartbeatService::class.java)
            intent.action = IntentAction.START.name
            //"Starting the service in >=26 Mode"
            context.startForegroundService(intent)
            return intent
        }

        fun stopHeartbeatService(context: Context): Intent {
            val intent = Intent(context, DeviceHeartbeatService::class.java)
            context.stopService(intent)
            return intent
        }
    }

    enum class ServiceState {
        STARTED,
        STOPPED,
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private val TAG = DeviceHeartbeatService::class.simpleName
    private var notificationText = "Device Heartbeat Service"
    private val defaultNotificationMsg = "Device Heartbeat Service.."

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Some component want to bind with the service")
        // We don't provide binding, so return null
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            Log.d(TAG, "using an intent with action $action")
            when (action) {
                IntentAction.START.name -> startService()
                IntentAction.STOP.name -> stopService()
                //IntentAction.DECLINE.name -> sendDeclineRequest()

                else -> Log.d(TAG, "This should never happen. No action in the received intent")
            }
        } else {
            Log.d(
                TAG,
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
        val notification = createNotification("")
        notification.flags =
            notification.flags or (Notification.FLAG_ONGOING_EVENT or Notification.FLAG_NO_CLEAR)
        notificationText = defaultNotificationMsg
        startForeground(XConst.NOTIFICATION_HEARTBEAT_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        //detachObservers()
        //Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent =
            Intent(applicationContext, DeviceHeartbeatService::class.java).also {
                it.setPackage(packageName)
            };
        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_MUTABLE) //FLAG_ONE_SHOT
        applicationContext.getSystemService(Context.ALARM_SERVICE);
        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        );
    }

    private fun startService() {
        if (isServiceStarted) return
        Log.d(TAG, "Starting the foreground service task")
        //dtoast("Service starting its task")
        isServiceStarted = true
        XpssInsta.devicePref.hbServiceState = ServiceState.STARTED.name

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HeartBeat::lock").apply {
                    acquire()
                }
            }

        //initBeaconX()

        // we're starting a loop in a coroutine
        XCoroutines.io {
            while (isServiceStarted) {
                processServiceIntention()
                delay(XpssInsta.devicePref.hbRefreshIntervalTimer + 100)
            }
        }
    }

    private suspend fun processServiceIntention() {
        val t1 = XpssInsta.devicePref.hbLastTs + XpssInsta.devicePref.hbRefreshIntervalTimer
        val t2 = System.currentTimeMillis()
        elog("HEARTBEAT_TIMER", "$t1,$t2, diff=${t2 - t1}, condition=${t2 > t1}")
        if (t2 > t1) {
            XpssInsta.devicePref.hbLastTs = t1
            delay(10000L) //10sec, because Native C code is not initialize sometime throws No implementation found for void com.pacesoft.sdk.session.Xskb.setKey(byte[]) (tried Java_com_pacesoft_sdk_session_Xskb_setKey and Java_com_pacesoft_sdk_session_Xskb_setKey___3B)
            apiInit_4_DeviceHeartbeat()
            elog("HEARTBEAT_TIMER", "$t1,$t2, diff=${t2 - t1}, Timer set to $t1")
        }
    }

    */
    /*
    private fun proceed() {
         try {
             apiInit_4_GeoFence(beaconXInfos)
             updateDevices()
             beaconXInfoHashMap.clear()
            //mBeaconCountdownHandler.postDelayed(this, XConst.BEACON_REFRESH_TIME)
         } catch (e: Exception) {
             Log.d(TAG,"Error making the request: ${e.message}")
         }
     }
     *//*


    private fun stopService() {
        Log.d(TAG, "Stopping the foreground service")
        //dtoast("Service stopping")

        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.d(TAG, "Service stopped without being started: ${e.message}")
        }

        isServiceStarted = false

        XpssInsta.devicePref.hbServiceState = ServiceState.STOPPED.name
    }


    private var mReceiverTag = false

    //private val beaconXInfoHashMap by lazy { ConcurrentHashMap<String, BeaconXInfo>() }
    //private var beaconXInfos = ArrayList<BeaconXInfo>()
    //private val mokoBleScanner: MokoBleScanner by lazy { MokoBleScanner(applicationContext) }
    //private val mokoInsta by lazy { MokoSupport.getInstance() }
    //private val beaconXInfoParsable by lazy { BeaconXInfoParseableImpl() }
    private var isBeaconScanningOn: Boolean = false


    */
    /*
    private fun initBeaconX() {
           mokoInsta.init(applicationContext)
           // 注册广播接收器
           // Register a broadcast receiver
           val filter = IntentFilter()
           filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
           registerReceiver(mReceiver, filter)
           mReceiverTag = true
           if (!mokoInsta.isBluetoothOpen) {
               // 蓝牙未打开，开启蓝牙
               // (Bluetooth is not turned on, turn on Bluetooth)
               mokoInsta.enableBluetooth()
           } else {
               if (isBeaconScanningOn == false) {
                   startBeaconScan()
               }
           }
    }
    *//*


    private val mBeaconCountdownHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val runBeaconCountdown = object : Runnable {
        override fun run() {
            apiInit_4_DeviceHeartbeat()
            //updateDevices()
            //beaconXInfoHashMap.clear()
            mBeaconCountdownHandler.postDelayed(this, XConst.HEARTBEAT_REFRESH_INTERVAL_TIMER)
        }
    }

    */
    /*  private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
          override fun onReceive(context: Context?, pIntent: Intent?) {
              pIntent?.also { intent ->
                  val action = intent.action
                  if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
                      val blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)
                      when (blueState) {
                          BluetoothAdapter.STATE_TURNING_OFF -> if (isBeaconScanningOn) {
                              stopHeartBeat()
                          }
                          BluetoothAdapter.STATE_ON -> if (!isBeaconScanningOn) {
                              startHeartBeat()
                          }
                      }
                  }
              }
          }
      }
    *//*


    fun startHeartBeat() {
        mBeaconCountdownHandler.removeCallbacks(runBeaconCountdown)
        mBeaconCountdownHandler.post(runBeaconCountdown)
    }

    fun stopHeartBeat() {
    }

    val mRepo = HeartbeatApiRepo()

    private fun apiInit_4_DeviceHeartbeat() {
        return

        var reqId = XpssInsta.devicePref.hbReferenceId
        if (reqId == 999999999L) {
            XpssInsta.devicePref.hbReferenceId = 0L
        }
        reqId += 1
        val hbReqId = String.format("%09d", reqId);

        val security = Security(
            rooted = "NO",
            screenRecording = "No",
            activityHijacking = "No",
            clickJacking = "No"
        )

        val terminalInfo = TerminalInfo(
            ip = XpssInsta.devicePref.ipAddress ?: "",
            terminalId = "",
            terminalName = "",
            terminalAppVersion = getVersionName(),
            terminalOsVersion = XDevice.osVer
        )

        val deviceInfo = DeviceInfo(
            ip = XpssInsta.devicePref.ipAddress ?: "",
            deviceId = XpssInsta.userId,
            deviceName = XDevice.model,
            appVersion = getVersionName(),
            osVersion = XDevice.osVer
        )

        val metadata = Metadata(
            location = Location("0.0", "0.0"),
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

        val (req, err) = getApiReqBody(hbReq)
        if (err != null) Log.e("HeartBeat", "Error:$err")
        else if (req != null) {
            mRepo.apiInit_4_DeviceHeartbeat(req)
        }
    }


    private fun getApiReqBody(obj: Any): Pair<XApiRequest?, String?> {
        val cp = XpssInsta.keysPref.getLatestPsck()

        val result = XCrypto.getEncryptedData(
            jsonStr = gson.toJson(obj),
            iv = cp.iv,
            key = cp.dk
        )

        val errorMsg = result.errorMsg
        return if (errorMsg != null) {
            Pair(null, errorMsg)
        } else {
            val req = XApiRequest(
                crypto = cp,
                apiKey = XpssInsta.apiKey,
                body = result.toBaseRequest(),
                bodyOg = obj
            )
            Pair(req, null)
        }
    }

    private fun createNotification(msg: String): Notification {
        if (msg.isNotEmpty())
            notificationText = msg

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            XConst.NOTIFICATION_HEARTBEAT_CHANNEL_ID,
            XConst.NOTIFICATION_HEARTBEAT_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).let {
            it.description = XConst.NOTIFICATION_HEARTBEAT_CHANNEL_DESC
            it.enableLights(true)
            it.lightColor = Color.RED
            it.enableVibration(true)
            it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            it
        }
        notificationManager.createNotificationChannel(channel)

        */
        /*
        val pendingIntent: PendingIntent = Intent(this, Splash::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }
        *//*



        val builder: Notification.Builder =
            Notification.Builder(
                this,
                XConst.NOTIFICATION_HEARTBEAT_CHANNEL_ID
            )

        if (msg.isNotEmpty()) {
            // Add Play button intent in notification.
            val declinePayIntent = Intent(this, DeviceHeartbeatService::class.java)
            declinePayIntent.action = IntentAction.DECLINE.name
            val pendingPlayIntent = PendingIntent.getService(this, 0, declinePayIntent, 0)
            val playAction: Notification.Action =
                Notification.Action(android.R.drawable.ic_media_play, "DECLINE", pendingPlayIntent)
            builder.addAction(playAction)

            // Add Pause button intent in notification.
            */
            /*
            val acceptPayIntent = Intent(this, SplashActivity::class.java)
            acceptPayIntent.action = IntentAction.ACCEPT.name
            val pendingPrevIntent = PendingIntent.getActivity(
                this,
                0,
                acceptPayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val prevAction: Notification.Action =
                Notification.Action(android.R.drawable.ic_media_pause, "ACCEPT", pendingPrevIntent)
            builder.addAction(prevAction)
            *//*

        }
        return builder
            .setContentTitle("Pacesoft Service")
            .setContentText(notificationText)
            //.setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_stat_project)
            .setTicker("")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }


    fun getHeaders(device: Boolean): HashMap<String, String?> {

        //val host = XUrl.getHost()
        val connection = "Keep-Alive"
        val headerMap = HashMap<String, String?>()
        if (!device)
            headerMap["Host"] = ""
        headerMap["Connection"] = connection
        return headerMap
    }

    override fun onResponse(heartbeatResponse: HeartbeatRsp?) {
        //dtoast("Response :" + heartbeatResponse?.responseText ?: "")

        if (heartbeatResponse != null) {
            if (heartbeatResponse != null) {
                //private var cid = ""
                //cid = heartbeatResponse.payDetails.customerId.toString()
                //pushNotification(heartbeatResponse.payDetails)
            } else {
                if (notificationText != defaultNotificationMsg) {
                    notificationText = defaultNotificationMsg
                    val notification: Notification = createNotification("")
                    val mNotificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    mNotificationManager.notify(XConst.NOTIFICATION_HEARTBEAT_ID, notification)
                }
            }
        }
    }

    */
    /*
    private fun pushNotification(payDetails: UserHeartbeatRsp.PayDetails) {
        if(notificationText.equals(Default_Notification_MSG)) {
            stopBeaconScan()
            val text = "You have Pay request for " + payDetails.amount
            val notification: Notification = createNotification(text)
            val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(NotifyID, notification)
        }
    }
    *//*


    override fun onError(heartbeatResponse: HeartbeatRsp?, msg: String?) {
        Toast.makeText(applicationContext, "Response Error :" + msg, Toast.LENGTH_LONG).show()
    }


    fun getVersionName(): String {
        val pInfo =
            XpssInsta.context.packageManager.getPackageInfo(XpssInsta.context.packageName, 0)
        return pInfo.versionName
    }

    */
    /*
    private fun getUserId(): String? {
        return XpssInsta.devicePref.userId
    }

    private fun getRequestId(): String {
        val userId = XpssInsta.devicePref.userId
        return if (userId?.trim()!!.isNotEmpty())
            userId?.substring((userId.length.minus(3)), userId.length) + System.currentTimeMillis()
        else
            System.currentTimeMillis().toString()
    }
    *//*

}
*/
