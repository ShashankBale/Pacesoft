package com.pacesoft.sdk.network.repo.pojo.auth.merchantTransaction

import com.google.gson.annotations.SerializedName
import com.pacesoft.sdk.module.BasePsApiRspBody

class MerchantTxnRsp : BasePsApiRspBody() {
    @SerializedName("Count")
    val count: Int? = null

    @SerializedName("Results")
    val results: List<Result?>? = null

    data class Result(
        @SerializedName("Id") val id: Long? = null,
        @SerializedName("Bin") val bin: Int? = null,
        @SerializedName("ClientId") val clientId: Int? = null,
        @SerializedName("TransactionType") val transactionType: String? = null,
        @SerializedName("ResponseType") val responseType: String? = null,
        @SerializedName("Status") val status: String? = null,
        @SerializedName("Name") val name: String? = null,
        @SerializedName("ClientName") val clientName: String? = null,
        @SerializedName("Currency") val currency: String? = null,
        @SerializedName("TxnAmount") val txnAmount: String? = null,
        @SerializedName("Amount") val amount: Double? = null,
        @SerializedName("OpenAt") val openAt: String? = null,
        @SerializedName("CloseAt") val closeAt: Any? = null,
        @SerializedName("SettledAt") val settledAt: Any? = null,
        @SerializedName("CardLast4Digit") val cardLast4Digit: String? = null,
        @SerializedName("InstrumentType") val instrumentType: String? = null,
        @SerializedName("TransactionId") val transactionId: Int? = null,
        @SerializedName("BatchId") val batchId: Int? = null,
        @SerializedName("IpAddress") val ipAddress: String? = null,
        @SerializedName("NwAddress") val nwAddress: String? = null,
        @SerializedName("RequestData") val requestData: String? = null,
        @SerializedName("ResponseData") val responseData: String? = null,
        @SerializedName("CreatedAt") val createdAt: String? = null,
        @SerializedName("CreatedBy") val createdBy: Int? = null,
        @SerializedName("BillToName") val billToName: String? = null,
        @SerializedName("BillToAddress1") val billToAddress1: String? = null,
        @SerializedName("BillToAddress2") val billToAddress2: String? = null,
        @SerializedName("BillToAddress3") val billToAddress3: String? = null,
        @SerializedName("BillToCountry") val billToCountry: String? = null,
        @SerializedName("BillToCity") val billToCity: String? = null,
        @SerializedName("BillToState") val billToState: String? = null,
        @SerializedName("BillToPostalCode") val billToPostalCode: String? = null,
        @SerializedName("BillToPhone") val billToPhone: String? = null,
        @SerializedName("BillToEmailAddress") val billToEmailAddress: String? = null,
        @SerializedName("ShipToName") val shipToName: String? = null,
        @SerializedName("ShipToAddress1") val shipToAddress1: String? = null,
        @SerializedName("ShipToAddress2") val shipToAddress2: String? = null,
        @SerializedName("ShipToAddress3") val shipToAddress3: String? = null,
        @SerializedName("ShipToCountry") val shipToCountry: String? = null,
        @SerializedName("ShipToCity") val shipToCity: String? = null,
        @SerializedName("ShipToState") val shipToState: String? = null,
        @SerializedName("ShipToPostalCode") val shipToPostalCode: String? = null,
        @SerializedName("ShipToPhone") val shipToPhone: String? = null,
        @SerializedName("ShipToEmailAddress") val shipToEmailAddress: String? = null,
        @SerializedName("ApprovedAmount") val approvedAmount: Double? = null,
        @SerializedName("CapturedAmount") val capturedAmount: Double? = null,
        @SerializedName("LastAction") val lastAction: String? = null,
        @SerializedName("ReferenceNumber") val referenceNumber: String? = null,
        @SerializedName("AccountFromType") val accountFromType: String? = null,
        @SerializedName("SettlementDate") val settlementDate: String? = null,
        @SerializedName("TransactionDateTime") val transactionDateTime: String? = null,
        @SerializedName("ExpirationDate") val expirationDate: String? = null,
        @SerializedName("ProcessingTime") val processingTime: Int? = null,
        @SerializedName("PaymentMethod") val paymentMethod: String? = null,
        @SerializedName("Processer") val processor: String? = null,
        @SerializedName("ReferenceGUID") val referenceGUID: Any? = null,
        @SerializedName("AuthorizationCode") val authorizationCode: String? = null,
        @SerializedName("ResponseCode") val responseCode: String? = null,
        @SerializedName("ResponseReason") val responseReason: String? = null,
        @SerializedName("Network") val network: String? = null,
        @SerializedName("TnxDateTime") val tnxDateTime: String? = null,
        @SerializedName("CVVResultCode") val cVVResultCode: String? = null,
        @SerializedName("AVSResultCode") val aVSResultCode: String? = null,
        @SerializedName("AVSResponse") val aVSResponse: String? = null,
        @SerializedName("CardBrand") val cardBrand: String? = null,
        @SerializedName("EncryptedPan") val encryptedPan: Any? = null
    )
}


