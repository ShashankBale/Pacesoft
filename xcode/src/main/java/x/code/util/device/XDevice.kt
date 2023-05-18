package x.code.util.device

import android.provider.Settings.Secure
import x.code.app.XCodeApp


object XDevice {
    val androidId: String by lazy { Secure.getString(XCodeApp.app.contentResolver, Secure.ANDROID_ID) }

    val manufacturer : String by lazy { android.os.Build.MANUFACTURER }

    val model : String by lazy { android.os.Build.MODEL }

    val osVer : String by lazy { android.os.Build.VERSION.SDK_INT.toString() }

}