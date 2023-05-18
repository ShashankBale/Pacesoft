package com.pacesoft.sdk.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pacesoft.sdk.R
import com.pacesoft.sdk.module.*
import com.pacesoft.sdk.network.api.XUrl
import com.pacesoft.sdk.network.repo.pojo.BaseApiResponse
import com.pacesoft.sdk.network.util.AppServerException
import com.pacesoft.sdk.network.util.BadNetworkConnectivityCallback
import com.pacesoft.sdk.session.XpssInsta
import com.pacesoft.sdk.util.text.XCrypto
import x.code.util.repo.Resource
import okhttp3.internal.toHeaderList
import retrofit2.Response
import x.code.util.XConst
import x.code.util.gson
import x.code.util.log.elog
import java.net.SocketTimeoutException
import java.net.UnknownHostException

open class BaseVm : ViewModel() {

    val shouldUseUserProfileMockJson = false
    val mockUserProfileJsonFile = "rsp_json/user_profile_8.json"


    fun getApiReq(obj: Any, isTransaction: Boolean = false): Pair<XApiRequest?, String?> {
        val cry: Cryptos = XpssInsta.keysPref.getLatestPsck()

        val jsonStr = gson.toJson(obj)
        elog("PACESOFT_API_REQ", jsonStr)
        val result: SecurityHelperResult = XCrypto.getEncryptedData(
            jsonStr = jsonStr,
            iv = cry.iv,
            key = if (isTransaction) cry.tk else cry.dk
        )

        val errorMsg = result.errorMsg
        return if (errorMsg != null) {
            Pair(null, errorMsg)
        } else {
            val req = XApiRequest(
                crypto = cry,
                apiKey = XpssInsta.apiKey,
                body = result.toBaseRequest(),
                bodyOg = obj
            )
            Pair(req, null)
        }
    }

    fun getApiRsp(
        req: XApiRequest,
        rsp: Response<PsApiRsp>?,
        eApi: Throwable? = null,
        bodyType: Class<*>,
        isTransaction: Boolean = false,
    ): Resource<Any> {
        val isDefaultCrypto = isDefaultInHeaders(rsp)

        val baseResource = (getResource2(rsp, eApi) as Resource<PsApiRsp>)
        if (baseResource.data != null) {
            val result: SecurityHelperResult = getApiRspBodyJson(
                pCry = req.crypto,
                baseResource = baseResource,
                isTransaction = isTransaction,
                isDefaultCrypto = isDefaultCrypto
            )

            elog("PACESOFT_API_RSP", (result.strJsonDecryptedData ?: "NULL"))

            if (result.errorMsg == null) {
                val decryptedPayload: BasePsApiRspBody = gson.fromJson(
                    result.strJsonDecryptedData,
                    bodyType
                ) as BasePsApiRspBody

                val cryptoPayload: CryptoPayload? = decryptedPayload.cryptoPayload

                XpssInsta.userPref.addPsck(cryptoPayload)
                return Resource.success(decryptedPayload)
            } else {
                return Resource.error(result.errorMsg ?: "Something went wrong!!")
            }
        } else {
            val displayError = when (rsp) {
                null -> baseResource.msg ?: "Something went wrong!!"
                else -> getErrorResponseMessage(
                    pCry = req.crypto,
                    rsp = rsp,
                    isTransaction = isTransaction,
                    isDefaultCrypto = isDefaultCrypto
                )
            }
            return Resource.error(displayError)
        }
    }


    private fun isDefaultInHeaders(rsp: Response<PsApiRsp>?): Boolean {
        val headers = rsp?.headers()?.toHeaderList()
        if (headers != null) {
            val headersMap: MutableMap<String, String> = HashMap(headers.size)
            for (header in headers) {
                headersMap[header.name.utf8()] = header.value.utf8()
                Log.d("isDefault", header.name.utf8() + " = " + header.value.toString())
            }

            val isDefault = headersMap["isDefault"]?.lowercase()
            Log.d("isDefault", "" + isDefault)
            return isDefault == "true"
        }
        return false
    }

    /*
    fun getMockUserProfile(): UserProfileRsp.Response? {
        return try {
            val context = XApplication.instance.applicationContext
            val str =
                context.assets.open(mockUserProfileJsonFile).bufferedReader().use { it.readText() }
            val type = object : TypeToken<UserProfileRsp.Response>() {}.type
            Gson().fromJson(str, type)
        } catch (e: Exception) {
            null
        }
    }
    */

