package com.pacesoft.sdk.util.utility

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    private val hexArray = "0123456789ABCDEF".toCharArray()

    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v: Int = 0xff and bytes[j].toInt()//(bytes[j] && 0xFF)
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun hexToBytes(s: String): ByteArray? {
        val len = s.length
        if (len % 2 != 0) {
            return ByteArray(0)
        }
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((s[i].digitToIntOrNull(16) ?: -1 shl 4)
            + s[i + 1].digitToIntOrNull(16)!! ?: -1).toByte()
            Log.d("AuthActivity", "" + (data[i / 2]))
            i += 2
        }
        return data
    }

    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }


    /**
     * Write the SKB key to persistent storage so it can be retrieved later
     *
     * @param key     byte representation of SKB key
     * @param keyFile File where key is to be stored
     */
    @Throws(java.lang.Exception::class)
    fun writeKeyToStorage(key: ByteArray, keyFile: File) {
        if (!keyFile.exists()) {
            keyFile.createNewFile()
        }
        val fos = FileOutputStream(keyFile)
        fos.write(key)
        fos.close()
    }

    /**
     * Read contents of a file into a byte array
     *
     * @param filePath path to where data is stored
     * @return a byte array with the contents of the file
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    fun readBytesFromFile(filePath: String): ByteArray? {
        val file = File(filePath)
        val fis = FileInputStream(file)
        val data = ByteArray(file.length().toInt())
        fis.read(data)
        fis.close()
        return data
    }


    fun printHex(bytes: ByteArray?): String {

        val sb = StringBuilder()
        for (b in bytes!!) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }


    fun formatAmount(raw: String): String {

        /* Remove heading '0's */
        var lraw = raw
        while (lraw.startsWith("0")) lraw = lraw.substring(1)

        /* Set currency separator */
        val sb = StringBuilder(raw)
        sb.insert(raw.length - 2, ".")

        /* Add $ sign */sb.append('$')
        return sb.toString()
    }

    fun getDate(format: String): String? {
        val dateFormat = SimpleDateFormat(format)
        return dateFormat.format(Date())
    }

    fun getTime(format: String): String? {
        val dateFormat = SimpleDateFormat(format)
        return dateFormat.format(Date())
    }
}