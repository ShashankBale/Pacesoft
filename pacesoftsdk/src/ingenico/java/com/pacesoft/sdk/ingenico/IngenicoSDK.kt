package com.pacesoft.sdk.ingenico

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.text.TextUtils
import android.util.Log
import com.pacesoft.sdk.ingenico.IngenicoDeviceConfig.KEYID_PIN
import com.pacesoft.sdk.ingenico.entity.CardOption
import com.pacesoft.sdk.ingenico.entity.EMVOption
import com.pacesoft.sdk.ingenico.utility.*
import com.pacesoft.sdk.util.utility.Utils
import com.pacesoft.sdk.vm.user.merchant.terminal.MTerminalVm
import com.usdk.apiservice.aidl.DeviceServiceData
import com.usdk.apiservice.aidl.UDeviceService
import com.usdk.apiservice.aidl.beeper.UBeeper
import com.usdk.apiservice.aidl.constants.LogLevel
import com.usdk.apiservice.aidl.constants.RFDeviceName
import com.usdk.apiservice.aidl.data.IntValue
import com.usdk.apiservice.aidl.data.StringValue
import com.usdk.apiservice.aidl.device.DeviceInfo
import com.usdk.apiservice.aidl.device.UDeviceManager
import com.usdk.apiservice.aidl.emv.*
import com.usdk.apiservice.aidl.emv.EMVTag.A_TAG_TM_CVM_LIMIT
import com.usdk.apiservice.aidl.emv.EMVTag.EMV_TAG_IC_ICCDYNNUM
import com.usdk.apiservice.aidl.led.Light
import com.usdk.apiservice.aidl.led.ULed
import com.usdk.apiservice.aidl.magreader.TrackID
import com.usdk.apiservice.aidl.pinpad.*
import com.usdk.apiservice.aidl.printer.UPrinter
import com.usdk.apiservice.aidl.rfreader.URFReader
import com.usdk.apiservice.limited.DeviceServiceLimited
import com.usdk.apiservice.limited.pinpad.PinpadLimited
import java.nio.charset.Charset
import java.util.*

class IngenicoSDK {

    private var onlineSuccess: Boolean = false
    private var amount:String = ""
    private var initialized = false
    private lateinit var activity: Activity
    private lateinit var ingenicoSDKCB: IngenicoSDKCB
    private var deviceService: UDeviceService? = null
    private var beeper: UBeeper? = null
    private var deviceManager: UDeviceManager? = null
    private var ledManger: ULed? = null
    var emvOption: EMVOption = EMVOption.create()
    var cardOption: CardOption = CardOption.create()
    var emv: UEMV? = null
    protected var pinpad: UPinpad? =null
    private var lastCardRecord: CardRecord? = null
    private var wholeTrkId = 0
    var uiHandler = Handler(Looper.getMainLooper())
    protected var pinpadLimited: PinpadLimited? = null
    protected var processOnlineResponse = false
    var mCardType = -1
    var track2Data = ""
    private var tagParams:LinkedHashMap<String,String?>? =null
    init {

    }

    fun initializeIngenico(activity: Activity,ingenicoSDKCB: IngenicoSDKCB){
        this@IngenicoSDK.activity=activity
        this@IngenicoSDK.ingenicoSDKCB = ingenicoSDKCB
        bindService()
    }

    fun terminateService(){
        unbindService()
    }



    fun register() {
        try {
            deviceService!!.register(null, Binder())
            //showContent()
            initialized=true
            ingenicoSDKCB.onInitialized()
        } catch (e: RemoteException) {
            initialized=false
            e.printStackTrace()
        }
    }

    fun unregister() {
        try {
            deviceService?.unregister(null)
            //showErrorContent
            initialized = false
            ingenicoSDKCB.onInitializationFailed()
        } catch (e: RemoteException) {
            initialized = false
            e.printStackTrace()
        }

    }

    fun enableEMVLog(status: Boolean) {
        try {
            if (status)
                deviceService?.setLogLevel(
                    LogLevel.EMVLOG_REALTIME,
                    LogLevel.USDKLOG_VERBOSE
                )//Detailed log
            else
                deviceService?.setLogLevel(LogLevel.EMVLOG_CLOSE, LogLevel.USDKLOG_CLOSE)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun unbindService(){
        val service = Intent("com.usdk.apiservice")
        service.setPackage("com.usdk.apiservice")
        activity.unbindService(serviceConnection)
    }

    private val serviceConnection  = object :  ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            deviceService = UDeviceService.Stub.asInterface(service)
            DeviceServiceLimited.bind(activity, deviceService, object :
                DeviceServiceLimited.ServiceBindListener {
                override fun onSuccess() {
                    Log.d(
                        "MainActivity",
                        "=> DeviceServiceLimited | bindSuccess"
                    )
                }

                override fun onFail() {
                    Log.e(
                        "MainActivity",
                        "=> bind DeviceServiceLimited fail"
                    )
                    initialized = false
                }
            })
            register()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            unregister()
            deviceService = null
            initialized = false
            DeviceServiceLimited.unbind(activity)
        }
    };

