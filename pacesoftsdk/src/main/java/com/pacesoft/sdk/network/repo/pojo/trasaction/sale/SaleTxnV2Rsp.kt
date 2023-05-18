package com.pacesoft.sdk.network.repo.pojo.trasaction.sale

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.pacesoft.sdk.module.BasePsApiRspBody
import kotlinx.android.parcel.Parcelize

@Parcelize
class SaleTxnV2Rsp : BasePsApiRspBody(), Parcelable {
    @SerializedName("Approved") val approved: Boolean? = null
    @SerializedName("AuthCode") val authCode: String? = null
    @SerializedName("AuthorizedAmount") val authorizedAmount: Double? = null
    @SerializedName("AVSResponse") val aVSResponse: String? = null
    @SerializedName("AVSResultCode") val aVSResultCode: String? = null
    @SerializedName("CardHolder") val cardHolder: Any? = null
    @SerializedName("CurrencyCode") val currencyCode: String? = null
    @SerializedName("CVVResultCode") val cVVResultCode: Any? = null
    @SerializedName("EntryMethod") val entryMethod: String? = null
    @SerializedName("MaskedPan") val maskedPan: String? = null
    @SerializedName("PAR") val pAR: Any? = null
    @SerializedName("PartialAuth") val partialAuth: Boolean? = null
    @SerializedName("PaymentType") val paymentType: Any? = null
    @SerializedName("ReceiptSuggestions") val receiptSuggestions: ReceiptSuggestions? = null
    @SerializedName("ReferenceId") val referenceId: String? = null
    @SerializedName("RequestedAmount") val requestedAmount: Double? = null
    @SerializedName("ResponseCode") val responseCode: String? = null
    @SerializedName("ResponseDescription") val responseDescription: String? = null
    @SerializedName("SurchargeAmount") val surchargeAmount: Int? = null
    @SerializedName("TerminalTransactionId") val terminalTransactionId: Int? = null
    @SerializedName("Timestamp") val timestamp: String? = null
    @SerializedName("Token") val token: Any? = null
    @SerializedName("TransactionHash") val transactionHash: Any? = null
    @SerializedName("TransactionId") val transactionId: String? = null
    @SerializedName("TransactionType") val transactionType: Any? = null
    @SerializedName("Version") val version: Any? = null

    data class ReceiptSuggestions(
        @SerializedName("AID") val aID: String? = null,
        @SerializedName("AIDName") val aIDName: String? = null,
        @SerializedName("CardBrand") val cardBrand: String? = null,
        @SerializedName("EntryMethod") val entryMethod: String? = null,
        @SerializedName("MaskedPan") val maskedPan: String? = null,
        @SerializedName("MerchantAddress1") val merchantAddress1: String? = null,
        @SerializedName("MerchantCity") val merchantCity: String? = null,
        @SerializedName("MerchantName") val merchantName: String? = null,
        @SerializedName("MerchantPhone") val merchantPhone: String? = null,
        @SerializedName("MerchantState") val merchantState: String? = null,
        @SerializedName("MerchantZip") val merchantZip: String? = null
    )
}

/*
{
  "Approved": true,
  "AuthCode": "AXS969",
  "AuthorizedAmount": 1.49,
  "AVSResponse": "Approved",
  "AVSResultCode": "0",
  "CardHolder": null,
  "Crypto": {
    "ApiKey": "4176a366-8651-4825-a3f3-e9dbd5d1b3f9",
    "DK": "Lg0kzHpM68R9XPigE54dl0ozk0y6k3L6HKJlzwDvzrM=",
    "IV": "n+jiS3n/vpwiMoNENhbjVA==",
    "TK": "p8iMRbzcNvYaT22sy0uPostVfIbg9c5hqwQhmkUMNis="
  },
  "CurrencyCode": "USD",
  "CVVResultCode": null,
  "EntryMethod": "Contactless",
  "MaskedPan": "****-****-****-1006",
  "PAR": null,
  "PartialAuth": false,
  "PaymentType": null,
  "ReceiptSuggestions": {
    "AID": "A000000025010402",
    "AIDName": "AMERICAN EXPRESS",
    "CardBrand": "AmericanExpress",
    "EntryMethod": "Contactless",
    "MaskedPan": "****-****-****-1006",
    "MerchantAddress1": "8320 S HARDY DRIVE",
    "MerchantCity": "TEMPE",
    "MerchantName": "Pace Software",
    "MerchantPhone": "480-333-3333",
    "MerchantState": "AZ",
    "MerchantZip": "85284"
  },
  "ReferenceId": "307306500161",
  "RequestedAmount": 1.49,
  "ResponseCode": "00",
  "ResponseDescription": "APPROVAL AXS969",
  "SurchargeAmount": 0,
  "TerminalTransactionId": 21628,
  "Timestamp": "2023-03-14T06:00:27.4665118+00:00",
  "Token": null,
  "TransactionHash": null,
  "TransactionId": "000000321000970",
  "TransactionType": null,
  "Version": null
}
* */