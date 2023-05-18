package x.code.util.view.text

import android.app.Activity
import android.content.res.Resources
import android.text.TextUtils
import androidx.core.text.HtmlCompat
import x.code.util.number.Numb
import java.math.RoundingMode
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs


object XStr {
    var csUtf8 = charset("UTF-8")

    //var locale = Locale("en", "IN")
    val locale = Locale.US
    val rupee = "₹"
    val dollar = "$"


    //private val numbFormat = NumberFormat.getNumberInstance(locale)
    private val numbFormat = NumberFormat.getNumberInstance(Locale.US)

    private const val mdPattern2 = "#,###,##0.00"
    private const val mdPattern4 = "#,###,##0.0000"

    private val m2DecimalFormat = DecimalFormat(mdPattern2)
    private val m4DecimalFormat = DecimalFormat(mdPattern4)

    private val decimalFormat4Int = DecimalFormat.getNumberInstance(locale)
    private val decimalFormat4Float: DecimalFormat =
        NumberFormat.getCurrencyInstance(locale) as DecimalFormat

    private val _2DecimalFormatCeil by lazy {
        val df = DecimalFormat(mdPattern2)
        df.roundingMode = RoundingMode.CEILING
        df
    }

    private val _2DecimalFormatFloor by lazy {
        val df = DecimalFormat(mdPattern2)
        df.roundingMode = RoundingMode.FLOOR
        df
    }

    init {
        val symbols: DecimalFormatSymbols = decimalFormat4Float.decimalFormatSymbols
        // Don't use null for currencySymbol otherwise it will give exception
        // and we want to remove "Rs" symbol, therefore we have to use this.
        symbols.currencySymbol = ""
        decimalFormat4Float.decimalFormatSymbols = symbols
    }

