package com.pacesoft.sdk.agnos

import android.app.Activity
import android.util.Log
import ca.amadis.agnos.sdk.Agnos
import com.pacesoft.sdk.util.utility.Utils
import java.nio.charset.StandardCharsets
import java.util.*

object Sred {

    private external fun getIV(): ByteArray;

    fun exportKCV(agnos: Agnos) {

        Log.v("Sred", "exportKCV")

        val data = agnos.exportKCV()
        if (data != null) {
            val kcv = Arrays.copyOfRange(data, 2, data.size)
            Log.v("Sred", "exportKCV - KCV: ${Utils.printHex(kcv)}")
        }
    }

    fun exportHashedPAN(agnos: Agnos) {

        Log.v("Sred", "exportHashedPAN")

        val data = agnos.exportHashedPAN()
        if (data != null) {
            val hpan = Arrays.copyOfRange(data, 2, data.size)
            Log.v("Sred", "exportHashedPAN - Hashed PAN: ${Utils.printHex(hpan)}")
        }
    }

    private fun extractBlock(name: String, buffer: ByteArray, index: Int): ByteArray {

        val len: Int = buffer[index].toUByte().toInt() * 256 + buffer[index + 1].toUByte().toInt()
        val extract = buffer.copyOfRange(index + 2, index + 2 + len)

        Log.v("Sred", "extractBlock - $name ($len): ${Utils.printHex(extract)}")
            
        return extract
    }
/*
    private fun decryptBlock(activity: Activity, block: ByteArray): ByteArray? {

        Log.v("Sred", "decryptBlock: ${Utils.printHex(block)}")

       *//* val privateKey = Crypto.readPrivateKey(activity, "km_l2_key.pem")
        var index: Int = 0

        // Extract block data
        val rsaKeyId = extractBlock("RSA key ID", block, index)
        index += (2 + rsaKeyId.size)

        val encryptedKek = extractBlock("encrypted KEK", block, index)
        index += (2 + encryptedKek.size)

        val encryptedData = extractBlock("encrypted data", block, index)
        index += (2 + encryptedData.size)

        val hmacKeyId = extractBlock("HMAC key ID", block, index)
        index += (2 + hmacKeyId.size)

        val checksum = extractBlock("checksum", block, index)
        index += (2 + checksum.size)

        // Decrypt data
        val kek = Crypto.rsaDecrypt(encryptedKek, privateKey) ?: return null
        Log.v("Sred", "decryptBlock - kek: " + Print.printHex(kek))

        // Extract data
        val clearData = Crypto.decryptAesEcbNoPadding(kek, encryptedData)
        Log.v("Sred", "decryptBlock - clear data: " + Print.printHex(clearData))
*//*
        return clearData
    }*/

    fun decryptDataBlock(agnos: Agnos):String {//(activity: Activity, agnos: Agnos) {

        Log.v("Sred", "decryptDataBlock")

        val data = agnos.exportCardData()
        if(data != null) {
            val ivdata = getIV();
            val hexIV = String(ivdata, StandardCharsets.UTF_8)
            Log.v("Sred", "hexIV - $hexIV")
            if (data == null && ivdata == null) {
                Log.v("Sred", "decryptDataBlock - data is null")
                return ""
            }
            return "$hexIV${Utils.bytesToHex(data)}"
        }else
            return "NA"
//        return Utils.bytesToHex(data)//String(data, StandardCharsets.UTF_8)
//        decryptBlock(activity, data)
    }

    fun decryptPinBlock(activity: Activity, pin: ByteArray?) {

        Log.v("Sred", "decryptPinBlock")

        if (pin == null) {
            Log.v("Sred", "decryptPinBlock - pin is null")
            return
        }

//        val clearPinData = decryptBlock(activity, pin)

        // Extract PIN from block
//        if (clearPinData != null) {
//
//            var index: Int = 0
//            while(index < 16 && clearPinData[index] % 16 != 0) { index ++ }
//            val size = 15 - index
//            if (size <= 1 || size > 12) { throw Exception("Invalid PIN block size: $size") }
//
//            val digits = "0123456789"
//            var pinPlain = ""
//            for(i in (index + 1)..15) {
//
//                val d = clearPinData[i].toUByte().toInt() % 16
//                if (d < 0 || d > 9) { throw Exception("Invalid digit: $d") }
//                pinPlain = "$pinPlain${digits[d]}"
//            }
//
//            Log.v("Sred", "decryptPinBlock - pin: $pinPlain")
//        }
    }
}