    @Throws(AppServerException::class)
    fun getErrApiRspMsg(
        rspObj: Response<out BaseApiResponse<*>>?,
        eApi: Throwable? = null,
    ): String? {
        try {
            if (eApi != null) {
                return when (eApi) {
                    is SocketTimeoutException -> "Network issue, please try again."
                    is UnknownHostException -> "No internet : Check the network and try again."
                    else -> eApi.message
                }
            }
            if (rspObj == null) return "response was null"

            //val name = " for %{rspObj::class.java.name}"
            val name = ""

            when (rspObj.code()) {
                200 -> {
                    val body = rspObj.body()
                    body ?: return "body is null$name"

                    return when {
                        x.code.util.view.text.XStr.isNotEmpty(body.status) ->
                            when {
                                body.status.equals("00", true) -> {
                                    if (body.payload != null)
                                        null //<----- SAB THIK HAI
                                    else
                                        "body#status=true but body#rsp was null$name"
                                }
                                x.code.util.view.text.XStr.isNotEmpty(body.reason) -> body.reason
                                else -> "body#status=false & body#reason is null$name"
                            }
                        x.code.util.view.text.XStr.isNotEmpty(body.reason) -> body.reason
                        else -> "body#status & body#reason are null$name"
                    }
                }

                404 -> return "Sorry something went wrong, please retry later. [#404]"
                502 -> return "Sorry something went wrong, please retry later. [#502]"
                else -> return "Sorry something went wrong, please retry later. Response code is non 200 (i.e. ${rspObj.code()})$name"
            }
        } catch (e: Exception) {
            return "349#response $e.message"
        }
    }

    @Throws(AppServerException::class)
    fun getErrApiRspMsg2(
        rspObj: Response<*>?,
        eApi: Throwable? = null,
    ): String? {
        try {
            if (eApi != null) {
                return when (eApi) {
                    is SocketTimeoutException -> "Network issue, please try again."
                    is UnknownHostException -> "No internet : Check the network and try again."
                    else -> eApi.message
                }
            }
            if (rspObj == null) return "response was null"

            //val name = " for %{rspObj::class.java.name}"
            val name = ""

            when (rspObj.code()) {
                200 -> {
                    val body = rspObj.body()
                    body ?: return "body is null$name"

                    /*return when {
                        x.code.util.view.text.XStr.isNotEmpty(body) ->
                            when {
                                body.status.equals("00", true) -> {
                                    if (body.payload != null)
                                        null //<----- SAB THIK HAI
                                    else
                                        "body#status=true but body#rsp was null$name"
                                }
                                x.code.util.view.text.XStr.isNotEmpty(body.reason) -> body.reason
                                else -> "body#status=false & body#reason is null$name"
                            }
                        x.code.util.view.text.XStr.isNotEmpty(body.reason) -> body.reason
                        else -> "body#status & body#reason are null$name"
                    }*/
                    return null
                }

                404 -> return "Sorry something went wrong, please retry later. [#404]"
                502 -> return "Sorry something went wrong, please retry later. [#502]"
                else -> return "Sorry something went wrong, please retry later. Response code is non 200 (i.e. ${rspObj.code()})$name"
            }
        } catch (e: Exception) {
            return "349#response $e.message"
        }
    }


    private fun eMsg(e: Throwable): String {
        e.printStackTrace()

        val strFormat = when (e) {
            is AppServerException -> "‣API-ERROR :\n%s" //☒✱●
            else -> XpssInsta.context.getString(R.string.ssww_2_withMsg)
        }
        return String.format(strFormat, e.message)
    }

    protected fun getResource(
        rsp: Response<out BaseApiResponse<*>>?,
        eApi: Throwable?,
    ) = try {
        val strErr = getErrApiRspMsg(rsp, eApi)
        if (strErr != null) throw AppServerException(strErr)

        Resource.success(rsp!!.body()!!.payload!!)
    } catch (e: Exception) {
        Resource.error(eMsg(e))
    }

