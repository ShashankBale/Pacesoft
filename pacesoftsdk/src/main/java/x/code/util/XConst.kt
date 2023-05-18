package x.code.util

object XConst {
    const val BEACON_REFRESH_TIME = 5000L
    const val DEBOUNCE_TIMEOUT = 700L
    const val THROTTLE_CLICK = 700L
    const val THROTTLE_CLICK_LONG = 1400L
    const val resendOtpTimerInMin: Long = 60 * 2 //Sec * Min
    const val terminalCardReaderTimerInSec: Long = 15 //30 sec

    //Hr * Minute * Seconds * Milliseconds
    const val HEARTBEAT_REFRESH_INTERVAL_TIMER = 1 * 60 * 60 * 1000L
    const val BEACON_REFRESH_TIME_SEC = 5L

    const val MAX_CRYPTO_KEYS_DAYS_OF_EXPIRY: Int = 30
    const val API_HEADER_TAG_API_KEY = "APIKEY"
    const val API_HEADER_TAG_IV = "IV"
    const val API_HEADER_TAG_TEMP_DK = "TEMP_DK"
    const val API_HEADER_TAG_TEMP_TK = "TEMP_TK"

    //REQUEST CODE
    const val RQ_CAMERA = 1
    const val RQ_CONTACT = 2

    const val RC_REQUEST_SPEECH_INPUT = 101
    const val RC_REQUEST_PLAY_STORE_IN_APP_UPDATE = 102
    const val RC_REQUEST_WEB_BROWSER_RETURN_URL = 103
    const val RC_REQUEST_ID_MULTIPLE_PERMISSIONS: Int = 104
    const val KEY_REQUEST_CODE_SETTINGS: Int = 105

    const val dummyMerchant1: String = "Your Merchant Name"
    const val errorMsgKey: String = "Message"

    const val NOTIFICATION_HEARTBEAT_CHANNEL_ID = "10001"
    const val NOTIFICATION_HEARTBEAT_CHANNEL_NAME = "Important Service"
    const val NOTIFICATION_HEARTBEAT_CHANNEL_DESC = "Pacesoft Service notifications channel"


    const val NOTIFICATION_HEARTBEAT_ID = 1001
    const val NOTIFICATION_HEARTBEAT_TITLE = "Pacesoft Service"
    const val NOTIFICATION_HEARTBEAT_TEXT = "Device Heartbeat Service"
}