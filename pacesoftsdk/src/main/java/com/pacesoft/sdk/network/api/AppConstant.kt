package com.pacesoft.sdk.network.api

object AppConstant {

    private var BASE_URL="";
    private var CONNECTION_TIME_OUT:Long = 30000
    private var NETWORK_CACHE_DIR = "app_cache"
    private var NETWORK_CACHE_SIZE :Long = 10 * 1024 * 1024
    internal fun setBaseURL(base_url : String){
        BASE_URL = base_url
    }
    internal fun getBaseURL():String{
        return BASE_URL
    }
    internal fun setTimeOut(timeout : Long){
        CONNECTION_TIME_OUT = timeout
    }
    internal fun getTimeOut():Long{
        return CONNECTION_TIME_OUT
    }
    internal fun setCacheSize(size : Long){
        NETWORK_CACHE_SIZE = size
    }
    internal fun getCacheSize():Long{
        return NETWORK_CACHE_SIZE
    }
    internal fun setAppCacheDir(cache_dir : String){
        NETWORK_CACHE_DIR = cache_dir
    }
    internal fun getAppCacheDir():String{
        return NETWORK_CACHE_DIR
    }

}