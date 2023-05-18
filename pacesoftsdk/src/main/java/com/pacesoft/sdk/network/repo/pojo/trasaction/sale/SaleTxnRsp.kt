package com.pacesoft.sdk.network.repo.pojo.trasaction.sale

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SaleTxnRsp(
    @SerializedName("approved") val approved: Boolean? = false,
    @SerializedName("responseCode") val responseCode: String? = "",
    @SerializedName("responseDescription") val responseDescription: String? = "",
    @SerializedName("authCode") val authCode: String? = "",
    @SerializedName("transactionId") val transactionId: String? = "",
    @SerializedName("referenceId") val referenceId: String? = "",
    @SerializedName("transactionType") val transactionType: String? = "",
    @SerializedName("transactionHash") val transactionHash: String? = "",
    @SerializedName("timestamp") val timestamp: String? = "",
    @SerializedName("entryMethod") val entryMethod: String? = "",
    @SerializedName("paymentType") val paymentType: String? = "",
    @SerializedName("maskedPan") val maskedPan: String? = "",
    @SerializedName("cardHolder") val cardHolder: String? = "",
    @SerializedName("partialAuth") val partialAuth: Boolean? = false,
    @SerializedName("currencyCode") val currencyCode: String? = "",
    @SerializedName("requestedAmount") val requestedAmount: Double? = 0.0,
    @SerializedName("authorizedAmount") val authorizedAmount: Double? = 0.0,
    @SerializedName("surchargeAmount") val surchargeAmount: Int? = 0,
    @SerializedName("PAR") val PAR: String? = "",
    @SerializedName("version") val version: String? = "",
    @SerializedName("token") val token: String? = "",
    @SerializedName("receiptSuggestions") val receiptSuggestions: ReceiptSuggestions? = ReceiptSuggestions(),
    @SerializedName("terminalTransactionId") val terminalTransactionId: Long? = 0,
    @SerializedName("emvData") val emvData: String? = "",
    @SerializedName("avsResultCode") val avsResultCode: String? = "",
    @SerializedName("cvvResultCode") val cvvResultCode: String? = "",
    @SerializedName("avsResponse") val avsResponse: String? = ""
) : Parcelable {
    @Parcelize
    data class ReceiptSuggestions(
        @SerializedName("AID") val AID: String? = "",
        @SerializedName("ARQC") val ARQC: String? = "",
        @SerializedName("IAD") val IAD: String? = "",
        @SerializedName("TVR") val TVR: String? = "",
        @SerializedName("TSI") val TSI: String? = "",
        @SerializedName("merchantName") val merchantName: String? = "",
        @SerializedName("applicationLabel") val applicationLabel: String? = "",
        @SerializedName("requestSignature") val requestSignature: Boolean? = false,
        @SerializedName("maskedPan") val maskedPan: String? = "",
        @SerializedName("authorizedAmount") val authorizedAmount: String? = "",
        @SerializedName("transactionType") val transactionType: String? = "",
        @SerializedName("entryMethod") val entryMethod: String? = ""
    ) : Parcelable
}

/*
{
  "approved": true,
  "responseCode": "00",
  "responseDescription": "APPROVAL AXS930",
  "authCode": "AXS930",
  "transactionId": "000000321000931",
  "referenceId": "222214501883",
  "timestamp": "2022-08-10T14:22:40.4356097+00:00",
  "entryMethod": "swiped",
  "maskedPan": "****-****-****-1003",
  "cardHolder": "UAT USA UAT USA",
  "partialAuth": false,
  "currencyCode": "USD",
  "requestedAmount": 0.23,
  "authorizedAmount": 0.23,
  "surchargeAmount": 0,
  "receiptSuggestions": {
    "merchantName": "DeviceServices",
    "requestSignature": false,
    "maskedPan": "****-****-****-1003",
    "entryMethod": "swiped"
  },
  "terminalTransactionId": 10331446155542592,
  "avsResultCode": "0",
  "avsResponse": "Approved"
}
* */