package com.sakura.anime.application

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


@HiltAndroidApp
class AnimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        disableSSLCertificateVerify()
        _instance = this

    }

    companion object {
        private lateinit var _instance: Application

        fun getInstance(): Context {
            return _instance
        }
    }

    private fun disableSSLCertificateVerify() {
        HttpsURLConnection.setDefaultSSLSocketFactory(getUnsafeSslSocketFactory())
        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
    }

    private fun getUnsafeSslSocketFactory(): SSLSocketFactory {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            return sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}