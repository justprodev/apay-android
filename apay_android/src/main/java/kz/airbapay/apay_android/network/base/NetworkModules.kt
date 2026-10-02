package kz.airbapay.apay_android.network.base

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kz.airbapay.apay_android.data.utils.DataHolder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val SSL = "SSL"

internal fun createOkHttpClientCoroutine(
    interceptor: BaseInterceptor,
) = createOkHttpClient(
    interceptor,
    initLoggingInterceptor()
)

internal fun createOkHttpClientLogging(
    interceptor: BaseInterceptor
) = createOkHttpClient(
    interceptor,
    null
)


private fun createOkHttpClient(
    interceptor: Interceptor,
    httpLoggingInterceptor: HttpLoggingInterceptor?
): OkHttpClient {

    val trustAllCerts = initTrustManager()
    val sslSocketFactory: SSLSocketFactory = initSslSocketFactory(trustAllCerts)
    val okHttpClientBuilder = initOkHttpBuilder(
        sslSocketFactory,
        trustAllCerts,
        interceptor,
        httpLoggingInterceptor
    )

    return okHttpClientBuilder.build()
}

private fun initLoggingInterceptor(): HttpLoggingInterceptor {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.level = BODY
    return httpLoggingInterceptor
}

private fun initTrustManager(): Array<TrustManager> = arrayOf(
    object : X509TrustManager {
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

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }
)

private fun initSslSocketFactory(trustAllCerts: Array<TrustManager>): SSLSocketFactory {
    val sslContext = SSLContext.getInstance(SSL)
    sslContext.init(null, trustAllCerts, SecureRandom())
    return sslContext.socketFactory
}

private fun initOkHttpBuilder(
    sslSocketFactory: SSLSocketFactory,
    trustAllCerts: Array<TrustManager>,
    interceptor: Interceptor,
    httpLoggingInterceptor: HttpLoggingInterceptor?,
): OkHttpClient.Builder = OkHttpClient
    .Builder()
    .apply {
        sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
        hostnameVerifier { _, _ -> true }
        connectTimeout(10L, TimeUnit.SECONDS)
        readTimeout(300L, TimeUnit.SECONDS)
        writeTimeout(300L, TimeUnit.SECONDS)
        addInterceptor(interceptor)
        httpLoggingInterceptor?.let { addInterceptor(it) }
    }

internal fun provideRetrofit(
    context: Context
): Retrofit? {
    if (DataHolder.baseUrl.isBlank()) {
        Log.e("AirbaPay", "ВНИМАНИЕ! Не была выполнена функция initOnCreate()")
        return null
    }
    val interceptor = BaseInterceptor(context)
    val gson = provideGson()
    val clientCoroutines: OkHttpClient = createOkHttpClientCoroutine(interceptor)
    return provideRetrofit(clientCoroutines, gson)
}

internal fun provideRetrofitLoggly(
    context: Context
): Retrofit? {
    if (DataHolder.baseUrl.isBlank()) {
        Log.e("AirbaPay", "ВНИМАНИЕ! Не была выполнена функция initOnCreate()")
        return null
    }
    val interceptor = BaseInterceptor(context)
    val gson = provideGson()
    val clientCoroutines: OkHttpClient = createOkHttpClientLogging(interceptor)
    return provideRetrofit(clientCoroutines, gson)
}

private fun provideRetrofit(
    client: OkHttpClient,
    gson: Gson
): Retrofit {
    return Retrofit.Builder()
        .baseUrl(DataHolder.baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

internal fun provideGson(): Gson = GsonBuilder().create()



