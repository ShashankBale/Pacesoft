package com.pacesoft.sdk.network.repo.pojo.trasaction.sale

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.pacesoft.sdk.module.BasePsApiRspBody
import kotlinx.android.parcel.Parcelize

@Parcelize
 class SaleTxnV2RspOld : BasePsApiRspBody(), Parcelable {
    @SerializedName("TerminalTransactionId") val terminalTransactionId: Long? = null
    @SerializedName("Approved") val approved: Boolean? = null
    @SerializedName("TransactionId") val transactionId: String? = null
    @SerializedName("ResponseCode") val responseCode: String? = null
    @SerializedName("ReferenceId") val referenceId: String? = null
    @SerializedName("AuthCode") val authCode: String? = null
    @SerializedName("ResponseDescription") val responseDescription: String? = null
    @SerializedName("RequestedAmount") val requestedAmount: Double? = null
    @SerializedName("CardHolder") val cardHolder: String? = null
    @SerializedName("EntryMethod") val entryMethod: String? = null
    @SerializedName("Timestamp") val timestamp: String? = null
    @SerializedName("MaskedPan") val maskedPan: String? = null
    @SerializedName("CurrencyCode") val currencyCode: String? = null
    @SerializedName("AuthorizedAmount") val authorizedAmount: String? = null
    @SerializedName("PAR") val pAR: String? = null
    @SerializedName("Version") val version: String? = null
    @SerializedName("PartialAuth") val partialAuth: String? = null
    @SerializedName("Token") val token: String? = null
    @SerializedName("Receipt") val receipt: Receipt? = null
    @SerializedName("CVVResultCode") val cVVResultCode: String? = null
    @SerializedName("AVSResultCode") val aVSResultCode: String? = null
    @SerializedName("AVSResponse") val aVSResponse: String? = null

   @Parcelize
    data class Receipt(
        @SerializedName("MaskedPan") val maskedPan: String?,
        @SerializedName("MerchantName") val merchantName: String?,
        @SerializedName("EntryMethod") val entryMethod: String?
    ) : Parcelable
 }