package com.pacesoft.sdk.vm.user.merchant.terminal

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.pacesoft.sdk.module.PsApiRsp
import com.pacesoft.sdk.module.XApiRequest
import com.pacesoft.sdk.network.repo.UserMerchantApiRepo
import com.pacesoft.sdk.network.repo.pojo.trasaction.sale.SaleTxnV2Req
import com.pacesoft.sdk.network.repo.pojo.trasaction.sale.SaleTxnV2Rsp
import com.pacesoft.sdk.vm.BaseVm
import x.code.util.repo.Resource
import retrofit2.Response
import x.code.util.number.Numb
import x.code.util.view.text.XStr
import java.util.*

class MTerminalVm : BaseVm() {
    val mRepo = UserMerchantApiRepo

    /**SaleTransaction*/
    val ldSale =
        MutableLiveData<Resource<SaleTxnV2Rsp?>>()//MutableLiveData<Resource<SaleTxnRsp?>>()

    /*fun apiInit_4_Sale(headerMap: HashMap<String, String?>, req: SaleTxnReq) {
        mRepo.apiInit_4_Sale(req, this, headerMap)
        ldSale.postValue(Resource.loading())
    }

    fun apiDump_4_Sale(rsp: Response<SaleTxnRsp?>?, eApi: Throwable? = null) {
        val resource = (getResource2(rsp, eApi) as Resource<SaleTxnRsp?>)
        ldSale.postValue(resource)
    }*/

     fun processSaleRequest(cartAmount:Double, cardEntryType : Int, track2Data : String?, encryptedData: String?, pParams: LinkedHashMap<String, String?>?) {
         var account : SaleTxnV2Req.Account? = null
         if(!XStr.isEmpty(track2Data)){
            var t2data = track2Data
            var pan = t2data!!.split("=")[0]

            account = SaleTxnV2Req.Account(
                pan = null,
                expirationDate = null,
                cvv = null,
                trackData = t2data,
                emvTlvData = null,
                emvData = null,
                encryptedTlvData = null,
                billTo = null,
                //plainEmvData =
            )

        }else {
             val params = pParams
             if (params != null) {
                 val iterator = params.entries//entries.iterator()
                 val stringBuilder = StringBuilder()
                 for (key in params.keys) {
                     val value = params[key]
                     if (value != null) {
                         Log.d("MTerminalTxnTap", key + ":" + value)
                         if (value.isEmpty())
                             stringBuilder.append(key).append("00").append(value)
                         else {
                             //                        var len = decimalFormat.format()
                             //                        var len = Integer.toString((value!!.length / 2), 16)
                             var len = String.format("%02X", (0xFF and (value!!.length / 2)))
                             stringBuilder.append(key).append(len).append(value)
                         }
                     }
                 }

                 if (!params.containsKey("4F")) {
                     val key = "84"
                     val value = params[key]
                     var len = String.format("%02X", (0xFF and (value!!.length / 2)))
                     stringBuilder.append("4F").append(len).append(value)
                 }
                 Log.d("MTerminalTxnTapCardFrag", "" + stringBuilder.toString())
                  account = SaleTxnV2Req.Account(
                     pan = null,
                     expirationDate = null,
                     cvv = null,
                     trackData = null,
                     emvTlvData = stringBuilder.toString(),
                     emvData = null,
                     encryptedTlvData = encryptedData,
                     billTo = null,
                     //plainEmvData =
                 )

             }
         }
             val shipTo = SaleTxnV2Req.ShipTo(
                 firstName = "",
                 lastName = "",
                 address = SaleTxnV2Req.ShipTo.Address(),
                 phoneNumber = "",
                 emailAddress = ""
             )
             val saleTransRequest = SaleTxnV2Req(
                 shipTo = shipTo,
                 amountTotal = XStr.cf2(cartAmount),
                 instrumentType = cardEntryType,//6,
                 digitalWalletType = 0,
                 account = account,
             )
             Log.d("MTerminalTxnTapCardFrag", "saleTransRequest:" + saleTransRequest.toString())
             apiInit_4V2_Sale(saleTransRequest)

    }