    fun isEmpty(str: String?): Boolean {
        try {
            if (str == null) return true
            return str.trim().isEmpty()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun isNotEmpty(str: String?): Boolean {
        return !isEmpty(str)
    }

    fun getOrDef(str: String?, defaultStr: String): String {
        return if (str == null || isEmpty(str)) defaultStr else str
    }


    fun capitalize(s: String?): String {
        try {
            if (s == null || s.isEmpty()) {
                return ""
            }
            val first = s[0]
            return if (Character.isUpperCase(first)) {
                s
            } else {
                Character.toUpperCase(first).toString() + s.substring(1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


    fun capitalizeWords1(str: String): String {
        var str = str
        str = str.toLowerCase(locale)
        val words: Array<String> = str.split("\\s".toRegex()).toTypedArray()
        var capitalizeWord = ""
        for (w in words) {
            val first = w.substring(0, 1)
            val afterFirst = w.substring(1)
            capitalizeWord += first.toUpperCase(locale) + afterFirst + " "
        }
        return capitalizeWord.trim { it <= ' ' }
    }

    fun capitalizeWords2(str: String): String {
        return str.split(" ").map { it.toLowerCase(locale).capitalize(locale) }.joinToString(" ")
    }

    fun capitalizeWordsForName(str: String): String {
        return str.split(" ").map { it.toLowerCase(locale).capitalize(locale) }.joinToString(" ")
    }


    fun getFirstLetters(ogText: String): String {
        var text = ogText
        try {
            return if (!TextUtils.isEmpty(text)) {
                var firstLetters = ""
                text = text.replace("[.,]".toRegex(), "") // Replace dots, etc (optional)
                for (s in text.split(" ".toRegex()).toTypedArray()) {
                    firstLetters += s[0]
                }
                firstLetters
            } else ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


    fun format(
        resource: android.content.res.Resources,
        strRes: Int,
        args: Any,
    ): String {
        return String.format(resource.getString(strRes), args)
    }


    fun cInt(input: Int): String = numbFormat.format(input).trim()
    fun cLng(input: Long): String = numbFormat.format(input).trim()

    fun cFloat(input: Float): String = decimalFormat4Int.format(input).trim()
    fun cDbl(input: Double): String = decimalFormat4Float.format(input).trim()

    //without common only decimal
    fun frac2(value: Float) = String.format("%.2f", value)
    fun frac2(input: Double) = String.format("%.2f", input)

    fun frac4(value: Float) = String.format("%.4f", value)
    fun frac4(decimals: Double) = String.format("%.4f", decimals)


    //cf = comma fractional number
    fun cf2Ceil(value: Float) = _2DecimalFormatCeil.format(value)
    fun cf2Ceil(value: Double) = _2DecimalFormatCeil.format(value)

    fun cf2Floor(value: Float) = _2DecimalFormatFloor.format(value)
    fun cf2Floor(value: Double) = _2DecimalFormatFloor.format(value)

    fun cf2(value: Float) = m2DecimalFormat.format(value)
    fun cf2(value: Double) = m2DecimalFormat.format(value)

    fun cf4(value: Float) = m4DecimalFormat.format(value)
    fun cf4(value: Double) = m4DecimalFormat.format(value)

    fun twoDigitFormat(value: Long) = String.format("%02d", value)


    // Condense = to make more dense, compact or to reduce to a shorter form.

    fun condenseNumb(valuePara: Double, isKShow: Boolean = false): String {
        val value = Numb.numbOrZero(valuePara)
        val absValue = abs(value)
        return when {
            absValue > 99_99_999 -> cf2(value / 1_00_00_000) + " Cr."
            absValue > 99_999 -> cf2(value / 1_00_000) + " L"
            isKShow && absValue > 999 -> cf2(value / 1_000) + " K"
            else -> cf2(value)
        }
    }

    fun condenseNumb(valuePara: Float, isKShow: Boolean = false): String {
        val value = Numb.numbOrZero(valuePara)
        val absValue = abs(value)
        return when {
            absValue > 99_99_999 -> cf2(value / 1_00_00_000) + " Cr."
            absValue > 99_999 -> cf2(value / 1_00_000) + " L"
            isKShow && absValue > 999 -> cf2(value / 1_000) + " K"
            else -> cf2(value)
        }
    }

    fun condenseNumbCeil(valuePara: Double, isKShow: Boolean = false): String {
        val value = Numb.numbOrZero(valuePara)
        val absValue = abs(value)
        return when {
            absValue > 99_99_999 -> cf2Ceil(value / 1_00_00_000) + " Cr."
            absValue > 99_999 -> cf2Ceil(value / 1_00_000) + " L"
            isKShow && absValue > 999 -> cf2Ceil(value / 1_000) + " K"
            else -> cf2Ceil(value)
        }
    }

    fun condenseNumbFloor(valuePara: Double, isKShow: Boolean = false): String {
        val value = Numb.numbOrZero(valuePara)
        val absValue = abs(value)
        return when {
            absValue > 99_99_999 -> cf2Floor(value / 1_00_00_000) + " Cr."
            absValue > 99_999 -> cf2Floor(value / 1_00_000) + " L"
            isKShow && absValue > 999 -> cf2Floor(value / 1_000) + " K"
            else -> cf2Floor(value)
        }
    }

    fun getStartMasked(strOg: String, until: Int = 4): String {
        if (strOg.length >= until) {
            val str = strOg.substring(strOg.length - until, strOg.length)
            return "*$str"
        }
        return strOg
    }


    fun getErrMsg(errorMsg: String?, lineNumber: Int, resource: Resources): String {
        val err = when (errorMsg) {
            null -> "#$lineNumber"
            else -> "$errorMsg#$lineNumber"
        }

        //return String.format(resource.getString(R.string.ssww_2_withMsg), err)
        return err
    }

    fun hashSha256(text: String): String {
        val bytes = text.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    fun getUnderlineText(txt: String): CharSequence {
        return HtmlCompat.fromHtml("<u>$txt</u>", HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    fun getTimeAgo(
        strInputDate: String,
        df: java.text.SimpleDateFormat,
        suffix: String = "ago",
    ): String {
        try {
            val inputDate: Date = df.parse(strInputDate) ?: return strInputDate

            val pasTime: Long = inputDate.time
            val nowTime: Long = Calendar.getInstance().timeInMillis

            val timeDiff = nowTime - pasTime
            val second = TimeUnit.MILLISECONDS.toSeconds(timeDiff)

            if (second < 60) return "$second Seconds $suffix"
            val minute = TimeUnit.MILLISECONDS.toMinutes(timeDiff)
            if (minute < 60) return "$minute Minutes $suffix"

            val hour = TimeUnit.MILLISECONDS.toHours(timeDiff)
            if (hour < 24) return "$hour Hours $suffix"

            val day = TimeUnit.MILLISECONDS.toDays(timeDiff)

            return if (day < 7) "$day Days $suffix"
            else {
                when {
                    day > 360 -> (day / 360).toString() + " Years " + suffix
                    day > 30 -> (day / 30).toString() + " Months " + suffix
                    else -> (day / 7).toString() + " Week " + suffix
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return strInputDate
        }
    }

    fun convertSecToMin(pMinutes: Long): String {
        val mins = pMinutes / 60
        val secs = pMinutes % 60
        val suffix = if(mins <= 1L) "min" else "mins"
        return "${twoDigitFormat(mins)}:${twoDigitFormat(secs)} $suffix"
    }


    fun getStringByLocal(context: Activity, resId: Int, locale: Locale): String? {
        val configuration = android.content.res.Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration).resources.getString(resId)
    }

    fun getRandomString(length: Int): String {
        val allowedChars =
            ('A'..'Z') + ('a'..'z') + ('0'..'9') + ('@') + ('!') + ('#') + ('$') + ('&')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun getOnlyAlphabets(str: String?): String? {
        try {
            str ?: return null
            val re = Regex("[^A-Za-z]+")
            return re.replace(str, "")
        } catch (e: Exception) {
            return null
        }
    }

    fun strList(al: List<*>): String {
        val sb = java.lang.StringBuilder()
        al.forEachIndexed { i, it ->
            sb.append("[$i] -> $it")
            sb.append("\n")
        }
        return sb.toString()
    }
}