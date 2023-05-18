package com.pacesoft.sdk.base

class SKBException : Exception {

    private var returnCode = 0
    private var function: String? = null

    constructor(result: Int, function: String): super("Error when executing SKB function $function: $result") {

        returnCode = result
        this.function = function
    }

    fun getReturnCode(): Int {
        return returnCode
    }

    fun getFunction(): String? {
        return function
    }
}