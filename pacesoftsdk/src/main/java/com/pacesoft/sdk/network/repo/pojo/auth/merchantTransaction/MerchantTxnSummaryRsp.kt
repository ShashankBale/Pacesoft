package com.pacesoft.sdk.network.repo.pojo.auth.merchantTransaction

import com.google.gson.annotations.SerializedName
import com.pacesoft.sdk.module.BasePsApiRspBody

class MerchantTxnSummaryRsp : BasePsApiRspBody() {
    @SerializedName("Status")
    val status: Boolean? = null

    @SerializedName("Count")
    val count: Int? = null

    @SerializedName("Results")
    val results: List<Result?>? = null

    data class Result(
        @SerializedName("Response") val response: String? = null,
        @SerializedName("Customer") val customer: String? = null,
        @SerializedName("Status") val status: String? = null,
        @SerializedName("TypeOfTransaction") val typeOfTransaction: String? = null,
        @SerializedName("AmountTotal") val amountTotal: Double? = null,
        @SerializedName("DateTime") val dateTime: String? = null,
        @SerializedName("CardLast4Digit") val cardLast4Digit: String? = null,
        @SerializedName("CardBrand") val cardBrand: String? = null,
        @SerializedName("TransactionId") val transactionId: Long? = null,
        @SerializedName("ClientName") val clientName: String? = null,
    )
}
