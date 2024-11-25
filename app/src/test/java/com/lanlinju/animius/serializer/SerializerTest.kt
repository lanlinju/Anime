package com.lanlinju.animius.serializer

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class SerializerTest {

    @Test
    fun testEncode() {
        // 创建 Person 对象
        val person = Person(name = "Tom", age = 20)

        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        // 将 Person 对象序列化为 JSON 字符串
        val jsonString = json.encodeToString(person)

        // 验证序列化结果
        assertEquals("""{"name":"Tom","age":20}""", jsonString)
    }

    @Test
    fun testDecode() {
        // JSON 字符串
        val jsonString = """{"name":"Tom","age":20}"""

        // 将 JSON 字符串反序列化为 Person 对象
        val person = Json.decodeFromString<Person>(jsonString)

        // 验证反序列化结果
        assertEquals("Tom", person.name)
        assertEquals(20, person.age)
    }

    @Serializable
    data class Person(
        val name: String = "Tom",
        val age: Int = 20
    )
}

