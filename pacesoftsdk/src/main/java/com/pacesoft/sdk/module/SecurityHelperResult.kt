package com.pacesoft.sdk.module

data class SecurityHelperResult(
    val errorMsg: String?,
    val encryptedData: String,
    val strJsonDecryptedData: String?
) {
    fun toBaseRequest(): PsApiReq {
        return PsApiReq(message = encryptedData)
    }

    /* fun <T> toResponse(reified O) {
             decryptedData.convert<String,>()
     }*/

//    {message = "sjadfkjsdkaljflksdajfkl"}
}