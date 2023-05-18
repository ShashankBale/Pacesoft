package com.pacesoft.sdk.agnos

interface AgnosPayCallbacks {
    fun onInitializeCompleted(purchase: Transaction)
    fun onInitializeFailed(message: String)
    fun onCardPresent()
    fun onCardRemoved()
    fun transactionIsReady()
    fun transactionDeclined(msg:String)
    fun txnCancelled(error:String)
}