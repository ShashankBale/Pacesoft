package com.pacesoft.sdk.module

import x.code.util.XBuild
import x.code.util.XConst

class XApiRequest(
    val crypto: Cryptos,
    val apiKey: String,
    val body: PsApiReq,
    val bodyOg: Any
) {
    fun getHeaders(): Map<String, String> {
        val mapOf: HashMap<String, String> = hashMapOf(
            XConst.API_HEADER_TAG_API_KEY to apiKey,
            XConst.API_HEADER_TAG_IV to crypto.iv,
        )

        if (XBuild.isInternalTesting()) {
            mapOf[XConst.API_HEADER_TAG_TEMP_DK] = crypto.dk
            mapOf[XConst.API_HEADER_TAG_TEMP_TK] = crypto.tk
        }

        return mapOf
    }
}