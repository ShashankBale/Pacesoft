package com.pacesoft.sdk.session

import com.pacesoft.sdk.base.SKBException
import com.pacesoft.sdk.util.utility.Utils
import com.pacesoft.sdk.util.utility.Utils.decodeHex
import x.code.app.XCodeApp
import x.code.util.log.dlog
import x.code.util.log.elog
import x.code.util.log.vlog
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.*

class Xskb {

    companion object {
        var isXsbkInit = false
    }

    private val keyFileName = "skbexportedkey"
    private var secRandom = SecureRandom()
    private val tag = "PaceSoft<>zKeyBox"

    constructor() {
        initializeKey()
    }

    @Throws(SKBException::class)
    private external fun encryptNative(input_text: ByteArray, iv: ByteArray): ByteArray?

    @Throws(SKBException::class)
    private external fun decryptNative(input_text: ByteArray, iv: ByteArray): ByteArray?

    @Throws(SKBException::class)
    private external fun setKey(exported_key_content: ByteArray?)

    @Throws(SKBException::class)
    private external fun getKey(): ByteArray?

    private external fun getIvSize(): Int

    private external fun getReturnCodeString(result: Int): String?

    /**
     * Initialize SKB and take care of key generation or loading. When the app is launched for the first time, an AES
     * key is created and saved on the device using SKB KeyStore. This key is loaded the next time the app is opened.
     */
    private fun initializeKey() {
        try {
            val keyFile: File = getKeyFile()
            try {
                if (keyFile.exists()) {
                    // Read the contents into a byte array and
                    // hand it off to native code to import SKB key
                    val exported_key_bytes: ByteArray? = Utils.readBytesFromFile(keyFile.path)
                    if (exported_key_bytes != null)
                        setKey(exported_key_bytes)
                    else
                        dlog(tag, "exported_key_bytes is null")
                } else {
                    // Create new key
                    setKey(null)
                }
                isXsbkInit = true
            } catch (e: SKBException) {
                elog(
                    t = tag,
                    m = "Failed to set key! Got error " + getReturnCodeString(e.getReturnCode()) + " (" + e.getReturnCode() + ") from SKB function " + e.getFunction()
                )
                return
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
            // Always save the key
            val exportedKeyBytes: ByteArray? = getKey()
            if (exportedKeyBytes != null) {
                val key: String = Utils.bytesToHex(exportedKeyBytes)
                vlog(tag, key)
                Utils.writeKeyToStorage(exportedKeyBytes, keyFile)
            } else
                dlog(tag, "exported_key_bytes is null by getKey()")
        } catch (e: SKBException) {
            elog(
                t = tag,
                m = "Failed to store key! Got error " + getReturnCodeString(e.getReturnCode()) + " (" + e.getReturnCode() + ") from SKB function " + e.getFunction()
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            dlog(tag, "Something went wrong")
        }
    }


    /**
     * @return file handle to where SKB KeyStore is located
     */
    private fun getKeyFile(): File {
        return File(XCodeApp.app.filesDir, keyFileName)
    }

    fun encryptText(input: String): String? {
        val b = input.toByteArray(StandardCharsets.UTF_8)
        val encBytes: ByteArray = encrypt(b) ?: return null
        //return encBytes.let { Utils.bytesToHex(it) } ?: kotlin.run { "" }
        return Utils.bytesToHex(encBytes)
    }

    fun decryptText(encryptedHex: String): String? {
        val encryptedByteArray = encryptedHex.decodeHex()
        val decrypted = decrypt(encryptedByteArray) ?: return null
        return String(decrypted, StandardCharsets.UTF_8)
    }


    /**
     * Encrypt data using SKB with AES in CTR mode using the previously loaded/created key.
     *
     * @param input data to encrypt
     * @return encrypted data
     */
    private fun encrypt(input: ByteArray): ByteArray? {
        try {
            val iv = ByteArray(getIvSize())
            secRandom.nextBytes(iv)
            val s = String(input, StandardCharsets.UTF_8)
            val iv_ = Utils.bytesToHex(iv)
            vlog(tag, "$s,$iv_".trimIndent())
            val outputStream = ByteArrayOutputStream()
            outputStream.write(iv)
            outputStream.write(encryptNative(s.toByteArray(StandardCharsets.UTF_8), iv))
            return outputStream.toByteArray()
        } catch (e: SKBException) {
            elog(
                t = tag,
                m = "SKB function " + e.getFunction()
                    .toString() + " returned " + getReturnCodeString(e.getReturnCode()).toString() + " (" + e.getReturnCode()
                    .toString() + ")"
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            elog(tag, "Something went wrong")
        }
        return null
    }

    /**
     * Decrypt data using SKB with AES in CTR mode using the previously loaded/created key.
     *
     * @param input data to decrypt
     * @return decrypted data
     */
    private fun decrypt(input: ByteArray): ByteArray? {
        try {
            val iv = Arrays.copyOfRange(input, 0, getIvSize())
            val ct = Arrays.copyOfRange(input, getIvSize(), input.size)
            return decryptNative(ct, iv)
        } catch (e: SKBException) {
            elog(
                t = tag,
                m = "SKB function " + e.getFunction()
                    .toString() + " returned " + getReturnCodeString(e.getReturnCode()).toString() + " (" + e.getReturnCode()
                    .toString() + ")"
            )
            return null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            elog(tag, "Something went wrong")
        }
        return null
    }
}