package x.code.util.number


class Numb {
    companion object {
        fun parseInt(s: Char?, iDefault: Int = -1): Int {
            try {
                if (s == null) return iDefault
                return s.toInt()
            } catch (e: Exception) {
                return iDefault
            }
        }

        fun parseInt(s: String?, iDefault: Int = -1): Int {
            try {
                if (s == null) return iDefault
                return s.trim().toInt()
            } catch (e: Exception) {
                return iDefault
            }
        }

        fun parseInt2(s: String?, iDefault: Int? = null): Int? {
            try {
                if (s == null) return iDefault
                return s.trim().toInt()
            } catch (e: Exception) {
                return iDefault
            }
        }

        fun parseLong(s: String?, iDefault: Long = -1): Long {
            try {
                if (s == null) return iDefault
                return s.trim().toLong()
            } catch (e: Exception) {
                return iDefault
            }
        }

        fun parseLong2(s: String?, iDefault: Long? = null): Long? {
            try {
                if (s == null) return iDefault
                return s.trim().toLong()
            } catch (e: Exception) {
                return iDefault
            }
        }

        fun parseFloat(s: String?, iDefault: Float = -1f): Float {
            try {
                if (s == null) return iDefault
                return s.trim().toFloat()
            } catch (e: Exception) {
                return iDefault
            }
        }

        fun parseFloat2(s: String?, iDefault: Float? = null): Float? {
            try {
                if (s == null) return iDefault
                return s.trim().toFloat()
            } catch (e: Exception) {
                return iDefault
            }
        }

        fun parseDouble(s: String?, iDefault: Double = -1.0): Double {
            try {
                if (s == null) return iDefault
                return s.trim().toDouble()
            } catch (e: Exception) {
                return iDefault
            }
        }

        fun parseDouble2(s: String?, iDefault: Double? = null): Double? {
            try {
                if (s == null) return iDefault
                return s.trim().toDouble()
            } catch (e: Exception) {
                return iDefault
            }
        }

        fun isInt(i: Float?): Boolean {
            i ?: return false
            return i == ((i as Int) as Float)
        }

        fun isInt(i: Double?): Boolean {
            i ?: return false
            return i == i.toInt().toDouble()
        }

        fun numbOrZero(f: Float?): Float {
            f ?: return 0.0f
            return if (f.isNaN() || f.isInfinite()) 0.0f
            else f
        }

        fun numbOrZero(d: Double?): Double {
            d ?: return 0.0
            return if (d.isNaN() || d.isInfinite()) 0.0
            else d
        }

        fun getCountryCodeAndPhoneNo(text: String): Pair<String?, String?> {
            return try {
                //val text = "1231239664282121"
                val countryCodeEndIndex = text.length - 10

                val phoneNo = text.substring(countryCodeEndIndex) //last 10 values
                val cc = text.substring(0, countryCodeEndIndex) //country code

                val countryCode = if (cc.contains("+")) cc else "+$cc"

                //println(phoneNo)
                //println(countryCode)

                Pair(countryCode, phoneNo)
            } catch (e: java.lang.Exception) {
                Pair(null, null)
            }
        }
    }
}