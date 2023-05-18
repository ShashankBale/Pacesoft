package com.pacesoft.sdk.network.api

import android.content.Context
import android.content.pm.PackageInfo
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pacesoft.sdk.app.PaceSoftSdk
import com.pacesoft.sdk.network.repo.service.ApiInterface
import com.pacesoft.sdk.network.util.UserSessionExpirableCallback
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.BufferedSource
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import x.code.util.XBuild
import java.io.File
import java.nio.charset.Charset
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


object XApi {
    private var mApiInterfaces: ApiInterface? = null
    private var mUserSessionExpiry: UserSessionExpirableCallback? = null

    private val BASE_URL = "https://google.co.in"

    private val appVersionName: String by lazy {
        val ctx = PaceSoftSdk.ctx
        val pInfo: PackageInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        pInfo.versionName
    }

    private val interceptor4Req: Interceptor by lazy {
        Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.header("from", "android_$appVersionName")
            chain.proceed(requestBuilder.build())
        }
    }

    /**
     * To connect network service
     */
    fun getApiServiceInterface(): ApiInterface {
        return if (mApiInterfaces != null) {
            mApiInterfaces as ApiInterface
        } else {
            getRetrofitClientV1()
            mApiInterfaces as ApiInterface
        }
    }


    private fun getRetrofitClientV1() {
        try {
            val mHttpClient: OkHttpClient.Builder = OkHttpClient.Builder()
                .addInterceptor(interceptor4Req)
                .addInterceptor(interceptorForUserSessionExpiry())

                .connectTimeout(20, TimeUnit.SECONDS) //time to establish connection
                .writeTimeout(40, TimeUnit.SECONDS) //the time to send req to server
                .readTimeout(60, TimeUnit.SECONDS) //the time you wait for the response


            val gson = GsonBuilder().setLenient().create()
            val retrofit =
                Retrofit.Builder().run {
                    baseUrl(BASE_URL)
                    addConverterFactory(GsonConverterFactory.create(gson))
                    addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    client(mHttpClient.build())
                    build()
                }
            mApiInterfaces = retrofit.create(ApiInterface::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     *  build retrofit object
     */
    private fun getRetrofitClientV2() {
        try {
            val context = PaceSoftSdk.ctx

            /*val mHttpClient: OkHttpClient.Builder = OkHttpClient.Builder()
                //.addInterceptor(getHttpLoggerInterceptor())
                .addInterceptor(ChuckInterceptor(context))
                .addInterceptor(interceptor4Req)
                .addInterceptor(interceptorForUserSessionExpiry())

                .connectTimeout(20, TimeUnit.SECONDS) //time to establish connection
                .writeTimeout(40, TimeUnit.SECONDS) //the time to send req to server
                .readTimeout(60, TimeUnit.SECONDS) //the time you wait for the response


            val build: OkHttpClient = mHttpClient.build()*/

            val unsafeOkHttpClient: OkHttpClient = getUnsafeOkHttpClient(context)


            val gson = GsonBuilder().setLenient().create()
            val retrofit =
                Retrofit.Builder().run {
                    baseUrl(BASE_URL)
                    addConverterFactory(GsonConverterFactory.create(gson))
                    addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    client(unsafeOkHttpClient)
                    build()
                }
            mApiInterfaces = retrofit.create(ApiInterface::class.java)


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getUnsafeOkHttpClient(context: Context?): OkHttpClient {
        try {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val httpCacheDirecotory = File(context?.cacheDir, AppConstant.getAppCacheDir())
            val cache = Cache(httpCacheDirecotory, AppConstant.getCacheSize())

            // Create a trust manager that does not validate certificate chains

            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
            })


            // Install the all-trusting trust manager
            val sslContext: SSLContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory: SSLSocketFactory = sslContext.getSocketFactory();

            val builder: OkHttpClient.Builder = OkHttpClient.Builder();
            builder.cache(cache)
                .addInterceptor(logging)
                .readTimeout(AppConstant.getTimeOut(), TimeUnit.MILLISECONDS)
                .connectTimeout(AppConstant.getTimeOut(), TimeUnit.MILLISECONDS)
                //.protocols(listOf(Protocol.HTTP_2))
                //.addInterceptor(getHttpLoggerInterceptor())
                //.addInterceptor(interceptor4Req)
                //.addInterceptor(interceptorForUserSessionExpiry())

                .connectTimeout(20 * 2, TimeUnit.SECONDS) //time to establish connection
                .writeTimeout(40 * 2, TimeUnit.SECONDS) //the time to send req to server
                .readTimeout(60 * 2, TimeUnit.SECONDS) //the time you wait for the response


            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager);
            builder.hostnameVerifier { hostname, session -> true; };

            val okHttpClient: OkHttpClient = builder.build();
            return okHttpClient;
        } catch (e: Exception) {
            throw RuntimeException(e);
        }
    }

    private fun interceptorForUserSessionExpiry(): Interceptor {
        return object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val original: Request = chain.request()
                val request = original.newBuilder()
                    .method(original.method, original.body)
                    .build()

                val rsp = chain.proceed(request)

                //var tryCount = 0

                when (rsp.code) {
                    401 -> showUserSessionOut(rsp)
                    403 -> showUserSessionOut(rsp)
                    404 -> return rsp //showApiUnreachable(rsp)
                    422 -> return rsp //showApiUnreachable(rsp)
                    502 -> return rsp //showServerDown(rsp)
                    504 -> return rsp //showServerDown(rsp)

                    else -> {
                        rsp.body ?: return rsp
                        if (!rsp.isSuccessful) {
                            showApiUnreachable(rsp)
                        } else {
                            val source: BufferedSource = rsp.body!!.source()
                            source.request(Long.MAX_VALUE) // Buffer the entire body.

                            val buffer: Buffer = source.buffer()
                            val charset: Charset? =
                                rsp.body?.contentType()?.charset(x.code.util.view.text.XStr.csUtf8)
                            if (charset != null) {
                                val json: String = buffer.clone().readString(charset)
                                val obj = JsonParser().parse(json)
                                if (obj is JsonObject && obj.has("Status")) {
                                    val errorStatus = obj["Status"].asString
                                    val reason = obj["Reason"].asString
                                    if (errorStatus.equals("FAIL", ignoreCase = true)
                                        && reason.equals("SESSION EXPIRED.", ignoreCase = true)
                                    ) {
                                        showUserSessionOut(rsp)
                                    }
                                }
                            }
                        }
                    }
                }

                return rsp

            }
        }
    }

    private fun showApiUnreachable(rsp: Response) {
        mUserSessionExpiry?.showServerDown("API : " + rsp.request.url)
    }

    private fun showUserSessionOut(rsp: Response) {
        if (XBuild.isInternalTesting())
            mUserSessionExpiry?.onUserSessionOut("Session expired from API : " + rsp.request.url)
        else
            mUserSessionExpiry?.onUserSessionOut("Session expired...")
    }

    fun initUserSessionExpiry(userSessionExpire: UserSessionExpirableCallback) {
        mUserSessionExpiry = userSessionExpire
    }
}