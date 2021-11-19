package io.github.gnuf0rce.mirai.plugin

import io.github.gnuf0rce.mirai.plugin.data.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.http.*
import kotlinx.coroutines.*
import xyz.cssxsh.baidu.*
import xyz.cssxsh.baidu.aip.*
import xyz.cssxsh.baidu.aip.censor.*
import xyz.cssxsh.baidu.exption.*
import java.net.*

internal val censor: AipContentCensor by lazy {
    AipContentCensor(client = object : BaiduApiClient(config = config) {
        // TODO: no opt null
        override var accessTokenValue: String?
            get() = ContentCensorToken.accessToken.takeIf { it.isNotBlank() }
            set(value) { ContentCensorToken.accessToken = value.orEmpty() }

        override var refreshTokenValue: String?
            get() = ContentCensorToken.refreshToken.takeIf { it.isNotBlank() }
            set(value) { ContentCensorToken.refreshToken = value.orEmpty() }

        override val accessToken: String
            get() {
                return try {
                    super.accessToken
                } catch (cause: NotTokenException) {
                    runBlocking {
                        refresh().accessToken
                    }
                }
            }
        override val client: HttpClient = super.client.config {
            install(HttpTimeout) {
                socketTimeoutMillis = ContentCensorConfig.socketTimeoutInMillis
                connectTimeoutMillis = ContentCensorConfig.connectionTimeoutInMillis
                requestTimeoutMillis = ContentCensorConfig.connectionTimeoutInMillis
            }
            engine {
                this as OkHttpConfig
                config {
                    if (ContentCensorConfig.proxy.isNotBlank()) proxy(with(Url(ContentCensorConfig.proxy)) {
                        val type = when (protocol) {
                            URLProtocol.SOCKS -> Proxy.Type.SOCKS
                            URLProtocol.HTTP -> Proxy.Type.HTTP
                            else -> throw IllegalArgumentException("Proxy: $this")
                        }
                        Proxy(type, InetSocketAddress(host, port))
                    })
                }
            }
        }
    })
}

internal val logger get() = MiraiAntiPornPlugin.logger

internal val config get() = ContentCensorConfig

private fun CensorItem.render(): String {
    return when (this) {
        is CensorItem.Star -> "(${datasetName}, ${probability}, ${name})"
        is CensorItem.Hit -> "(${datasetName}, ${probability}, ${words})"
        is CensorResult.Image.Record -> "(${datasetName}, ${probability}, ${(stars + hits).joinToString { it.render() }})"
    }
}

fun CensorResult.render(): String {
    return when (this) {
        is CensorResult.Image -> data.joinToString { record ->
            "${record.message}: ${(record.stars + record.hits).map { it.render() }}"
        }
        is CensorResult.Text -> data.joinToString { record ->
            "${record.message}: ${record.hits.map { it.render() }}"
        }
        is CensorResult.Video -> frames.joinToString { record ->
            "${record.frameThumbnailUrl}: ${record.conclusion}"
        }
        is CensorResult.Voice -> data.joinToString { record ->
            "${record.text}: ${record.conclusion}"
        }
    }
}

fun CensorResult.count(): Int {
    return when (this) {
        is CensorResult.Image -> data.size
        is CensorResult.Text -> data.size
        is CensorResult.Video -> frames.size
        is CensorResult.Voice -> data.size
    }
}

fun CensorResult.message(): String {
    return when (this) {
        is CensorResult.Image -> data.joinToString { it.message }
        is CensorResult.Text -> data.joinToString { it.message }
        is CensorResult.Video -> frames.joinToString { record -> record.data.joinToString { it.message } }
        is CensorResult.Voice -> data.joinToString { record -> record.auditData.joinToString { it.message } }
    }
}