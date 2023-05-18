package com.pacesoft.sdk.module

import x.code.util.view.text.XStr
import java.util.*

data class Cryptos(
    val iv: String,
    val dk: String,
    val tk: String,
    val cts: Long = Calendar.getInstance().timeInMillis, //cts = Created timestamp
) {
    companion object {
        /*fun convert(items: List<CryptoPayload>): List<Cryptos> {
            val al = ArrayList<Cryptos>(items.size)
            items.forEach {
                if (XStr.isEmpty(it.iv)) return@forEach
                if (XStr.isEmpty(it.dk)) return@forEach
                if (XStr.isEmpty(it.tk)) return@forEach

                al.add(
                    Cryptos(
                        iv = it.iv ?: "",
                        dk = it.dk ?: "",
                        tk = it.tk ?: "",
                        cts = Calendar.getInstance().timeInMillis
                    )
                )
            }
            return al
        }*/

        fun convert(item: CryptoPayload?): Cryptos? {
            item ?: return null
            if (XStr.isEmpty(item.iv)) return null
            if (XStr.isEmpty(item.dk)) return null
            if (XStr.isEmpty(item.tk)) return null
            if (XStr.isEmpty(item.apiKey)) return null

            return Cryptos(
                iv = item.iv ?: "",
                dk = item.dk ?: "",
                tk = item.tk ?: "",
                cts = Calendar.getInstance().timeInMillis
            )
        }

    }
}