    fun apiInit_4V2_Sale(reqBody: SaleTxnV2Req) {
        /*val jsonRequest = gson.toJson(req)//req.convert<AuthLoginReq,String>()
        Log.d("SaleAPI", "" + jsonRequest)
        val dk = XInsta.appKeysPref.tk
        val iv = XInsta.appKeysPref.iv

        val result = XCrypto.getEncryptedData(
            jsonStr = jsonRequest,
            iv = iv,
            key = dk
        )

        val headerMap = getHeaders4V2Api(iv)
        Log.d("SaleAPI", dk + "\n" + iv)

        if (result.errorMsg == null) {
            mRepo.apiInit_4V2_Sale(
                req = result.toBaseRequest(),
                vm = this,
                headerMap = headerMap
            )
            ldSale.postValue(Resource.loading())
        } else {
            ldSale.postValue(
                Resource.error(
                    result.errorMsg ?: "Something went wrong!!"
                )
            )
        }*/

        val (req, err) = getApiReq(obj = reqBody, isTransaction = true)
        if (err != null) ldSale.postValue(Resource.error(err))
        else if (req != null) {
            mRepo.apiInit_4V2_Sale(req = req, vm = this)
            ldSale.postValue(Resource.loading())
        }
    }

    fun apiDump_4V2_Sale(req: XApiRequest, rsp: Response<PsApiRsp>?, eApi: Throwable? = null) {
        /*val baseResource = (getResource2(rsp, eApi) as Resource<PsApiRsp>)
        if (baseResource.data != null) {
            val result = getPayload(crypto = req.crypto, baseResource = baseResource, isTerminal = true)
            if (result.errorMsg == null) {
                val saleResp = gson.fromJson(
                    result.decryptedData,
                    SaleTxnV2Rsp::class.java
                )
                val resource = Resource.success(saleResp)
                ldSale.postValue(resource)
            } else {
                ldSale.postValue(
                    Resource.error(
                        result.errorMsg ?: "Something went wrong!!"
                    )
                )
            }
        } else {
            val displayError = when (rsp) {
                null -> baseResource.msg ?: "Something went wrong!!"
                else -> getErrorResponseMessage(crypto = req.crypto, rsp = rsp, isTerminal = true)
            }
            ldSale.postValue(Resource.error(displayError))
        }
//        val resource = (getResource2(rsp, eApi) as Resource<SaleTxnV2Rsp?>)
//        ldSale.postValue(resource)
*/

        ldSale.postValue(
            getApiRsp(
                req = req,
                rsp = rsp,
                eApi = eApi,
                bodyType = SaleTxnV2Rsp::class.java,
                isTransaction = true,
            ) as Resource<SaleTxnV2Rsp>
        )
    }


    companion object {

        fun getFormattedInput(s: String): Pair<String, Double> {
            val t = when (val length = s.length) {
                0 -> "0.00"
                1 -> "0.0$s"
                2 -> "0.$s"
                else -> {
                    /*
                    val n = Numb.parseLong(s, 0)

                    val afterDot = n % 100
                    val beforeDot = n / 100
                    "$$beforeDot.$afterDot"
                    * */

                    s.substring(0, length - 2) + "." + s.substring(length - 2)
                }
            }
            return Pair("$${XStr.cDbl(Numb.parseDouble(t))}", Numb.parseDouble(t))
        }
    }

    fun getCardBrand(cardNumber: String?): String {
        return try {

            val regVisa = Regex("^4.*$")
            val regMaster = Regex("^(5|2223).*$")
            val regAmex = Regex("^(34|37).*$")
            val regDiners = Regex("^(30|36|38).*$")
            val regDiscover = Regex("^6.*$")
            val regJCB = Regex("^(2131|1800|35).*$")
            if (cardNumber!!.matches(regVisa)) return "Visa"
            if (cardNumber!!.matches(regMaster)) return "MasterCard"
            if (cardNumber!!.matches(regAmex)) return "Amex"
            if (cardNumber!!.matches(regDiners)) return "Diners"
            if (cardNumber!!.matches(regDiscover)) return "Discover"
            if (cardNumber!!.matches(regJCB)) "JCB" else "Unknown"
        } catch (ex: java.lang.Exception) {
            Log.e("CardScheme", ex.message + "GetCardBrand")
            "Unknown"
        }
    }
}