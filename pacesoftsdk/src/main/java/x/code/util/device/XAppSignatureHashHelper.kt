package x.code.util.device

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

// Get all package details
class XAppSignatureHashHelper(context: Context?) : ContextWrapper(context) {

    private val mTag: String = XAppSignatureHashHelper::class.java.simpleName
    private val mHashType = "SHA-256"
    private val mNumHashedBytes = 9
    private val mNumBase64Char = 11

    @TargetApi(32)
    private fun hash(packageName: String, signature: String): String? {
        val appInfo = "$packageName $signature"
        try {
            val messageDigest = MessageDigest.getInstance(mHashType)
            messageDigest.update(appInfo.toByteArray(StandardCharsets.UTF_8))
            var hashSignature = messageDigest.digest()

            //Truncated into mNumHashedBytes
            hashSignature = Arrays.copyOfRange(hashSignature, 0, mNumHashedBytes)

            //Encode into Base64
            var base64Hash =
                Base64.encodeToString(hashSignature, Base64.NO_PADDING or Base64.NO_WRAP)

            base64Hash = base64Hash.substring(0, mNumBase64Char)

            return base64Hash
        } catch (e: NoSuchAlgorithmException) {
            Log.e(mTag, "No Such Algorithm Exception", e)
        }
        return null
    }

    private val alAppSignatures: ArrayList<String> by lazy {
        val items = ArrayList<String>()
        try {
            // Get all package details
            val packageName = packageName
            val packageManager = packageManager
            val signatures = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            ).signatures
            for (signature in signatures) {
                val hash = hash(packageName, signature.toCharsString())
                if (hash != null) {
                    items.add(String.format("%s", hash))
                }
            }
        } catch (e: Exception) {
            Log.e(mTag, "Package not found", e)
        }

        items
    }

    fun getAppSignature(): String {
        return if (alAppSignatures.isNotEmpty()) alAppSignatures[0]
        else ""
    }
}