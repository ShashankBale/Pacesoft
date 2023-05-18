package com.pacesoft.sdk.agnos

interface CardListner {
    fun cardIsReady(encryptedData:String,params:LinkedHashMap<String,String?>)
    fun cardDeclined(params:HashMap<String,String?>, msg:String)
    fun txnCancelled(error:String)
}