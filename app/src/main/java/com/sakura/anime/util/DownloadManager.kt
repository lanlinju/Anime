package com.sakura.anime.util

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.concurrent.TimeUnit


object DownloadManager {
    private val httpClient = HttpClient(OkHttp) {
        engine {
            config {
                readTimeout(1L, TimeUnit.MINUTES)
                sslSocketFactory(createSSLSocketFactory(), TrustAllCerts())
                hostnameVerifier { _, _ -> true }
            }
        }
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

    suspend fun request(url: String): Response<ResponseBody> {
        return httpClient.get(url).body<Response<ResponseBody>>()
    }
}