/*
{
  "Count": 1,
  "Results": [
    {
      "Id": 36296,
      "Bin": 22230000,
      "ClientId": 1376,
      "TransactionType": "Sale",
      "ResponseType": "Approved",
      "Status": "Initiated",
      "Name": " ",
      "ClientName": "Bob's Coffee Shop",
      "Currency": "USD",
      "TxnAmount": "7.15",
      "Amount": 7.15,
      "OpenAt": "2023-03-04T03:16:26.7938121Z",
      "CloseAt": null,
      "SettledAt": null,
      "CardLast4Digit": "0011",
      "InstrumentType": "4",
      "TransactionId": 34343,
      "BatchId": 119,
      "IpAddress": "172.16.0.12",
      "NwAddress": "",
      "RequestData": "gpcWnbidHiqbGWBY9DcaxAgX1KnKNLAr2i6H+E+gkEau5ZnSLedQwDkG87d/FwXP9f87Y7v9dQIuZjobEo9cxWBA+KDlaIDzvEXVwbK6KK5w+E4KbQYRqJ1XswBTHsdjqbNsBa0Em7lBo0C28+s7kCr7wsE5gbY5oi0aU1tKbzEOPi8PQISmZn7pGeVEZN9l91yKmIk7YYT3TVCK5mCP9Ns/YBBEORudPF5kdDiGrHbabYgletMnR31l7CNe/kTl74Yi/fwIP8Kst5c0oJD1qF6dAB179O/52Uix/gR+4Gc=",
      "ResponseData": "70s+e5hYNNOyy5bW+BBjdMGgow9FQmYNdgrYe6vOl1ScPCrCA46iwgEf73YoEh1pq4UMeREtlBzav+qj8pKofcEURnEKpBVGJSRhn4hboa8kLWNkqh31ldreFe7ystgwRmkl1qa8NeN5tugS2anH1eQXlolxGJu8KNoffwJrWBt9bp0YUSvRZjdOHIChp5tA",
      "CreatedAt": "2023-03-04T03:16:26.7938121Z",
      "CreatedBy": 0,
      "BillToName": "",
      "BillToAddress1": "",
      "BillToAddress2": "",
      "BillToAddress3": null,
      "BillToCountry": "",
      "BillToCity": "",
      "BillToState": "",
      "BillToPostalCode": "",
      "BillToPhone": "",
      "BillToEmailAddress": "",
      "ShipToName": "",
      "ShipToAddress1": "",
      "ShipToAddress2": "",
      "ShipToAddress3": null,
      "ShipToCountry": "",
      "ShipToCity": "",
      "ShipToState": "",
      "ShipToPostalCode": "",
      "ShipToPhone": "",
      "ShipToEmailAddress": "",
      "ApprovedAmount": 7.15,
      "CapturedAmount": 7.15,
      "LastAction": "",
      "ReferenceNumber": "",
      "AccountFromType": "",
      "SettlementDate": "0001-01-01T00:00:00",
      "TransactionDateTime": "2023-03-04T03:16:26.7938121Z",
      "ExpirationDate": "1225",
      "ProcessingTime": 352,
      "PaymentMethod": "",
      "Processer": "Tsys",
      "ReferenceGUID": null,
      "AuthorizationCode": "VTLMC1",
      "ResponseCode": "00",
      "ResponseReason": "",
      "Network": "MasterCard",
      "TnxDateTime": "03/04/2023 03:16:26 AM",
      "CVVResultCode": "",
      "AVSResultCode": "0",
      "AVSResponse": "Approved",
      "CardBrand": "MasterCard",
      "EncryptedPan": null
    }
  ]
}
* */