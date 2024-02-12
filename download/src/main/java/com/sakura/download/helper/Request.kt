package com.sakura.download.helper

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Streaming
import retrofit2.http.Url

internal const val FAKE_BASE_URL = "http://www.example.com"

internal fun apiCreator(client: OkHttpClient): Api {
    val retrofit = Retrofit.Builder()
        .baseUrl(FAKE_BASE_URL)
        .client(client)
        .build()
    return retrofit.create(Api::class.java)
}

internal interface Api {

    @GET
    @Streaming
    suspend fun get(
        @Url url: String,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>
}