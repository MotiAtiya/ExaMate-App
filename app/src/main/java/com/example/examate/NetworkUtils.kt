package com.example.examate

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url
import java.security.cert.CertificateException
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object NetworkUtils {

    private const val BASE_URL = "https://us-central1-exammate-6aebc.cloudfunctions.net/"

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> =
                    arrayOf()
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier(HostnameVerifier { _, _ -> true })

            return builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(getUnsafeOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: ApiService = retrofit.create(ApiService::class.java)

    fun postRequest(relativeUrl: String, body: Any, callback: (JsonElement?) -> Unit) {
        apiService.postRequest(relativeUrl, body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val responseString = responseBody.string()
                        val jsonElement: JsonElement = JsonParser.parseString(responseString)
                        callback(jsonElement)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(null)
            }
        })
    }

    fun uploadFile(relativeUrl: String, filePart: MultipartBody.Part, callback: (ResponseBody?) -> Unit) {
        apiService.uploadFile(relativeUrl, filePart).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                callback(response.body())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(null)
            }
        })
    }

    private interface ApiService {
        @POST
        fun postRequest(@Url url: String, @Body body: Any): Call<ResponseBody>

        @Multipart
        @POST
        fun uploadFile(@Url url: String, @Part file: MultipartBody.Part): Call<ResponseBody>
    }
}