    protected fun getResource2(
        rsp: Response<*>?,
        eApi: Throwable?,
    ): Resource<Any> = try {
        val strErr = getErrApiRspMsg2(rsp, eApi)
        if (strErr != null) throw AppServerException(strErr)

        Resource.success(rsp!!.body()!!)
    } catch (e: Exception) {
        Resource.error(eMsg(e))
    }


    companion object {
        var badNetworkCounter = 0
        var badNetworkCallback: BadNetworkConnectivityCallback? = null
    }

    fun triggerSocketTimeoutException(url: String?) {
        try {
            badNetworkCounter += 1 //increment by 1
            if (badNetworkCounter >= 5) {
                badNetworkCounter = 0 //resetting to zero
                //notify only if more than 5 times
                badNetworkCallback?.showBadNetworkView()
            }
        } catch (e: Exception) {
            //ldSocketTimeoutException.postValue(0)
        }
    }

    fun getHeaders(device: Boolean): HashMap<String, String?> {
        val authorization: String = XpssInsta.apiKey
        val host = XUrl.getHost()
        val connection = "Keep-Alive"
        val headerMap = HashMap<String, String?>()
        headerMap["Authorization"] = "Bearer $authorization"
        if (!device)
            headerMap["Host"] = host
        headerMap["Connection"] = connection
        return headerMap
    }


    /*
    val host = XUrl.getV2Host()
    val connection = "Keep-Alive"
    * */
    fun getV2ApiHeaders(iv: String, apiKey: String) =
        mapOf(XConst.API_HEADER_TAG_API_KEY to apiKey, XConst.API_HEADER_TAG_IV to iv)

    private fun getApiRspBodyJson(
        pCry: Cryptos,
        baseResource: Resource<PsApiRsp>,
        isTransaction: Boolean = false,
        isDefaultCrypto: Boolean = false,
    ): SecurityHelperResult {
        val cry = if (isDefaultCrypto) XpssInsta.keysPref.getDefaultPacesoftKeys() else pCry
        return XCrypto.getDecryptedData(
            strToDecrypt = baseResource.data?.message ?: "",
            iv = cry.iv,
            key = if (isTransaction) cry.tk else cry.dk
        )
    }


    private fun getErrorResponseMessage(
        pCry: Cryptos,
        rsp: Response<PsApiRsp>,
        isTransaction: Boolean = false,
        isDefaultCrypto: Boolean = false
    ): String {
        try {
            val cry = if (isDefaultCrypto) XpssInsta.keysPref.getDefaultPacesoftKeys() else pCry

            val errorMessage = rsp.errorBody()?.string() ?: ""
            val jObjError = JsonParser.parseString(errorMessage).asJsonObject
            var data: JsonElement? = null
            data = if (jObjError.has(XConst.errorMsgKey))
                jObjError.get(XConst.errorMsgKey) ?: return "Error Response is null"
            else
                jObjError.get(XConst.errorMsgKey.lowercase()) ?: return "Error Response is null"
            val result = XCrypto.getDecryptedData(
                strToDecrypt = data.toString(),
                iv = cry.iv,
                key = if (isTransaction) cry.tk else cry.dk
            )

            elog("PACESOFT_API_RSP_ERR", result.strJsonDecryptedData ?: "")

            if (result.errorMsg == null) {
                val errorJsonObj = JsonParser.parseString(result.strJsonDecryptedData).asJsonObject
                val errorLists = errorJsonObj.get("ErrorLists").asJsonArray
                if (errorLists.size() > 0) {
                    val errorObj = errorLists.get(0).asJsonObject

                    /*
                    //Option 1
                    val displayMsg = errorObj.get("DisplayMessage")
                    val displayCode = errorObj.get("DisplayCode") ?: "NA"
                    return displayMsg.toString().plus("[error code:$displayCode]")
                    */

                    //Option 2
                    val errorElement: JsonObject = errorObj.get("Error").asJsonObject

                    val errMsg = errorElement.get("Message").asString
                    val errCode = errorElement.get("Code").asString
                    return "API-ERROR :\n$errMsg [$errCode] "
                } else
                    return "Something went wrong!!, [Dev:EmptyErrorLists]"
            } else {
                return result.errorMsg ?: "Something went wrong!!!"
            }

        } catch (e: Exception) {
            return "Exception in error response:" + e.message
        }
    }


    fun getLatestCryptos(): Cryptos {
        return XpssInsta.keysPref.getLatestPsck();
    }
}
