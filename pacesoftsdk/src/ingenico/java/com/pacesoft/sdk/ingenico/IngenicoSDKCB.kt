package com.pacesoft.sdk.ingenico

interface IngenicoSDKCB {

    fun onInitialized()
    fun onInitializationFailed()
    fun showException(msg:String)
    fun onError(msg:String)
    fun onTimeout()
    fun onMSRCardRead()
    fun onCardInserted(cardType: Int)
    fun onCardPass(cardType: Int)
//    fun onOnlineProcess(param :LinkedHashMap<String,String?>)
    fun onOnlineProcess(msg: String)
    fun onEndProcess(result: Int, transData: String?)
}