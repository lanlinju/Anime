package com.lanlinju.download.core

import com.lanlinju.download.utils.closeQuietly
import java.net.URI

class M3u8Parser(param: DownloadParam) {

    private var uri: URI
    private var originalUrl: String
    private var redirectUrl: String = ""
    private val tsUrlList = ArrayList<String>()
    private var key: String = ""
    private var iv: String = "0000000000000000"

    init {
        originalUrl = param.url
        uri = URI.create(originalUrl)
    }

    private fun isRedirectUrl(lines: List<String>): Boolean {
        val list = lines.filter { !it.startsWith("#") }
        return list[0].contains(".m3u8")
    }

    private fun parseRedirectUrl(lines: List<String>): String {
        val urls = ArrayList<Pair<Int, String>>()

        for (i in 1 until lines.size step 2) {
            if (lines[i].contains("BANDWIDTH")) {
                val bandwidth = parseBandwidth(lines[i])
                urls.add(Pair(bandwidth.toInt(), lines[i + 1]))
            }
        }
        // 选择最高码率下载
        val maxBandwidth = urls.maxByOrNull { it.first }
        val maxUrl = maxBandwidth!!.second

        redirectUrl = maxUrl.getFullUrl()
        return redirectUrl
    }

    private fun parseBandwidth(s: String): String {
        val bandwidthRegex = "BANDWIDTH=(.*?),".toRegex()
        return bandwidthRegex.find(s)!!.groupValues[1].trim()
    }

    private fun parseIv(s: String): String {
        if (s.contains("IV")) {
            val ivRegex = "IV=0x(.*?)".toRegex()
            ivRegex.find(originalUrl)?.let { iv = it.groupValues[1].trim() }
        }
        return iv
    }

    private fun parseKeyUrl(s: String): String {
        val keyRegex = "URI=\"(.*?)\"".toRegex()
        val keyUrl = keyRegex.find(s)!!.groupValues[1]

        return keyUrl.getFullUrl()
    }

    private fun parseTsUrl(tsUrl: String) {
        tsUrlList.add(tsUrl.getFullUrl())
    }

    private fun String.getFullUrl(): String {
        if (this.contains("http")) return this

        return if (!this.startsWith("/")) {
            val url = redirectUrl.ifEmpty { originalUrl }
            url.substring(0, url.lastIndexOf("/") + 1) + this
        } else {
            "${uri.scheme}://${uri.host}$this"
        }
    }

    private suspend fun getKeyContent(keyUrl: String, config: DownloadConfig): String {
        val response = config.request(keyUrl, emptyMap())
        response.body()!!.charStream().use {
            key = it.readText().trim()
        }
        return key
    }

    private suspend fun parseKey(s: String, config: DownloadConfig): String {
        val keyUrl = parseKeyUrl(s)
        return getKeyContent(keyUrl, config)
    }

    fun getTsUrlList(): List<String> {
        return tsUrlList
    }

    fun getKey(): String {
        return key
    }

    fun getIv(): String {
        return iv
    }

    suspend fun parseM3u8(m3u8Url: String, config: DownloadConfig) {
        val response = config.request(m3u8Url, emptyMap())
        try {
            if (!response.isSuccessful || response.body() == null) {
                throw RuntimeException("request failed")
            }
            val lines = response.body()!!.byteStream()
                .bufferedReader()
                .readLines()

            if (isRedirectUrl(lines)) {
                parseM3u8(parseRedirectUrl(lines), config)
                return
            }

            lines.forEach { line ->
                if (!line.startsWith("#")) {
                    parseTsUrl(line)
                } else {
                    if (line.contains("#EXT-X-KEY")) {
                        parseKey(line, config)
                        parseIv(line)
                    }
                }
            }
        } finally {
            response.closeQuietly()
        }
    }
}