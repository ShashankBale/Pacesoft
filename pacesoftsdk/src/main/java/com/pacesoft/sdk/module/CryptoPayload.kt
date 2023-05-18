package com.pacesoft.sdk.module

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.Calendar

@Parcelize
data class CryptoPayload(
    @SerializedName("IV") val iv: String?,
    @SerializedName("DK") val dk: String?,
    @SerializedName("TK") val tk: String?,
    @SerializedName("ApiKey") val apiKey: String?,
    val cts: Long = Calendar.getInstance().timeInMillis, //cts = Created timestamp
): Parcelable, Comparable<CryptoPayload> {
    override fun compareTo(other: CryptoPayload): Int {
        return cts.compareTo(other.cts);
    }

    override fun toString(): String {
        return "$iv==$cts"
    }
}