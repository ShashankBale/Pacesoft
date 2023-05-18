package x.code.util.log

import android.util.Log
import android.view.Gravity
import android.widget.Toast
import x.code.BuildConfig
import x.code.app.XCodeApp
import x.code.util.view.text.XStr

fun delog(t: String?, m: String) {
    if(BuildConfig.DEBUG) {
        var t = t
        var m = m
        t = checkIsEmpty(t)
        m = checkIsEmpty(m)
        Log.e("" + t, "" + m)
    }
}

fun elog(t: String?, m: String) {
    var t = t
    var m = m
    t = checkIsEmpty(t)
    m = checkIsEmpty(m)
    Log.e("" + t, "" + m)
}

fun elog(m: String) {
    var m = m
    m = checkIsEmpty(m)
    Log.e("#######", "" + m)
}

fun vlog(t: String?, m: String) {
    var t = t
    var m = m
    t = checkIsEmpty(t)
    m = checkIsEmpty(m)
    Log.v("" + t, "" + m)
}

fun vlog(m: String) {
    var m = m
    m = checkIsEmpty(m)
    Log.v("#######", "" + m)
}

fun dlog(t: String?, m: String) {
    var t = t
    var m = m
    t = checkIsEmpty(t)
    m = checkIsEmpty(m)
    Log.d("" + t, "" + m)
}

fun dlog(m: String) {
    var m = m
    m = checkIsEmpty(m)
    Log.d("#######", "" + m)
}

fun mammothLog(TAG: String?, sb: String) {
    if (sb.length > 4000) {
        elog(TAG, "sb.length = " + sb.length)
        val chunkCount = sb.length / 4000 // integer division
        for (i in 0..chunkCount) {
            val max = 4000 * (i + 1)
            if (max >= sb.length) {
                elog(
                    TAG,
                    "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i)
                )
            } else {
                elog(
                    TAG,
                    "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i, max)
                )
            }
        }
    } else {
        Log.v(TAG, sb)
    }
}

fun checkIsEmpty(data: String?): String {
    return if (XStr.isEmpty(data)) "++IsEmptyOrNull++"
    else data!!
}

/**
 * String Resource Value for Toast
 *
 * @param iRes R.string.string_key value
 */
fun toast(iRes: Int) {
    try {
        val t = Toast.makeText(
            XCodeApp.app,
            iRes,
            Toast.LENGTH_SHORT
        )
        //t.setGravity(Gravity.BOTTOM | Gravity.RIGHT, 0, 0);
        t.show()
    } catch (e: Exception) {
        e.printStackTrace()
    } catch (e: Error) {
        //Toast wont be show, because already tried it, otherwise with will go in infinite loop
    }
}

/**
 * String Resource Value for Toast used by Developer Debug
 *
 * @param iRes R.string.string_key value
 */
fun dtoast(iRes: Int) {
    try {
        if (BuildConfig.DEBUG) toast(iRes)
    } catch (e: Exception) {
        e.printStackTrace()
    } catch (e: Error) {
        //Toast wont be show, because already tried it, otherwise with will go in infinite loop
    }
}

/**
 * Hardcode String value for Toast
 *
 * @param str hardcode string value
 */
fun toast(str: String, isShort: Boolean = false) {
    try {
        val t = Toast.makeText(
            XCodeApp.app,
            "" + str,
            if (isShort) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )
        //t.setGravity(Gravity.BOTTOM | Gravity.RIGHT, 0, 0);
        t.show()
    } catch (e: Exception) {
        e.printStackTrace()
    } catch (e: Error) {
        //Toast wont be show, because already tried it, otherwise with will go in infinite loop
    }
}

/**
 * Hardcode String value for Toast used by Developer Debug
 *
 * @param str hardcode string value
 */
fun dtoast(str: String) {
    try {
        if (BuildConfig.DEBUG) {
            try {
                val t = Toast.makeText(XCodeApp.app, "$str", Toast.LENGTH_SHORT)
                t.setGravity(Gravity.TOP, 0, 0);
                t.show()
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (e: Error) {
                //Toast wont be show, because already tried it, otherwise with will go in infinite loop
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } catch (e: Error) {
        //Toast wont be show, because already tried it, otherwise with will go in infinite loop
    }
}
