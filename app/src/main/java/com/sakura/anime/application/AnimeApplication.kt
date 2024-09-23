package com.sakura.anime.application

import android.app.Application
import android.content.Context
import android.widget.Toast
import dagger.hilt.android.HiltAndroidApp
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
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
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                val myTrustedAnchors = arrayOfNulls<X509Certificate>(0)
                return myTrustedAnchors
            }

            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}

            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
        })

        try {
            val sc = SSLContext.getInstance("SSL")

            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { hostname, session -> true }
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

//    private fun disableSSLCertificateVerify() {
//        HttpsURLConnection.setDefaultSSLSocketFactory(getUnsafeSslSocketFactory())
//        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
//    }

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
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "error: disableSSLCertificateVerify", Toast.LENGTH_LONG).show()
            throw java.lang.RuntimeException(e)
        }
    }
}

