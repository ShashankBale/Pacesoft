package x.code.util.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import x.code.app.XCodeApp

class XNetConnStatus(context: Context) : LiveData<Boolean>() {

    private var mConnMngr: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val mConnMngrNetCb: ConnectivityManager.NetworkCallback by lazy {
        val connMngrNetCb = object : ConnectivityManager.NetworkCallback() {

            override fun onLost(network: Network) {
                super.onLost(network)
                postValue(false)
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                postValue(true)
            }
        }

        connMngrNetCb
    }


    override fun onActive() {
        super.onActive()
        //postValue(true)

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                mConnMngr.registerDefaultNetworkCallback(mConnMngrNetCb)
            }
            else -> {
                val builder = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)

                mConnMngr.registerNetworkCallback(builder.build(), mConnMngrNetCb)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        try {
            //postValue(false)
            mConnMngr.unregisterNetworkCallback(mConnMngrNetCb)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        @Suppress("DEPRECATION")
        fun isConnected(): Boolean {
            var result = false
            val cm =
                XCodeApp.ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cm?.run {
                    cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                        result = when {
                            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                            else -> false
                        }
                    }
                }
            } else {
                cm?.run {
                    cm.activeNetworkInfo?.run {
                        if (type == ConnectivityManager.TYPE_WIFI) {
                            result = true
                        } else if (type == ConnectivityManager.TYPE_MOBILE) {
                            result = true
                        }
                    }
                }
            }
            return result
        }

    }
}