    private fun bindService() {
        val service = Intent("com.usdk.apiservice")
        service.setPackage("com.usdk.apiservice")
        activity.bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    fun getBeeper(): UBeeper? {
        try {
            if (beeper == null)
                beeper = UBeeper.Stub.asInterface(deviceService?.getBeeper())
            return beeper
        } catch (e: RemoteException) {
            e.printStackTrace()
            return null
        }
    }

    @Throws(IllegalStateException::class)
    fun getEMV(): UEMV {
        val iBinder: IBinder = object : IBinderCreator() {
            @Throws(RemoteException::class)
            override fun create(): IBinder {
                return deviceService!!.emv
            }
        }.start()
        return UEMV.Stub.asInterface(iBinder)
    }


    @Throws(java.lang.IllegalStateException::class)
    fun getPinpad(kapId: KAPId?, keySystem: Int, deviceName: String?): UPinpad {
        val iBinder: IBinder = object : IBinderCreator() {
            @Throws(RemoteException::class)
            override fun create(): IBinder {
                return deviceService!!.getPinpad(kapId, keySystem, deviceName)
            }
        }.start()
        return UPinpad.Stub.asInterface(iBinder)
    }

    @Throws(java.lang.IllegalStateException::class)
    fun getRFReader(deviceName: String?): URFReader {
        val iBinder: IBinder = object : IBinderCreator() {
            @Throws(RemoteException::class)
            override fun create(): IBinder {
                val param = Bundle()
                param.putString("rfDeviceName", deviceName)
                return deviceService!!.getRFReader(param)
            }
        }.start()
        return URFReader.Stub.asInterface(iBinder)
    }

    @Throws(java.lang.IllegalStateException::class)
    fun getPrinter(): UPrinter {
        val iBinder: IBinder = object : IBinderCreator() {
            @Throws(RemoteException::class)
            override fun create(): IBinder {
                return deviceService!!.printer
            }
        }.start()
        return UPrinter.Stub.asInterface(iBinder)
    }

    fun setAmount(amount:String){
        this@IngenicoSDK.amount = amount
    }

    fun beepTwoSec() {
        beeper?.startBeep(800)
        beeper?.startBeep(500)
        beeper?.startBeep(800)
        beeper?.startBeep(500)
    }

    fun stopBeeper() {
        beeper?.stopBeep()
    }


    fun getDeviceManager(): UDeviceManager? {

        try {
            if (deviceManager == null)
                deviceManager = UDeviceManager.Stub.asInterface(deviceService?.getDeviceManager())
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return deviceManager
    }


    fun getDeviceInfo(): DeviceInfo? {
        return try {
            deviceManager?.getDeviceInfo();
        } catch (e: RemoteException) {
            e.printStackTrace()
            null
        }
    }



    fun printDeviceInfo(): String {

        return try {
            val deviceInfo = getDeviceInfo()
            val stringBuilder = StringBuilder()
            stringBuilder.append("Serial no: " + deviceInfo?.serialNo);
            stringBuilder.append("Hardware serial no: " + deviceInfo?.hardWareSn);
            stringBuilder.append("Terminal model: " + deviceInfo?.model);
            stringBuilder.append("Manufacturer: " + deviceInfo?.manufacture);
            stringBuilder.toString()
        } catch (e: RemoteException) {
            e.printStackTrace()
            "NA"
        }

    }


    fun getLEDManger(): ULed? {

        try {
            if (ledManger == null) {
                val param = Bundle()
                param.putString(DeviceServiceData.RF_DEVICE_NAME, RFDeviceName.INNER);
                ledManger = ULed.Stub.asInterface(deviceService?.getLed(param))

            }

        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return ledManger
    }


    public fun turnOnGreenAndRed() {
        try {
            ledManger?.turnOn(Light.BLUE)
            ledManger?.turnOn(Light.RED)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    public fun turnOffGreenAndRed() {
        try {
            ledManger?.turnOff(Light.BLUE)
            ledManger?.turnOff(Light.RED)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

//    var remoteSecDevServiceProvider: RemoteSecDevServiceProvider?=null
//    suspend fun connectNSDS() {
//        remoteSecDevServiceProvider = RemoteSecDevServiceProvider(application)
//        val result = remoteSecDevServiceProvider?.connect()
//    }
//
//    fun disconnectNSDS(){
//        RemoteSecDevServiceProvider(application)?.disconnect()
//    }
//
//    fun getNSDSProvider(): SecDeviceCtrlHelper? {
//        return remoteSecDevServiceProvider?.getSecDeviceCtrlHelper()
//    }
//    fun getNSDSModuleProvider(): SecModuleHelper? {
//        return remoteSecDevServiceProvider?.getSecModuleHelper()
//    }

    fun initDeviceInstance() {
        emv = getEMV()
        pinpad = getPinpad(
            KAPId(IngenicoDeviceConfig.REGION_ID, IngenicoDeviceConfig.KAP_NUM),
            KeySystem.KS_MKSK,
            IngenicoDeviceConfig.PINPAD_DEVICE_NAME
        )
        getDeviceManager()
    }

    private fun openPinpad() {
        try {
            pinpad?.open()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun closePinpad() {
        try {
            pinpad?.close()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    var supportTrack1 = true
    var supportTrack2 = true
    var supportTrack3 = true
    var supportICC = true
    var supportPass = true
    var supportMag = true
    var supportAll = true
    var loopSearch = false

    fun initCardOption() {
        setTrkIdWithWholeData(
            supportTrack1,
            TrackID.TRK1
        )


        setTrkIdWithWholeData(
            supportTrack2,
            TrackID.TRK2
        )

        setTrkIdWithWholeData(
            supportTrack3,
            TrackID.TRK3
        )

        cardOption.supportICCard(
            supportICC
        )

        cardOption.supportRFCard(
            supportPass
        )

        cardOption.supportMagCard(
            supportMag
        )

        cardOption.supportAllRFCardTypes(
            supportAll
        )

        cardOption.loopSearchRFCard(
            loopSearch
        )

        cardOption.rfDeviceName(IngenicoDeviceConfig.RF_DEVICE_NAME)
        cardOption.trackCheckEnabled(false)
    }

    private fun setTrkIdWithWholeData(isSlted: Boolean, trkId: Int) {
        if (isSlted) {
            wholeTrkId = wholeTrkId or trkId
        } else {
            wholeTrkId = wholeTrkId and trkId.inv()
        }
        cardOption.trkIdWithWholeData(wholeTrkId)
    }

    fun searchMSRCard():Int{

        mCardType = -1
        processOnlineResponse = false
        try {
            if(emv != null)
                emv?.stopSearch()
            emv?.searchCard(
                cardOption.toBundle(),
                IngenicoDeviceConfig.TIMEOUT,
                object : SearchCardListener.Stub() {
                    override fun onCardPass(cardType: Int) {
                        Log.d("cardprocessing","=> onCardPass | cardType = $cardType")
                        startEMV(emvOption.flagPSE(0x01.toByte()))
                        mCardType = 2
                        ingenicoSDKCB.onCardPass(mCardType)
                    }

                    override fun onCardInsert() {
                        Log.d("cardprocessing","=> onCardInsert")
                        startEMV(emvOption.flagPSE(0x00.toByte()))
                        mCardType = 1
                        ingenicoSDKCB.onCardInserted(mCardType)
                    }

                    override fun onCardSwiped(track: Bundle) {
                        Log.d("cardprocessing","=> onCardSwiped")
                        Log.d("cardprocessing","==> Pan: " + track.getString(EMVData.PAN))
                        Log.d("cardprocessing","==> Track 1: " + track.getString(EMVData.TRACK1))
                        Log.d("cardprocessing","==> Track 2: " + track.getString(EMVData.TRACK2))
                        Log.d("cardprocessing","==> Track 3: " + track.getString(EMVData.TRACK3))
                        Log.d("cardprocessing","==> Service code: " + track.getString(EMVData.SERVICE_CODE))
                        Log.d("cardprocessing","==> Card exprited date: " + track.getString(EMVData.EXPIRED_DATE))
                        val trackStates = track.getIntArray(EMVData.TRACK_STATES)
                        for (i in trackStates!!.indices) {
                            Log.d("cardprocessing",
                                String.format(
                                    "==> Track %s state: %d",
                                    i + 1,
                                    trackStates!![i]
                                )
                            )
                        }
                        track2Data = track.getString(EMVData.TRACK2)?:""
                        track2Data= track2Data.substringBefore("?","")
                        track2Data=track2Data.substringAfter(";","")
//                        entryMode = 4
                        mCardType = 0
//                        ingenicoSDKCB.onMSRCardRead(mCardType,track2Data)
                        ingenicoSDKCB.onMSRCardRead()
//                        showMessage("MSR Card Processed")
                    }

                    override fun onTimeout() {
                        Log.d("cardprocessing","=> onTimeout")
                        ingenicoSDKCB.onTimeout()
                    }

                    override fun onError(code: Int, message: String) {
                        Log.d("cardprocessing",String.format("=> onError | %s[0x%02X]", message, code))
                        ingenicoSDKCB.onError(String.format("=> onError | %s[0x%02X]", message, code))
                    }
                })
        } catch (e: java.lang.Exception) {
            handleException(e)
        }
        return mCardType
    }


    protected fun searchRFCard(next: Runnable) {
        val rfCardOption: Bundle = CardOption.create()
            .supportICCard(false)
            .supportMagCard(false)
            .supportRFCard(true)
            .rfDeviceName(IngenicoDeviceConfig.RF_DEVICE_NAME)
            .toBundle()
        try {
            emv?.searchCard(rfCardOption, IngenicoDeviceConfig.TIMEOUT, object : SearchListenerAdapter() {
                override fun onCardPass(cardType: Int) {
                    next.run()
                }


                override fun onTimeout() {
                    stopEMV()
                }

                override fun onError(code: Int, message: String?) {
                    stopEMV()
                }
            })
        } catch (e: Exception) {
            handleException(e)
        }
    }

    fun getDeviceId():String {
        return if (getDeviceInfo() != null)
            getDeviceInfo()!!.serialNo
        else
            ""
    }

    fun startEMV(option: EMVOption) {
        try {
//            outputBlueText("******  start EMV ******")
            getKernelVersion()
            getCheckSum()
            if(emv == null)
                return
            val ret: Int = emv!!.startEMV(option.toBundle(), emvEventHandler)
//            outputResult(ret, "=> Start EMV")
            openPinpad()
            loadMainKey()
            loadWorkKeys()
        } catch (e: java.lang.Exception) {
            handleException(e)
        }
    }

    private fun getKernelVersion() {
        try {
            if(emv == null)
                return
            val version = StringValue()
            val ret: Int = emv?.getKernelVersion(version)!!
            if (ret == EMVError.SUCCESS) {
//                outputBlackText("EMV kernel version: " + version.data)
            } else {
//                outputRedText("EMV kernel version: fail, ret = $ret")
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun getCheckSum() {
        try {
            if(emv == null)
                return
            val flag = 0xA2
            val checkSum = StringValue()
            val ret: Int = emv!!.getCheckSum(flag, checkSum)
            if (ret == EMVError.SUCCESS) {
//                outputBlackText("EMV kernel[" + flag + "] checkSum: " + checkSum.data)
            } else {
//                outputRedText("EMV kernel[$flag] checkSum: fail, ret = $ret")
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun stopEMV() {
        try {
            closePinpad()
            if(emv!=null)
                emv!!.stopEMV()
        } catch (e: java.lang.Exception) {
            handleException(e)
        }
    }

    fun stopSearch() {
        try {
            emv?.stopSearch()
        } catch (e: java.lang.Exception) {
            handleException(e)
        }
    }

    protected fun halt() {
        try {
            emv?.halt()
        } catch (e: java.lang.Exception) {
            handleException(e)
        }
    }


    protected var emvEventHandler: EMVEventHandler = object : EMVEventHandler.Stub() {
        @Throws(RemoteException::class)
        override fun onInitEMV() {
            doInitEMV()
        }

        @Throws(RemoteException::class)
        override fun onWaitCard(flag: Int) {
            doWaitCard(flag)
        }

        @Throws(RemoteException::class)
        override fun onCardChecked(cardType: Int) {
            // Only happen when use startProcess()
            doCardChecked(cardType)
        }

        @Throws(RemoteException::class)
        override fun onAppSelect(reSelect: Boolean, list: List<CandidateAID>) {
            doAppSelect(reSelect, list)
        }

        @Throws(RemoteException::class)
        override fun onFinalSelect(finalData: FinalData) {
            doFinalSelect(finalData)
        }

        @Throws(RemoteException::class)
        override fun onReadRecord(cardRecord: CardRecord) {
            lastCardRecord = cardRecord
            doReadRecord(cardRecord)
        }

        @Throws(RemoteException::class)
        override fun onCardHolderVerify(cvmMethod: CVMMethod) {
            doCardHolderVerify(cvmMethod)
        }

        @Throws(RemoteException::class)
        override fun onOnlineProcess(transData: TransData) {
            doOnlineProcess(transData)
        }

        @Throws(RemoteException::class)
        override fun onEndProcess(result: Int, transData: TransData) {
            doEndProcess(result, transData)
        }

        @Throws(RemoteException::class)
        override fun onVerifyOfflinePin(
            flag: Int,
            random: ByteArray,
            caPublicKey: CAPublicKey,
            offlinePinVerifyResult: OfflinePinVerifyResult
        ) {
            doVerifyOfflinePin(flag, random, caPublicKey, offlinePinVerifyResult)
        }

        @Throws(RemoteException::class)
        override fun onObtainData(ins: Int, data: ByteArray) {
            Log.d("EMV",
                "=> onObtainData: instruction is 0x" + Integer.toHexString(ins) + ", data is " + BytesUtil.bytes2HexString(
                    data
                )
            )
        }

        @Throws(RemoteException::class)
        override fun onSendOut(ins: Int, data: ByteArray) {
            doSendOut(ins, data)
        }
    }

    @Throws(RemoteException::class)
    fun doInitEMV() {
        Log.d("EMV","=> onInitEMV ")
        manageAID()
        manageCAPKey()
        //  init transaction parameters，please refer to transaction parameters
        //  chapter about onInitEMV event in《UEMV develop guide》
        //  For example, if VISA is supported in the current transaction,
        //  the label: DEF_TAG_PSE_FLAG(M) must be set, as follows:
        emv?.setTLV(KernelID.VISA, EMVTag.DEF_TAG_PSE_FLAG, "03")
        // For example, if AMEX is supported in the current transaction，
        // labels DEF_TAG_PSE_FLAG(M) and DEF_TAG_PPSE_6A82_TURNTO_AIDLIST(M) must be set, as follows：
        // emv?.setTLV(KernelID.AMEX, EMVTag.DEF_TAG_PSE_FLAG, "03");
        // emv?.setTLV(KernelID.AMEX, EMVTag.DEF_TAG_PPSE_6A82_TURNTO_AIDLIST, "01");
    }

    @Throws(RemoteException::class)
    protected fun manageAID() {
        Log.d("EMV","****** manage AID ******")
        val aids = arrayOf(
            "A000000333010106",
            "A000000333010103",
            "A000000333010102",
            "A000000333010101",
            "A0000000651010",
            "A0000000043060",
            "A0000000041010",
            "A000000003101002",
            "A0000000031010"
        )
        for (aid in aids) {
            val ret: Int = emv!!.manageAID(ActionFlag.ADD, aid, true)
            outputResult(ret, "=> add AID : $aid")
        }
    }


    @Throws(RemoteException::class)
    private fun manageCAPKey() {
        emv!!.manageCAPubKey(ActionFlag.CLEAR, null)
        Log.d("EMV","****** manage CAPKey ******")
        val ca = arrayOf(
            "9F0605A0000000659F220109DF05083230323931323331DF060101DF070101DF028180B72A8FEF5B27F2B550398FDCC256F714BAD497FF56094B7408328CB626AA6F0E6A9DF8388EB9887BC930170BCC1213E90FC070D52C8DCD0FF9E10FAD36801FE93FC998A721705091F18BC7C98241CADC15A2B9DA7FB963142C0AB640D5D0135E77EBAE95AF1B4FEFADCF9C012366BDDA0455C1564A68810D7127676D493890BDDF040103DF03144410C6D51C2F83ADFD92528FA6E38A32DF048D0A",
            "9F0605A0000000659F220110DF05083230323231323331DF060101DF070101DF02819099B63464EE0B4957E4FD23BF923D12B61469B8FFF8814346B2ED6A780F8988EA9CF0433BC1E655F05EFA66D0C98098F25B659D7A25B8478A36E489760D071F54CDF7416948ED733D816349DA2AADDA227EE45936203CBF628CD033AABA5E5A6E4AE37FBACB4611B4113ED427529C636F6C3304F8ABDD6D9AD660516AE87F7F2DDF1D2FA44C164727E56BBC9BA23C0285DF040103DF0314C75E5210CBE6E8F0594A0F1911B07418CADB5BAB",
            "9F0605A0000000659F220112DF05083230323431323331DF060101DF070101DF0281B0ADF05CD4C5B490B087C3467B0F3043750438848461288BFEFD6198DD576DC3AD7A7CFA07DBA128C247A8EAB30DC3A30B02FCD7F1C8167965463626FEFF8AB1AA61A4B9AEF09EE12B009842A1ABA01ADB4A2B170668781EC92B60F605FD12B2B2A6F1FE734BE510F60DC5D189E401451B62B4E06851EC20EBFF4522AACC2E9CDC89BC5D8CDE5D633CFD77220FF6BBD4A9B441473CC3C6FEFC8D13E57C3DE97E1269FA19F655215B23563ED1D1860D8681DF040103DF0314874B379B7F607DC1CAF87A19E400B6A9E25163E8",
            "9F0605A0000000659F220114DF05083230323631323331DF060101DF070101DF0281F8AEED55B9EE00E1ECEB045F61D2DA9A66AB637B43FB5CDBDB22A2FBB25BE061E937E38244EE5132F530144A3F268907D8FD648863F5A96FED7E42089E93457ADC0E1BC89C58A0DB72675FBC47FEE9FF33C16ADE6D341936B06B6A6F5EF6F66A4EDD981DF75DA8399C3053F430ECA342437C23AF423A211AC9F58EAF09B0F837DE9D86C7109DB1646561AA5AF0289AF5514AC64BC2D9D36A179BB8A7971E2BFA03A9E4B847FD3D63524D43A0E8003547B94A8A75E519DF3177D0A60BC0B4BAB1EA59A2CBB4D2D62354E926E9C7D3BE4181E81BA60F8285A896D17DA8C3242481B6C405769A39D547C74ED9FF95A70A796046B5EFF36682DC29DF040103DF0314C0D15F6CD957E491DB56DCDD1CA87A03EBE06B7B",
            "9F0605A0000003339F220101DF05083230323931323331DF060101DF070101DF028180BBE9066D2517511D239C7BFA77884144AE20C7372F515147E8CE6537C54C0A6A4D45F8CA4D290870CDA59F1344EF71D17D3F35D92F3F06778D0D511EC2A7DC4FFEADF4FB1253CE37A7B2B5A3741227BEF72524DA7A2B7B1CB426BEE27BC513B0CB11AB99BC1BC61DF5AC6CC4D831D0848788CD74F6D543AD37C5A2B4C5D5A93BDF040103DF0314E881E390675D44C2DD81234DCE29C3F5AB2297A0",
            "9F0605A0000003339F220102DF05083230323431323331DF060101DF070101DF028190A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57DF040103DF031403BB335A8549A03B87AB089D006F60852E4B8060",
            "9F0605A0000003339F220103DF05083230323731323331DF060101DF070101DF0281B0B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33DDF040103DF031487F0CD7C0E86F38F89A66F8C47071A8B88586F26",
        )

        for (item in ca) {
            val tlvList = TLVList.fromBinary(item)
            val tag9F06 = tlvList.getTLV("9F06")
            val rid = tag9F06.bytesValue
            val tag9F22 = tlvList.getTLV("9F22")
            val index = tag9F22.byteValue
            val tagDF05 = tlvList.getTLV("DF05")
            val expiredDate = tagDF05.bcdValue
            val tagDF02 = tlvList.getTLV("DF02")
            val mod = tagDF02.bytesValue
            val capKey = CAPublicKey()
            capKey.rid = rid
            capKey.index = index
            capKey.expDate = expiredDate
            capKey.mod = mod
            if (tlvList.contains("DF04")) {
                val tagDF04 = tlvList.getTLV("DF04")
                capKey.exp = tagDF04.bytesValue
            }
            if (tlvList.contains("DF03")) {
                val tagDF03 = tlvList.getTLV("DF03")
                capKey.hash = tagDF03.bytesValue
                capKey.hashFlag = 0x01.toByte()
            } else {
                capKey.hashFlag = 0x00.toByte()
            }
            val ret = emv!!.manageCAPubKey(ActionFlag.ADD, capKey)
            outputResult(
                ret,
                "=> add CAPKey rid = : " + BytesUtil.bytes2HexString(rid) + ", index = " + index
            )
        }
        val capKey = CAPublicKey()
        val expiry = "3230323731323331"
        val bytesExpiry = byteArrayOf(0x24, 0x12, 0x31)
        val result = String(bytesExpiry, Charset.forName("GBK"))
        capKey.rid = byteArrayOf(0xA0.toByte(), 0x00, 0x00, 0x00, 0x03)
        capKey.index = 0x92.toByte()
        capKey.expDate = byteArrayOf(0x24, 0x12, 0x31)
        capKey.mod = byteArrayOf(0x99.toByte(), 0x6a, 0xf5.toByte(), 0x6f, 0x56, 0x91.toByte(), 0x87.toByte(), 0xd0.toByte(), 0x92.toByte(), 0x93.toByte(), 0xc1.toByte(), 0x48, 0x10, 0x45, 0x0e, 0xd8.toByte(), 0xee.toByte(), 0x33, 0x57, 0x39, 0x7b, 0x18, 0xa2.toByte(), 0x45, 0x8e.toByte(), 0xfa.toByte(), 0xa9.toByte(), 0x2d, 0xa3.toByte(), 0xb6.toByte(), 0xdf.toByte(), 0x65, 0x14, 0xec.toByte(), 0x06, 0x01, 0x95.toByte(), 0x31, 0x8f.toByte(), 0xd4.toByte(), 0x3b, 0xe9.toByte(), 0xb8.toByte(), 0xf0.toByte(), 0xcc.toByte(), 0x66, 0x9e.toByte(), 0x3f, 0x84.toByte(), 0x40, 0x57, 0xcb.toByte(), 0xdd.toByte(), 0xf8.toByte(), 0xbd.toByte(), 0xa1.toByte(), 0x91.toByte(), 0xbb.toByte(), 0x64, 0x47, 0x3b, 0xc8.toByte(), 0xdc.toByte(), 0x9a.toByte(), 0x73, 0x0d, 0xb8.toByte(), 0xf6.toByte(), 0xb4.toByte(), 0xed.toByte(), 0xe3.toByte(), 0x92.toByte(), 0x41, 0x86.toByte(), 0xff.toByte(), 0xd9.toByte(), 0xb8.toByte(), 0xc7.toByte(), 0x73, 0x57, 0x89.toByte(), 0xc2.toByte(), 0x3a, 0x36, 0xba.toByte(), 0x0b, 0x8a.toByte(), 0xf6.toByte(), 0x53, 0x72, 0xeb.toByte(), 0x57, 0xea.toByte(), 0x5d, 0x89.toByte(), 0xe7.toByte(), 0xd1.toByte(), 0x4e, 0x9c.toByte(), 0x7b, 0x6b, 0x55, 0x74, 0x60, 0xf1.toByte(), 0x08, 0x85.toByte(), 0xda.toByte(), 0x16, 0xac.toByte(), 0x92.toByte(), 0x3f, 0x15, 0xaf.toByte(), 0x37, 0x58, 0xf0.toByte(), 0xf0.toByte(), 0x3e, 0xbd.toByte(), 0x3c, 0x5c, 0x2c, 0x94.toByte(), 0x9c.toByte(), 0xba.toByte(), 0x30, 0x6d, 0xb4.toByte(), 0x4e, 0x6a, 0x2c, 0x07, 0x6c, 0x5f, 0x67, 0xe2.toByte(), 0x81.toByte(), 0xd7.toByte(), 0xef.toByte(), 0x56, 0x78, 0x5d, 0xc4.toByte(), 0xd7.toByte(), 0x59, 0x45, 0xe4.toByte(), 0x91.toByte(), 0xf0.toByte(), 0x19, 0x18, 0x80.toByte(), 0x0a, 0x9e.toByte(), 0x2d, 0xc6.toByte(), 0x6f, 0x60, 0x08, 0x05, 0x66, 0xce.toByte(), 0x0d, 0xaf.toByte(), 0x8d.toByte(), 0x17, 0xea.toByte(), 0xd4.toByte(), 0x6a, 0xd8.toByte(), 0xe3.toByte(), 0x0a, 0x24, 0x7c, 0x9f.toByte())
        capKey.exp=  byteArrayOf(0x03)
        capKey.hash= byteArrayOf(0x42,
            0x9C.toByte(),
            0x95.toByte(),0x4A,0x38,0x59,
            0xCE.toByte(), 0xF9.toByte(),0x12,
            0x95.toByte(), 0xF6.toByte(),0x63, 0xC9.toByte(),0x63,
            0xE5.toByte(), 0x82.toByte(), 0xED.toByte(),0x6E, 0xB2.toByte(),0x53)
        val ret = emv!!.manageCAPubKey(ActionFlag.ADD, capKey)
    }

    @Throws(RemoteException::class)
    fun doWaitCard(flag: Int) {
        when (flag) {
            WaitCardFlag.ISS_SCRIPT_UPDATE, WaitCardFlag.SHOW_CARD_AGAIN -> searchRFCard { respondCard() }
            WaitCardFlag.EXECUTE_CDCVM -> {
                emv?.halt()
                uiHandler.postDelayed(Runnable { searchRFCard { respondCard() } }, 1200)
            }
            else -> Log.d("MainActivity","!!!! unknow flag !!!!")
        }
    }

    protected fun respondCard() {
        try {
            emv?.respondCard()
        } catch (e: RemoteException) {
            handleException(e)
        }
    }

    fun doCardChecked(cardType: Int) {
        // Only happen when use startProcess()
    }

    /**
     * Request cardholder to select application
     */
    fun doAppSelect(reSelect: Boolean, candList: List<CandidateAID>) {
//        outputText("=> onAppSelect: cand AID size = " + candList.size)
        if (candList.size > 1) {
            selectApp(candList, object : DialogUtil.OnSelectListener {


                override fun onCancel() {
                    try {
                        emv?.stopEMV()
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }

                override fun onSelected(item: Int) {
                    respondAID(candList[item].aid)
                }
            })
        } else {
            respondAID(candList[0].aid)
        }
    }

    protected fun selectApp(candList: List<CandidateAID>, listener: DialogUtil.OnSelectListener?) {
        val aidInfoList: MutableList<String> = ArrayList()
        for (candAid in candList) {
            aidInfoList.add(String(candAid.apn))
        }
        activity.runOnUiThread {
            DialogUtil.showSelectDialog(
                activity,
                "Please select app",
                aidInfoList,
                0,
                listener
            )
        }
    }

    protected fun respondAID(aid: ByteArray?) {
        try {
            Log.d("EMV","Select aid: " + BytesUtil.bytes2HexString(aid))
            val tmAid: TLV = TLV.fromData(EMVTag.EMV_TAG_TM_AID, aid)
            outputResult(emv!!.respondEvent(tmAid.toString()), "...onAppSelect: respondEvent")
        } catch (e: java.lang.Exception) {
            handleException(e)
        }
    }

    /**
     * Parameters can be set or adjusted according to the aid selected finally
     * please refer to transaction parameters chapter about onFinalSelect event in《UEMV develop guide》
     */
    @Throws(RemoteException::class)
    fun doFinalSelect(finalData: FinalData) {
        Log.d("EMV","=> onFinalSelect | " + EMVInfoUtil.getFinalSelectDesc(finalData))
        var tlvList: String? = null
        val txnAmtLong =  amount.replace("""[$,.]""".toRegex(), "")

        var bcd = String

            // First left pad the string
            // with space up to length L
            .format("%" + 12 + "s", txnAmtLong.toString())

            // Then replace all the spaces
            // with the given character ch
            .replace(' ', '0')//Utils.decimalToBcd(amount.toLong())
        var yymmdd = Utils.getDate("yyMMdd")
        var hhmmss = Utils.getTime("HHmmss")
        var nanoTime = System.nanoTime().toString()
        var capability = "0008C8"//"E0F8C8"//
        var currencyCode = "0840"
        var entryMode = when(mCardType){
            1-> "05"
            2-> "07"
            else -> "07"
        }
        var transType = "00"//0x00: Goods/Service
        var terminalType = "21"
            //"05"//"06"//"01"//"95"//"05"
        var sequenceCounter = nanoTime.substring(nanoTime.length-8,nanoTime.length)
        var serialNumber = deviceManager?.deviceInfo?.serialNo
        var serialNumberLen = serialNumber?.length?.div(2)
        when (finalData.kernelID.toInt()) {
            KernelID.EMV ->                // Parameter settings, see transaction parameters of EMV Contact Level 2 in《UEMV develop guide》
                // For reference only below
                tlvList =
                    "9F0206${bcd}9F03060000000000009A03${yymmdd}9F2103${hhmmss}9F4104${sequenceCounter}" +
                        "5F360102"    + "9F3501${terminalType}9F3303${capability}9F3901${entryMode}5F2802${currencyCode}9F4202${currencyCode}9F40056000F0A0019F1A02${currencyCode}5F2A02${currencyCode}DF780${serialNumberLen}${serialNumber}9F530111DF811901119C01${transType}9F660410800000" + "DF9181040100DF91810C0130DF91810E0190"
//                DF0606111111111111DF812606111111111111
//                tlvList =
//                    "9F02060000000001009F03060000000000009A031710209F21031505129F410400000001" +
//                            "9F3501229F3303E0F8C89F40056000F0A0019F1A0201565F2A0201569C0100" + "DF9181040100DF91810C0130DF91810E0190"



            KernelID.PBOC ->                // if suport PBOC Ecash，see transaction parameters of PBOC Ecash in《UEMV develop guide》.
                // If support qPBOC, see transaction parameters of QuickPass in《UEMV develop guide》.
                // For reference only below
                tlvList =
                    "9F0206${bcd}9F03060000000000009A03${yymmdd}9F2103${hhmmss}9F4104${sequenceCounter}9F660427004080"+"9F4C080000000000000000"+
                            "9F530111DF811901119C01${transType}5F360102"+"9F3501${terminalType}9F3303${capability}9F3901${entryMode}5F2802${currencyCode}9F4202${currencyCode}"+
                            "9F40056000F0A0019F1A02${currencyCode}"
            KernelID.VISA ->                // Parameter settings, see transaction parameters of PAYWAVE in《UEMV develop guide》.
                tlvList = java.lang.StringBuilder()
                    .append("9C01${transType}")
                    .append("9F0206${bcd}")
                    .append("9A03${yymmdd}")
                    .append("9F2103${hhmmss}")
                    .append("9F4104${sequenceCounter}")
                    .append("9F3501${terminalType}")
                    .append("9F1A02${currencyCode}")
                    .append("5F2A02${currencyCode}")
                    .append("9F3901${entryMode}")
                    .append("9F3303${capability}")
                    .append("9F1B0400003A98")
                    .append("9F660436004000")
                    .append("DF06027C00")
                    .append("DF812406000000100000")
                    .append("DF812306000000100000")
                    .append("DF812606100000100000")
                    .append("DF918165050100000000")
                    .append("DF040102")
                    .append("DF810602C000")
                    .append("9F4C080000000000000000")
                    .append("DF9181040100").toString()

            KernelID.MASTER ->                // Parameter settings, see transaction parameters of PAYPASS in《UEMV develop guide》.
                tlvList = java.lang.StringBuilder()
                    .append("9F3501${terminalType}")
                    .append("9F40056000F0A001")//6000F0A001
                    .append("9F0206${bcd}")
                    .append("9A03${yymmdd}")
                    .append("9F2103${hhmmss}")
                    .append("9F4104${sequenceCounter}")
                    .append("9F1A02${currencyCode}")
                    .append("5F2A02${currencyCode}")
                    .append("9F3901${entryMode}")
                    .append("9F3303${capability}")
                    .append("9C01${transType}")
                    .append("DF918111050000000000")
                    .append("DF91811205FFFFFFFFFF")
                    .append("DF91811005FFFFFFFFFF")
                    .append("DF9182010102")
                    .append("DF9182020100")
                    .append("DF9181150100")
                    .append("DF9182040100")
                    .append("DF812406000000010000")
                    .append("DF812506000000010000")
                    .append("DF812606100000010000")
                    .append("DF812306000000010000")
                    .append("DF9182050160")
                    .append("DF9182060160")
                    .append("DF9182070120")
                    .append("DF9182080120").toString()
            KernelID.AMEX -> {
                emv?.setTLV(KernelID.AMEX,A_TAG_TM_CVM_LIMIT,"")
                tlvList = java.lang.StringBuilder()
                    .append("9C01${transType}")
                    .append("9F0206${bcd}")
                    .append("9A03${yymmdd}")
                    .append("9F2103${hhmmss}")
                    .append("9F4104${sequenceCounter}")
                    .append("9F3501${terminalType}")
                    .append("9F1A02${currencyCode}")
                    .append("5F2A02${currencyCode}")
                    .append("9F3901${entryMode}")
                    .append("9F3303${capability}")
                    .append("9F09020001")
                    .append("9F6D0180")//Amex CTLS Reader cap
                    .append("DF812606111111111111")//CVM limit
                    .append("9F1B0400003A98").toString()
            }
            KernelID.DISCOVER -> {
                tlvList = java.lang.StringBuilder()
                    .append("9C01${transType}")
                    .append("9F0206${bcd}")
                    .append("9A03${yymmdd}")
                    .append("9F2103${hhmmss}")
                    .append("9F4104${sequenceCounter}")
                    .append("9F3501${terminalType}")
                    .append("9F1A02${currencyCode}")
                    .append("5F2A02${currencyCode}")
                    .append("9F3901${entryMode}")
                    .append("9F3303${capability}")
                    .append("DF812606111111111111")//CVM limit
                    .append("9F0106010203040506")
                    .append("9F09020100")
                    .append("9F6604B440C000")
                    .append("9F1B0400003A98").toString()
            }
            KernelID.JCB -> {
                tlvList = java.lang.StringBuilder()
                    .append("9C01${transType}")
                    .append("9F0206${bcd}")
                    .append("9A03${yymmdd}")
                    .append("9F2103${hhmmss}")
                    .append("9F4104${sequenceCounter}")
                    .append("9F3501${terminalType}")
                    .append("9F1A02${currencyCode}")
                    .append("5F2A02${currencyCode}")
                    .append("9F3901${entryMode}")
                    .append("9F3303${capability}")
                    .append("DF91840306111111111111")
                    .append("9F0106000000000010")
                    .append("9F1B0400003A98").toString()
            }
            KernelID.PURE->{
                tlvList = java.lang.StringBuilder()
                    .append("9C01${transType}")
                    .append("9F0206${bcd}")
                    .append("9A03${yymmdd}")
                    .append("9F2103${hhmmss}")
                    .append("9F4104${sequenceCounter}")
                    .append("9F3501${terminalType}")
                    .append("9F1A02${currencyCode}")
                    .append("5F2A02${currencyCode}")
                    .append("9F3901${entryMode}")
                    .append("9F3303${capability}")
                    .append("9F1B0400003A98").toString()
            }
            else -> {
                tlvList = java.lang.StringBuilder()
                    .append("9C01${transType}")
                    .append("9F0206${bcd}")
                    .append("9A03${yymmdd}")
                    .append("9F2103${hhmmss}")
                    .append("9F4104${sequenceCounter}")
                    .append("9F3501${terminalType}")
                    .append("9F1A02${currencyCode}")
                    .append("5F2A02${currencyCode}")
                    .append("9F3901${entryMode}")
                    .append("9F3303${capability}")
                    .append("9F1B0400003A98").toString()
            }
        }
        outputResult(
            emv!!.setTLVList(finalData.kernelID.toInt(), tlvList),
            "...onFinalSelect: setTLVList"
        )
        outputResult(emv!!.respondEvent(null), "...onFinalSelect: respondEvent")
    }

    /**
     * Application to process card record data and set parameters
     * such as display card number, find blacklist, set public key, etc
     */
    @Throws(RemoteException::class)
    fun doReadRecord(record: CardRecord?) {
        Log.d("EMV","=> onReadRecord | " + EMVInfoUtil.getRecordDataDesc(record))
        val tagStr =  "57, 9f06, df79, 4f, 84, 50, 5f34, 9f09, 82, 95, 9f34, 9b,"+
        "9f36, 9f41, 9f37, 9f4C, 9c, 9f35, 9f33, 9f40, 9f1A, 9F42, 9f10, 9f26, 9f27,"+
        "9f66, 9f6e, 9f7c, df78, 5f57, 9f39, 9f53"

//        val tagStr =
//            "4f,50,57, 5a,71, 72, 82,84,8a,8e,  91,  95,9a,  99,  9b , 9c  ,5f20  ,5f24  ,5f28,  5f2a,  5f34 ," +
//                    "5f2d,  9f02 , 9f03 , 9f06 , 9f07,  9f08 , 9f09 , 9f0d , 9f0e , 9f0f,  9f10 , 9f11,  9f12,  9f14, 9f17,  9f1a,  9f1b , 9f1e , 9f1f , 9f21,  9f26  ,  9f27 ,   9f33,  9f34,    9f35,   9f36 ,  9f37," +
//                    "9f39 ,  9f40 , 9f41,  9f42 , 9f53 , 9f5b,  9f5d , 9f67 , 9f6e , 9f71,  9f7c,  df918110,  df918111,  df918112 , df918124,  df30 , df32  ,df34  ,df35  ,df36 , df37 , df38 , df39"
        val tagArray = tagStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val tags: MutableList<String> = ArrayList()
        for (i in tagArray.indices) {
            val t = tagArray[i].trim { it <= ' ' }
            if (!TextUtils.isEmpty(t)) {
                tags.add(t)
            }
        }
        val list: List<TlvResponse> = ArrayList()
        val ret: Int = emv!!.getKernelDataList(tags, list)
        LogUtil.d("getKernelDataList ret = $ret")
        for (i in list.indices) {
            val info = list[i]
            LogUtil.d(
                "i = " + i + ", " + BytesUtil.bytes2HexString(info.tag) + ", ret = " + info.result + ", " + BytesUtil.bytes2HexString(
                    info.value
                )
            )
        }
        outputResult(emv!!.respondEvent(null), "...onReadRecord: respondEvent")
    }

    /**
     * Request the cardholder to perform the Cardholder verification specified by the kernel.
     */
    @Throws(RemoteException::class)
    fun doCardHolderVerify(cvm: CVMMethod) {
        Log.d("EMV","=> onCardHolderVerify | " + EMVInfoUtil.getCVMDataDesc(cvm))
        val param = Bundle()
        param.putByteArray(PinpadData.PIN_LIMIT, byteArrayOf(0, 4, 5, 6, 7, 8, 9, 10, 11, 12))
        val listener: OnPinEntryListener = object : OnPinEntryListener.Stub() {
            override fun onInput(arg0: Int, arg1: Int) {}
            override fun onConfirm(arg0: ByteArray, arg1: Boolean) {
                respondCVMResult(1.toByte())
            }

            override fun onCancel() {
                respondCVMResult(0.toByte())
            }

            override fun onError(error: Int) {
                Log.d("EMV","pinpad Error:${error}")
                respondCVMResult(2.toByte())
            }
        }

        if((cvm.cvm.toInt())== CVMFlag.EMV_CVMFLAG_OFFLINEPIN) {
            val cvmFlagValue: ByteArray =
                BytesUtil.hexString2Bytes(emv?.getTLV(EMVTag.DEF_TAG_CVM_FLAG))
            //Offline ciphertext pin
            if (cvmFlagValue != null && cvmFlagValue.size > 0 && cvmFlagValue[0].toInt() == 0x31) {
                val cslValue: ByteArray = BytesUtil.hexString2Bytes(emv?.getTLV("DF91815D"))
                //The public key recovery fails
                if (cslValue != null && cslValue.size > 1 && cslValue[1].toInt() and 0x40 == 0x40) {
                    // In the case of offline ciphertext pin, if the public key recovery fails, it shall be applied without pop-up encryption window.
                    // Only the modular version of the kernel is supported
                    val chvStatus: TLV =
                        TLV.fromData(EMVTag.DEF_TAG_CHV_STATUS, byteArrayOf(0x01))
                    emv?.respondEvent(chvStatus.toString())
                    return
                }
            }
            pinpad!!.startOfflinePinEntry(param, listener)
        }else if((cvm.cvm.toInt()) == CVMFlag.EMV_CVMFLAG_ONLINEPIN) {
            var bytes =  lastCardRecord?.pan
            if(bytes != null)
                Log.d("EMV","=> onCardHolderVerify | onlinpin${bytes?.let { BytesUtil.bytes2HexString(it) }}")
            param.putByteArray(PinpadData.PAN_BLOCK, lastCardRecord!!.getPan())
            pinpad!!.startPinEntry(IngenicoDeviceConfig.KEYID_PIN, param, listener)
        }else {
            Log.d("EMV","=> onCardHolderVerify | default")
            respondCVMResult(1.toByte())
        }
    }

    fun loadWorkKeys() {
        Log.d("EMV",">>> loadWorkKeys")
        try {
            val keyIds = intArrayOf(KEYID_PIN)//, KEYID_MAC, KEYID_TRACK, KEYID_DES)
            val keyTypes =
                intArrayOf(KeyType.PIN_KEY)//, KeyType.MAC_KEY, KeyType.TDK_KEY, KeyType.DEK_KEY)
            val keys = arrayOf( // value is 24 bytes of 9
                "116817D8855150C9116817D8855150C9116817D8855150C9")//,  // value is 24 bytes of 8
//                "2DE2F089C15D9E992DE2F089C15D9E992DE2F089C15D9E99",  // value is 24 bytes of 7
//                "BDE3888C42CE9DECBDE3888C42CE9DECBDE3888C42CE9DEC",  // value is 24 bytes of 6
//                "A30FE2C1D07BCC11A30FE2C1D07BCC11A30FE2C1D07BCC11"
//            )
            val kcvs = arrayOf(
                "0F2FCF4A")//,
//                "F9F4FBD3",
//                "4CBE91BE",
//                "B0B563C2"
//            )
            for (i in keyIds.indices) {
                val keyType = keyTypes[i]
                val keyId = keyIds[i]
                val keyHandle = KeyHandle(
                    KAPId(IngenicoDeviceConfig.REGION_ID, IngenicoDeviceConfig.KAP_NUM),
                    KeySystem.KS_MKSK,
                    keyId
                )
                val params = Bundle()
                params.putParcelable(PinpadData.SRC_KEY_HANDLE, getMainKeyHandle())
                params.putParcelable(PinpadData.DST_KEY_HANDLE, keyHandle)
                params.putParcelable(PinpadData.KEY_CFG, getKeyCfg(keyType))
                params.putInt(PinpadData.ENC_KEY_FMT, EncKeyFmt.ENC_KEY_FMT_BUNDLE)
                params.putInt(PinpadData.FORMAT, EncKeyBundleFmt.BUNDLE_FMT_ECB)
                params.putByteArray(PinpadData.ENC_KEY, BytesUtil.hexString2Bytes(keys[i]))
                params.putByteArray(PinpadData.CHECK_VALUE, BytesUtil.hexString2Bytes(kcvs[i]))
                val isSucc = pinpad!!.loadEncKeyWithBundle(params)
                if (isSucc) {
                    Log.d("EMV",
                        String.format(
                            "loadEncKeyWithBundle(keyType = %s, keyId = %s) success",
                            keyType,
                            keyId
                        )
                    )
                } else {
                    Log.e("EMV",
                        String.format(
                            "loadEncKeyWithBundle(keyType = %s, keyId = %s) fail",
                            keyType,
                            keyId
                        )
                    )
                }
            }
        } catch (e: RemoteException) {
            Log.d("EMV","RemoteException: " + e.message)
        }
    }


    fun loadMainKey() {
        try {
            pinpadLimited = PinpadLimited(activity,
                KAPId(IngenicoDeviceConfig.REGION_ID,
                IngenicoDeviceConfig.KAP_NUM),
                KeySystem.KS_MKSK,
                IngenicoDeviceConfig.PINPAD_DEVICE_NAME)
            Log.d("EMV",">>> getKapMode")
            val kapMode = IntValue()
            val isSucc = pinpad!!.getKapMode(kapMode)
            if (isSucc) {
                Log.d("EMV","getKapMode success[0 - LPTK_MODE; 1 - WORK_MODE]: " + kapMode.data)
            } else {
                Log.d("EMV","getKapMode fail")
                return
            }
            val key = "111111111111111111111111111111111111111111111111"
            loadPlaintMainKey(key, KeyAlgorithm.KA_TDEA)
        } catch (e: RemoteException) {
            Log.d("EMV","RemoteException: " + e.message)
        }
    }

    fun createPinpad(kapId: KAPId?, keySystem: Int, deviceName: String?): UPinpad? {
        return try {
            pinpadLimited = PinpadLimited(activity, kapId, keySystem, deviceName)
            getPinpad(kapId, keySystem, deviceName)
        } catch (e: RemoteException) {
            e.printStackTrace()
            null
        }
    }
    @Throws(RemoteException::class)
    private fun loadPlaintMainKey(key: String, algorithm: Char) {
        //            if (kapMode.getData() != 0) {
        Log.d("EMV",">>> format")
        var isSucc: Boolean = pinpadLimited!!.format()
        if (isSucc) {
            Log.d("EMV","format success")
        } else {
            Log.d("EMV","format fail")
            return
        }
        //            }
        Log.d("EMV",">>> loadPlainTextKey")
        val keyId = KEYID_MAIN
        pinpad!!.setKeyAlgorithm(algorithm)
        isSucc =
            pinpadLimited!!.loadPlainTextKey(KeyType.MAIN_KEY, keyId, BytesUtil.hexString2Bytes(key))
        if (isSucc) {
            Log.d("EMV",String.format("loadPlainTextKey(MAIN_KEY, keyId = %s) success", keyId))
        } else {
            Log.d("EMV","loadPlainTextKey fail")
            return
        }
        Log.d("EMV",">>> switchToWorkMode")
        isSucc = pinpadLimited!!.switchToWorkMode()
        if (isSucc) {
            Log.d("EMV","switchToWorkMode success")
        } else {
            Log.d("EMV","switchToWorkMode fail")
        }
    }

    private fun getMainKeyHandle(): KeyHandle? {
        return KeyHandle(
            KAPId(IngenicoDeviceConfig.REGION_ID, IngenicoDeviceConfig.KAP_NUM),
            getKeySystem(),
            KEYID_MAIN
        )
    }
    protected val KEYID_MAIN = 0
    fun getKeySystem(): Int {
        return KeySystem.KS_MKSK
    }

    protected fun getKeyCfg(keyType: Int): KeyCfg? {
        when (keyType) {
            KeyType.PIN_KEY -> return KeyCfg(
                KeyUsage.KU_PIN_ENCRYPTION,
                KeyAlgorithm.KA_TDEA,
                KeyModeOfUse.MOU_ENC_OR_WRAP_ONLY,
                KeyVersionNumber.KVN_NOT_USED, KeyExportability.KE_NON_EXPORTABLE.code.toByte()
            )
            KeyType.MAC_KEY -> return KeyCfg(
                KeyUsage.KU_ISO_9797_1_MAC_ALGORITHM_3,
                KeyAlgorithm.KA_TDEA,
                KeyModeOfUse.MOU_GENERATE_AND_VERIFY,
                KeyVersionNumber.KVN_NOT_USED, KeyExportability.KE_NON_EXPORTABLE.code.toByte()
            )
            KeyType.TDK_KEY -> return KeyCfg(
                KeyUsage.KU_TRACK_DATA_ENCRYPTION,
                KeyAlgorithm.KA_TDEA,
                KeyModeOfUse.MOU_ENC_OR_WRAP_ONLY,
                KeyVersionNumber.KVN_NOT_USED, KeyExportability.KE_NON_EXPORTABLE.code.toByte()
            )
            KeyType.DEK_KEY -> return KeyCfg(
                KeyUsage.KU_DATA_ENCRYPTION,
                KeyAlgorithm.KA_TDEA,
                KeyModeOfUse.MOU_ENC_DEC_WRAP_UNWRAP,
                KeyVersionNumber.KVN_NOT_USED, KeyExportability.KE_NON_EXPORTABLE.code.toByte()
            )
        }
        return null
    }


    fun respondCVMResult(result: Byte) {
        try {
            if(emv==null)
                return
            val chvStatus: TLV = TLV.fromData(EMVTag.DEF_TAG_CHV_STATUS, byteArrayOf(result))
            val ret: Int = emv!!.respondEvent(chvStatus.toString())
            outputResult(ret, "...onCardHolderVerify: respondEvent")
        } catch (e: java.lang.Exception) {
            handleException(e)
        }
    }

    /**
     * Request the application to execute online authorization.
     */
    @Throws(RemoteException::class)
    fun doOnlineProcess(transData: TransData) {
        Log.d("EMV","=> onOnlineProcess | TLVData for online:" + BytesUtil.bytes2HexString(transData.tlvData))
        Log.d("EMV","=>${lastCardRecord?.flowType}")




        val onlineResult = doOnlineProcess()
        if(emv == null)
            return
        val ret: Int = emv!!.respondEvent(onlineResult)
        outputResult(ret, "...onOnlineProcess: respondEvent")
    }

    fun processOnlineResponse(status: Boolean){
        this@IngenicoSDK.processOnlineResponse = true
        onlineSuccess = status
    }

    /**
     * pack message, communicate with server, analyze server response message.
     *
     * @return result of online process，he data elements are as follows:
     * DEF_TAG_ONLINE_STATUS (M)
     * If online communication is success, following is necessary while retured by host service.
     * EMV_TAG_TM_ARC (C)
     * DEF_TAG_AUTHORIZE_FLAG (C)
     * EMV_TAG_TM_AUTHCODE (C)
     * DEF_TAG_HOST_TLVDATA (C)
     */
    private fun doOnlineProcess(): String {
        Log.d("EMV","****** doOnlineProcess ******")
        Log.d("EMV","... ...")
        Log.d("EMV","... ...")

        /***************************/

        val tagStr =  "57, 9f06, df79, 4f, 84, 50, 5f34, 9f09, 82, 95, 9f34, 9b,"+
                "9f36, 9f41, 9f37, 9f4C, 9c, 9f35, 9f33, 9f40, 9f1A, 9F42, 9f10, 9f26, 9f27,"+
                "9f66, 9f6e, 9f7c, df78, 5f57, 9f39, 9f53"

//        val tagStr =
//            "4f,50,57, 5a,71, 72, 82,84,8a,8e,  91,  95,9a,  99,  9b , 9c  ,5f20  ,5f24  ,5f28,  5f2a,  5f34 ," +
//                    "5f2d,  9f02 , 9f03 , 9f06 , 9f07,  9f08 , 9f09 , 9f0d , 9f0e , 9f0f,  9f10 , 9f11,  9f12,  9f14, 9f17,  9f1a,  9f1b , 9f1e , 9f1f , 9f21,  9f26  ,  9f27 ,   9f33,  9f34,    9f35,   9f36 ,  9f37," +
//                    "9f39 ,  9f40 , 9f41,  9f42 , 9f53 , 9f5b,  9f5d , 9f67 , 9f6e , 9f71,  9f7c,  df918110,  df918111,  df918112 , df918124,  df30 , df32  ,df34  ,df35  ,df36 , df37 , df38 , df39"
        val tagArray = tagStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val tags: MutableList<String> = ArrayList()
        for (i in tagArray.indices) {
            val t = tagArray[i].trim { it <= ' ' }
            if (!TextUtils.isEmpty(t)) {
                tags.add(t)
            }
        }
        val list: List<TlvResponse> = ArrayList()
        val retriveList: Int = emv!!.getKernelDataList(tags, list)
        LogUtil.d("getKernelDataList ret = $retriveList")
        val paramMap = LinkedHashMap<String,String?>()
        for (i in list.indices) {
            val info = list[i]
            LogUtil.d(
                "i = " + i + ", " + BytesUtil.bytes2HexString(info.tag) + ", ret = " + info.result + ", " + BytesUtil.bytes2HexString(
                    info.value
                )
            )
            if(info.result == 0){
                paramMap.put(BytesUtil.bytes2HexString(info.tag),BytesUtil.bytes2HexString(info.value))
            }
        }


        LogUtil.d("EMV_TAG_IC_ICCDYNNUM  = ${emv?.getTLV(EMV_TAG_IC_ICCDYNNUM)}")
        /***************************/
//        ingenicoSDKCB.onOnlineProcess(paramMap)
        if (paramMap.size == 0) {//XStr.isEmpty(encryptedData) ||
//            mUserActivity.snack("Transaction Failed")
            ingenicoSDKCB.onError("Transaction Failed")
        } else {
            tagParams = paramMap
            ingenicoSDKCB.onOnlineProcess("")
                //mUserActivity?.snack("Please remove card, Transaction request send ..")

//            apiInit_4V2_Sale(encryptedData = encryptedData, params = params)
        }



        var counter = 0
        // wait for online response processOnlineResponse()
        while (!processOnlineResponse){
            counter+=200
            Thread.sleep(200)
            if(counter == 60000)
                processOnlineResponse = true
        }

        return if (onlineSuccess) {
            val onlineResult = StringBuffer()
            onlineResult.append(EMVTag.DEF_TAG_ONLINE_STATUS).append("01").append("00")
            val hostRespCode = "3030"
            onlineResult.append(EMVTag.EMV_TAG_TM_ARC).append("02").append(hostRespCode)
            val onlineApproved = true
            onlineResult.append(EMVTag.DEF_TAG_AUTHORIZE_FLAG).append("01")
                .append(if (onlineApproved) "01" else "00")
            val hostTlvData =
                "9F3501229C01009F3303E0F1C89F02060000000000019F03060000000000009F101307010103A0A802010A010000000052856E2C9B9F2701809F260820F63D6E515BD2CC9505008004E8009F1A0201565F2A0201569F360201C982027C009F34034203009F37045D5F084B9A031710249F1E0835303530343230308408A0000003330101019F090200309F410400000001"
            onlineResult.append(
                TLV.fromData(
                    EMVTag.DEF_TAG_HOST_TLVDATA,
                    BytesUtil.hexString2Bytes(hostTlvData)
                ).toString()
            )
            onlineResult.toString()
        } else {
            Log.d("EMV","!!! online failed !!!")
            "DF9181090101"
        }
    }



    fun doVerifyOfflinePin(
        flag: Int,
        random: ByteArray?,
        capKey: CAPublicKey?,
        result: OfflinePinVerifyResult
    ) {
        Log.d("EMV","=> onVerifyOfflinePin")
        try {
            /** inside insert card - 0；inside swing card – 6；External device is connected to the USB port - 7；External device is connected to the COM port -8  */
            val icToken = 0
            //Specify the type of "PIN check APDU message" that will be sent to the IC card.Currently only support VCF_DEFAULT.
            val cmdFmt = OfflinePinVerify.VCF_DEFAULT
            val offlinePinVerify = OfflinePinVerify(flag.toByte(), icToken, cmdFmt, random)
            val pinVerifyResult = PinVerifyResult()
            val ret: Boolean =
                pinpad!!.verifyOfflinePin(offlinePinVerify, getPinPublicKey(capKey), pinVerifyResult)
            if (!ret) {
                Log.d("EMV","verifyOfflinePin fail: " + pinpad!!.getLastError())
                stopEMV()
                return
            }
            val apduRet = pinVerifyResult.apduRet
            val sw1 = pinVerifyResult.sW1
            val sw2 = pinVerifyResult.sW2
            result.setSW(sw1.toInt(), sw2.toInt())
            result.result = apduRet.toInt()
        } catch (e: java.lang.Exception) {
            handleException(e)
        }
    }

    /**
     * Inform the application that the EMV transaction is completed and the kernel exits.
     */
    fun doEndProcess(result: Int, transData: TransData?) {
        if (result != EMVError.SUCCESS) {
            Log.d("EMV","=> onEndProcess | " + EMVInfoUtil.getErrorMessage(result))
            ingenicoSDKCB.onEndProcess(result,EMVInfoUtil.getErrorMessage(result))
        } else {
            Log.d("EMV",
                "=> onEndProcess | EMV_RESULT_NORMAL | " + EMVInfoUtil.getTransDataDesc(
                    transData
                )
            )
            if(mCardType != 2)
                ingenicoSDKCB.onEndProcess(result,EMVInfoUtil.getErrorMessage(result))
            else
                fetchTagAndProcess()
        }
//        outputText("\n")
    }

    private fun fetchTagAndProcess() {
        Log.d("EMV","... ...")
        Log.d("EMV","... ...")

        /***************************/

        val tagStr =  "57, 9f06, df79, 4f, 84, 50, 5f34, 9f09, 82, 95, 9f34, 9b,"+
                "9f36, 9f41, 9f37, 9f4C, 9c, 9f35, 9f33, 9f40, 9f1A, 9F42, 9f10, 9f26, 9f27,"+
                "9f66, 9f6e, 9f7c, df78, 5f57, 9f39, 9f53"

//        val tagStr =
//            "4f,50,57, 5a,71, 72, 82,84,8a,8e,  91,  95,9a,  99,  9b , 9c  ,5f20  ,5f24  ,5f28,  5f2a,  5f34 ," +
//                    "5f2d,  9f02 , 9f03 , 9f06 , 9f07,  9f08 , 9f09 , 9f0d , 9f0e , 9f0f,  9f10 , 9f11,  9f12,  9f14, 9f17,  9f1a,  9f1b , 9f1e , 9f1f , 9f21,  9f26  ,  9f27 ,   9f33,  9f34,    9f35,   9f36 ,  9f37," +
//                    "9f39 ,  9f40 , 9f41,  9f42 , 9f53 , 9f5b,  9f5d , 9f67 , 9f6e , 9f71,  9f7c,  df918110,  df918111,  df918112 , df918124,  df30 , df32  ,df34  ,df35  ,df36 , df37 , df38 , df39"
        val tagArray = tagStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val tags: MutableList<String> = ArrayList()
        for (i in tagArray.indices) {
            val t = tagArray[i].trim { it <= ' ' }
            if (!TextUtils.isEmpty(t)) {
                tags.add(t)
            }
        }
        val list: List<TlvResponse> = ArrayList()
        val retriveList: Int = emv!!.getKernelDataList(tags, list)
        LogUtil.d("getKernelDataList ret = $retriveList")
        val paramMap = LinkedHashMap<String,String?>()
        for (i in list.indices) {
            val info = list[i]
            LogUtil.d(
                "i = " + i + ", " + BytesUtil.bytes2HexString(info.tag) + ", ret = " + info.result + ", " + BytesUtil.bytes2HexString(
                    info.value
                )
            )
            if(info.result == 0){
                paramMap.put(BytesUtil.bytes2HexString(info.tag),BytesUtil.bytes2HexString(info.value))
            }
        }
        ingenicoSDKCB.onOnlineProcess("Please remove card, Transaction request send ..")

    }

    fun doSendOut(ins: Int, data: ByteArray) {
        when (ins) {
            KernelINS.DISPLAY ->            // DisplayMsg: MsgID（1 byte） + Currency（1 byte）+ DataLen（1 byte） + Data（30 bytes）
                if (data[0].toInt() == MessageID.ICC_ACCOUNT) {
                    val len = data[2].toInt()
                    val account: ByteArray = BytesUtil.subBytes(data, 1 + 1 + 1, len)
                    val accTLVList: TLVList = TLVList.fromBinary(account)
                    val track2: String =
                        BytesUtil.bytes2HexString(accTLVList.getTLV("57").getBytesValue())
                    Log.d("EMV","=> onSendOut | track2 = $track2")
                }
            KernelINS.DBLOG -> {
                var i = data.size - 1
                while (i >= 0) {
                    if (data[i].toInt() == 0x00) {
                        data[i] = 0x20
                    }
                    i--
                }
                Log.d("DBLOG", String(data))
            }
            KernelINS.CLOSE_RF -> {
                Log.d("EMV","=> onSendOut: Notify the application to halt contactless module")
                halt()
            }
            else -> Log.d("EMV",
                "=> onSendOut: instruction is 0x" + Integer.toHexString(ins) + ", data is " + BytesUtil.bytes2HexString(
                    data
                )
            )
        }
    }

    fun outputResult(ret: Int, stepName: String) {
        Log.d("outputResult","ret:${ret}  step:${stepName}")
        /* when (ret) {
             EMVError.SUCCESS -> //outputBlackText("$stepName success")
             EMVError.REQUEST_EXCEPTION -> //outputRedText("$stepName fail: register yet?")
             EMVError.SERVICE_CRASH -> //outputRedText("$stepName fail: masterContol service crash")
             else -> //outputRedText(String.format("$stepName fail[0x%02X]", ret))
         }*/
    }

    fun getPinPublicKey(from: CAPublicKey?): PinPublicKey? {
        if (from == null) {
            return null
        }
        val to = PinPublicKey()
        to.mRid = from.rid
        to.mExp = from.exp
        to.mExpiredDate = from.expDate
        to.mHash = from.hash
        to.mHasHash = from.hashFlag
        to.mIndex = from.index
        to.mMod = from.mod
        return to
    }


    fun handleException(e: java.lang.Exception) {
        e.printStackTrace()
//        showException(e.javaClass.simpleName + " : " + e.message)
        ingenicoSDKCB.showException(e.javaClass.simpleName + " : " + e.message)
    }



    abstract inner class IBinderCreator {
        @Throws(java.lang.IllegalStateException::class)
        fun start(): IBinder {
            if (deviceService == null) {
                bindService()
                throw java.lang.IllegalStateException("Servic unbound,please retry latter!")
            }
            return try {
                create()
            } catch (e: DeadObjectException) {
                deviceService = null
                throw java.lang.IllegalStateException("Service process has stopped,please retry latter!")
            } catch (e: RemoteException) {
                throw java.lang.IllegalStateException(e.message, e)
            } catch (e: SecurityException) {
                throw java.lang.IllegalStateException(e.message, e)
            }
        }

        @Throws(RemoteException::class)
        abstract fun create(): IBinder
    }

    fun initSaleRequest(mvm: MTerminalVm) {
        if(tagParams == null)
            ingenicoSDKCB.onError("Card information error")

        var instrumentType = "01"
        if(tagParams?.containsKey("9F39") == true){
            instrumentType = tagParams!!.get("9F39")?:"01"
        }
        mvm.processSaleRequest(amount.toDouble(),instrumentType.toInt(),null,null,tagParams)
    }

    fun initMSRSaleRequest(mvm: MTerminalVm) {
        val entryTypeMSR = 4
        tagParams?.let { mvm.processSaleRequest(amount.toDouble(),entryTypeMSR,track2Data,"", it) }
    }


}