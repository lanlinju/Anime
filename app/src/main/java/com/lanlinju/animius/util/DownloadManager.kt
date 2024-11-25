package com.lanlinju.animius.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.IOException
import java.util.concurrent.TimeUnit

object DownloadManager {
    private const val FAKE_BASE_URL = "http://www.example.com"

    private val client = OkHttpClient.Builder()
//        .addInterceptor(interceptor)
        .readTimeout(1L, TimeUnit.MINUTES)
        .build()

    private fun apiCreator(): Api {
        val retrofit = Retrofit.Builder()
            .baseUrl(FAKE_BASE_URL)
            .client(client)
            .build()
        return retrofit.create(Api::class.java)
    }

    private val api = apiCreator()

    suspend fun request(
        url: String,
        header: Map<String, String> = emptyMap()
    ): Response<ResponseBody> {
        return api.get(url, header)
    }

    suspend fun getHtml(url: String, headers: Map<String, String> = emptyMap()): String {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).headers(headers.toHeaders()).get().build()
            val response = client.newCall(request).execute()
            var html: String
            if (response.isSuccessful) {
                response.body!!.let { body ->
                    html = body.charStream().readText()
                }
            } else {
                throw IOException(response.toString())
            }
            html
        }
    }

    private fun Map<String, String>.toHeaders(): Headers {
        val builder = Headers.Builder()
        if (isEmpty()) return builder.build()

        for ((name, value) in this) {
            builder.add(name, value)
        }
        return builder.build()
    }
}

interface Api {

    @GET
    @Streaming
    suspend fun get(
        @Url url: String,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>
}

/*
val interceptor = Interceptor { chain: Interceptor.Chain ->
    var request = chain.request()

    if (request.url.toString().contains("silisili")) {
        request = request.newBuilder()
            .addHeader("Cookie", "silisili=on;path=/;max-age=86400")
            .build()
    }

    chain.proceed(request)
}*/



