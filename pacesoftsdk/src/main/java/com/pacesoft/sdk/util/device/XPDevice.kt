package com.pacesoft.sdk.util.device

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import x.code.util.device.XDevice
import x.code.util.log.delog
import java.security.*

object XPDevice {

    private const val mTag = "XPDevice#"

    fun getDeviceId(): String {
        val pubKey = getOrGenPublicKey()
        val pubKey2 = pubKey.replace("\n", "")
        delog("$mTag AksDeviceId", pubKey2)
        return pubKey2
    }

    private fun getOrGenPublicKey(): String {
        try {
            //Alias as constant which will not be change
            //To Developer : Don't change this
            val alias = "PaceSoftKey001"

            //Get Instance of KeyStore object
            val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            //Create the keys only if not present.
            if (!keyStore.containsAlias(alias)) {
                val purpose = KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                val parameterSpec: KeyGenParameterSpec =
                    KeyGenParameterSpec.Builder(alias, purpose).run {
                        setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        build()
                    }

                //Using Elliptic Curve (EC) Cryptography key for generating KeyPair, which will generate 126 size string length
                val keyAlgo = KeyProperties.KEY_ALGORITHM_EC
                val kpg = KeyPairGenerator.getInstance(keyAlgo, keyStore.provider)
                kpg.initialize(parameterSpec)

                val kp: KeyPair = kpg.generateKeyPair()
                val pk: PublicKey = kp.public
                val strPk = String(Base64.encode(pk.encoded, Base64.DEFAULT))
                Log.i("$mTag KpgPk", strPk)
                return strPk
            } else {
                // Retrieve the keys, since it was previously generated using Elliptic Curve (EC) Cryptography
                val ksEntry: KeyStore.Entry = keyStore.getEntry(alias, null)
                val pvtKeyEntry = ksEntry as KeyStore.PrivateKeyEntry

                val pk: PublicKey = pvtKeyEntry.certificate.publicKey as PublicKey
                val strPk = String(Base64.encode(pk.encoded, Base64.DEFAULT))
                Log.i("$mTag KpgPk", strPk)
                return strPk
            }
        } catch (e: Exception) {
            Log.e("$mTag Err", Log.getStackTraceString(e))
            return XDevice.androidId
        }
    }


    /*
    fun deleteGenPublicKey(): Boolean {
        try {
            val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val alias = "TEST_KEY_2"

            // Check if create present, if present than delete them
            return if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("$mTag Err", Log.getStackTraceString(e))
            return false
        }
    }
    */

    /*fun getGDeviceId(pContext: Context): String {

        // Although you can define your own key generation parameter specification, it's
        // recommended that you use the value specified here.
        val mainKey: MasterKey = MasterKey.Builder(pContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        *//*
        * Generate a new EC key pair entry in the Android Keystore by
        * using the KeyPairGenerator API. The private key can only be
        * used for signing or verification and only with SHA-256 or
        * SHA-512 as the message digest.
        *//*
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
            *//* algorithm = *//* KeyProperties.KEY_ALGORITHM_EC,
            *//* provider = *//* "AndroidKeyStore"
        )
        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            *//* keystoreAlias = *//* "TEMP", //<-TODO : Need to test this
            *//* purposes = *//* KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).run {
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            build()
        }

        kpg.initialize(parameterSpec)

        val kp: KeyPair = kpg.generateKeyPair()
        val privateKey = kp.private
        val publicKey = kp.public

        val strPub = getPublicKey(publicKey)
        //val strPvt = getPrivateKey(privateKey)

        //Log.e("PaceSoftPublic", strPub)
        //Log.e("PaceSoftPrivate", strPvt)

        //tvText?.text = "Public : $strPub \n Private : $strPvt"

        val strPub2 = strPub.replace("\n", "")
        return strPub2
    }

    private fun getPublicKey(publicKey: PublicKey): String {
        return try {
            String(Base64.encode(publicKey.encoded, Base64.DEFAULT))
        } catch (e: java.lang.Exception) {
            e.message ?: "Something went wrong"
        }
    }

    private fun getPrivateKey(privateKey: PrivateKey): String {
        return try {
            String(Base64.encode(privateKey.encoded, Base64.DEFAULT))
        } catch (e: java.lang.Exception) {
            e.message ?: "Something went wrong"
        }
    }


    private fun printKey(keyStore: KeyStore) {
        val keyStoreEntry = keyStore.getEntry("WASTE_KEY", null) as KeyStore.PrivateKeyEntry
        val publicKeyBytes: ByteArray = keyStoreEntry.certificate.publicKey.encoded
        println(String(publicKeyBytes))
    }*/


}