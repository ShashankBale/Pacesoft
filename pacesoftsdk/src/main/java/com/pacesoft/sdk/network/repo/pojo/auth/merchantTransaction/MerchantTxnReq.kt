package com.pacesoft.sdk.network.repo.pojo.auth.merchantTransaction

import com.google.gson.annotations.SerializedName

data class MerchantTxnReq(
    @SerializedName("skip") val skip: Int?,
    @SerializedName("take") val take: Int?,
    @SerializedName("sortColumn") val sortColumn: String?,
    @SerializedName("SortOrder") val sortOrder: Int?,
    @SerializedName("searchText") val searchText: String?,
    @SerializedName("term") val term: String?,
)