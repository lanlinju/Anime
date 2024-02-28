package com.sakura.anime.util

import com.sakura.anime.data.remote.parse.AnimeSource
import com.sakura.download.utils.decrypt
import java.util.Base64
/**
 * 先Base64解码数据，然后再AES解密
 */
fun AnimeSource.decryptData(data: String, key: String, iv: String):String {
    val bytes = Base64.getDecoder().decode(data.toByteArray())
    val debytes = bytes.decrypt(key, iv)
    return debytes.decodeToString()
}