package com.lanlinju.animius.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

/**
 * Network util
 */
fun createHttpClient() = HttpClient(OkHttp) {
    install(HttpCookies)
    install(HttpTimeout) {
        socketTimeoutMillis = 30_000L
        connectTimeoutMillis = 30_000L
    }
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                message.log("HttpClient")
            }
        }
        level = LogLevel.INFO
    }
}

object DownloadManager {
    val httpClient = createHttpClient()

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
        val html = httpClient.get(url) {
            headers {
                headers.forEach { (key, value) ->
                    append(key, value)
                }
            }
        }.bodyAsText()
        return html
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



