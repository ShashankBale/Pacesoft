package x.code.util.hardware

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object XVibrate {

    const val _20 = 20L
    const val SHORT = 100L
    const val MEDIUM = 200L
    const val LONG = 300L

    fun vibrate(activity : Activity, milliseconds: Long) {
        val vibrator: Vibrator =
            activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    milliseconds,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(milliseconds)
        }
    }

}