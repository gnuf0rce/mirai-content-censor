package io.github.gnuf0rce.mirai.plugin

import io.github.gnuf0rce.mirai.plugin.data.AipClientConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

internal class AipContentCensorToolKtTest {

    private val client by lazy {
        val properties = Properties().apply { load(File("local.properties").inputStream()) }
        AipContentCensor(config = object : AipClientConfig {
            override val appId: String by properties
            override val apiKey: String by properties
            override val secretKey: String by properties
            override val connectionTimeoutInMillis: Int = 30 * 1000
            override val socketTimeoutInMillis: Int = 30 * 1000
            override val proxy: String = ""
        })
    }

    @Test
    fun handle(): Unit = runBlocking {
        client.textCensorUserDefined("我是你爹").parser().also(::println)
    }
}