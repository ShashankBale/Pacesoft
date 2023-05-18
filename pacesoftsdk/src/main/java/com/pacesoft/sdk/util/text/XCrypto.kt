package com.pacesoft.sdk.util.text

import android.util.Base64
import android.util.Log
import com.pacesoft.sdk.module.SecurityHelperResult
import com.pacesoft.sdk.session.XpssInsta
import x.code.util.view.text.XStr
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object XCrypto {

    private const val ALGORITHM = "AES"
    private const val MODE = "CBC"
    private const val PADDING = "PKCS7Padding"
    private const val TRANSFORMATION = "$ALGORITHM/$MODE/$PADDING"

    fun getEncryptedData(jsonStr: String, iv: String, key: String): SecurityHelperResult {
        Log.d("EncryptedData","IV:"+iv+"\nkey:"+key)
        var error = ""
        val keyBytes: ByteArray
        val ivBytes: ByteArray
        try {
            keyBytes = Base64.decode(key, Base64.DEFAULT)
            ivBytes = Base64.decode(iv, Base64.DEFAULT)
            val sKey = SecretKeySpec(keyBytes, ALGORITHM)
            val inputData = jsonStr.toByteArray(Charsets.UTF_8)
            val ivSpec = IvParameterSpec(ivBytes);
            synchronized(Cipher::class.java) {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, sKey, ivSpec)
                val cipherText = ByteArray(cipher.getOutputSize(inputData.size))
                var ctLength = cipher.update(
                    inputData, 0, inputData.size,
                    cipherText, 0
                )
                ctLength += cipher.doFinal(cipherText, ctLength)
                val encryptedStr = Base64.encodeToString(cipherText, Base64.NO_WRAP)
                return if (XStr.isEmpty(encryptedStr))
                    com.pacesoft.sdk.module.SecurityHelperResult(
                        errorMsg = "Something went wrong. Response Failed",
                        encryptedData = "",
                        strJsonDecryptedData = null
                    )
                else
                    SecurityHelperResult(
                        errorMsg = null,
                        encryptedData = encryptedStr,
                        strJsonDecryptedData = null
                    )
            }
        } catch (uee: UnsupportedEncodingException) {
            uee.printStackTrace()
            error = uee.message ?: "UnsupportedEncodingException"
        } catch (ibse: IllegalBlockSizeException) {
            ibse.printStackTrace()
            error = ibse.message ?: "IllegalBlockSizeException"
        } catch (bpe: BadPaddingException) {
            bpe.printStackTrace()
            error = bpe.message ?: "BadPaddingException"
        } catch (ike: InvalidKeyException) {
            ike.printStackTrace()
            error = ike.message ?: "InvalidKeyException"
        } catch (nspe: NoSuchPaddingException) {
            nspe.printStackTrace()
            error = nspe.message ?: "NoSuchPaddingException"
        } catch (nsae: NoSuchAlgorithmException) {
            nsae.printStackTrace()
            error = nsae.message ?: "NoSuchAlgorithmException"
        } catch (e: ShortBufferException) {
            e.printStackTrace()
            error = e.message ?: "ShortBufferException"
        } catch (e: Exception) {
            e.printStackTrace()
            error = e.message ?: "Exception"
        }

        if (XStr.isEmpty(error))
            error = "Something went wrong. Request Failed!!"
        return SecurityHelperResult(
            errorMsg = error,
            encryptedData = "",
            strJsonDecryptedData = null
        )
    }

    fun getDecryptedData(strToDecrypt: String, iv: String, key: String): SecurityHelperResult {
        var error = ""
        val ivBytes: ByteArray
        val keyBytes: ByteArray
        try {
            keyBytes = Base64.decode(key, Base64.DEFAULT)
            ivBytes = Base64.decode(iv, Base64.DEFAULT)
            val inputData: ByteArray = Base64.decode(strToDecrypt, Base64.NO_WRAP)
            val sKey = SecretKeySpec(keyBytes, ALGORITHM)
            val ivSpec = IvParameterSpec(ivBytes);
            synchronized(Cipher::class.java) {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, sKey, ivSpec)
                val plainText = ByteArray(cipher.getOutputSize(inputData.size))
                var ptLength = cipher.update(inputData, 0, inputData.size, plainText, 0)
                ptLength += cipher.doFinal(plainText, ptLength)
                val decryptedString = plainText.toString(Charsets.UTF_8)
                val decryptedResult = decryptedString.trim { it <= ' ' }
                return if (XStr.isEmpty(decryptedString))
                    SecurityHelperResult(
                        errorMsg = "Something went wrong. Response Failed",
                        encryptedData = "",
                        strJsonDecryptedData = decryptedResult
                    )
                else
                    SecurityHelperResult(
                        errorMsg = null,
                        encryptedData = "",
                        strJsonDecryptedData = decryptedResult
                    )
            }
        } catch (uee: UnsupportedEncodingException) {
            uee.printStackTrace()
            error = uee.message ?: "UnsupportedEncodingException"
        } catch (ibse: IllegalBlockSizeException) {
            ibse.printStackTrace()
            error = ibse.message ?: "IllegalBlockSizeException"
        } catch (bpe: BadPaddingException) {
            bpe.printStackTrace()
            error = bpe.message ?: "BadPaddingException"
            XpssInsta.keysPref.removeLatestPsck() //TODO : Need to fix this
        } catch (ike: InvalidKeyException) {
            ike.printStackTrace()
            error = ike.message ?: "InvalidKeyException"
        } catch (nspe: NoSuchPaddingException) {
            nspe.printStackTrace()
            error = nspe.message ?: "NoSuchPaddingException"
        } catch (nsae: NoSuchAlgorithmException) {
            nsae.printStackTrace()
            error = nsae.message ?: "NoSuchAlgorithmException"
        } catch (e: ShortBufferException) {
            e.printStackTrace()
            error = e.message ?: "ShortBufferException"
        } catch (e: Exception) {
            error = e.message ?: "Exception"
            e.printStackTrace()
        }

        if (XStr.isEmpty(error))
            error = "Something went wrong. Response Failed!!"
        return SecurityHelperResult(
            errorMsg = "APP-ERROR :$error",
            encryptedData = "",
            strJsonDecryptedData = null
        )
    }


    fun pwdEncrypt(text: String): String {
        val (ivSpec, keySpec, cipher) = getCipherKeys()
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return Base64.encodeToString(cipher.doFinal(text.toByteArray()), Base64.NO_WRAP)
    }

    fun pwdDecrypt(text: ByteArray): String {
        val (ivSpec, keySpec, cipher) = getCipherKeys()
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return String(cipher.doFinal(text))
    }

    private fun getCipherKeys(): Triple<IvParameterSpec, SecretKeySpec, Cipher> {
        val ivSpec = IvParameterSpec("3ad7asdfasdf0d7asdf".toByteArray())
        val keySpec = SecretKeySpec("2b7e15asdfasdgasdfgasdff7123419cf4f3c".toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        return Triple(ivSpec, keySpec, cipher)
    }

    fun encodeBase64(strPlain: String): String {
        return try {
            val data: ByteArray = strPlain.toByteArray(StandardCharsets.UTF_8)
            return Base64.encodeToString(data, Base64.NO_WRAP)
        } catch (e1: Exception) {
            e1.printStackTrace()
            ""
        }
    }

    fun decodeBase64(strBase64: String): String {
        return try {
            val data = Base64.decode(strBase64, Base64.DEFAULT)
            return String(data, XStr.csUtf8)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            ""
        }
    }
}