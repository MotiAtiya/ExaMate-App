package com.example.examate

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    fun getRequest(relativeUrl: String, callback: (JsonElement?) -> Unit) {
        apiService.getRequest(relativeUrl).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val responseString = responseBody.string()
                        val jsonElement: JsonElement = JsonParser.parseString(responseString)
                        callback(jsonElement)
                    } else {
                        Log.d("GET request", "Response body is null")
                        callback(null)
                    }
                } else {
                    Log.d("GET request", "Response not successful: ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("GET request", t.toString())
                callback(null)
            }
        })
    }

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
                        Log.d("POST request", "Response body is null")
                        callback(null)
                    }
                } else {
                    Log.d("POST request", "Response not successful: ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("POST request", t.toString())
                callback(null)
            }
        })
    }


    private interface ApiService {
        @GET
        fun getRequest(@Url url: String): Call<ResponseBody>

        @POST
        fun postRequest(@Url url: String, @Body body: Any): Call<ResponseBody>
    }
}


// Example of Using:

//// Make the GET request
//NetworkUtils.getRequest("getAllClasses") { jsonElement ->
//    if (jsonElement != null) {
//        Log.d("GET response", jsonElement.toString())
//    } else {
//        Log.d("GET response", "Failed to get response")
//    }
//}
//
//// Prepare the POST request body
//val requestBody = mapOf("name" to "myname")
//
//// Make the POST request
//NetworkUtils.postRequest("createNewClass", requestBody) { jsonElement ->
//    if (jsonElement != null) {
//        Log.d("POST response", jsonElement.toString())
//    } else {
//        Log.d("POST response", "Failed to get response")
//    }
//}