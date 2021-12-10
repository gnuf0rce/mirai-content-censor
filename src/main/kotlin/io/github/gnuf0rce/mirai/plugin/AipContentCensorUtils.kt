package io.github.gnuf0rce.mirai.plugin

import io.github.gnuf0rce.mirai.plugin.data.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.http.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.*
import xyz.cssxsh.baidu.*
import xyz.cssxsh.baidu.aip.*
import xyz.cssxsh.baidu.aip.censor.*
import xyz.cssxsh.baidu.exception.*
import java.net.*
import java.time.*

internal val censor: AipContentCensor by lazy {
    AipContentCensor(client = object : BaiduAipClient(config = config) {

        override var expires: OffsetDateTime by ContentCensorToken::expires

        override var accessTokenValue: String by ContentCensorToken::accessToken

        override var refreshTokenValue: String by ContentCensorToken::refreshToken

        override val accessToken: String
            get() {
                return try {
                    super.accessToken
                } catch (cause: NotTokenException) {
                    runBlocking(MiraiAntiPornPlugin.coroutineContext) {
                        if (refreshTokenValue.isBlank()) {
                            token().accessToken
                        } else {
                            refresh().accessToken
                        }
                    }
                }
            }

        override val refreshToken: String
            get() {
                return try {
                    super.refreshToken
                } catch (cause: NotTokenException) {
                    runBlocking(MiraiAntiPornPlugin.coroutineContext) {
                        token().refreshToken
                    }
                }
            }

        override val apiIgnore: suspend (Throwable) -> Boolean = { throwable ->
            when (throwable) {
                is HttpRequestTimeoutException,
                is IOException
                -> {
                    logger.warning { "AipContentCensor Ignore: $throwable" }
                    true
                }
                else -> false
            }
        }

        override val client: HttpClient = with(ContentCensorConfig) {
            if (socketTimeoutInMillis < 15_000 || connectionTimeoutInMillis < 15_000 || requestTimeoutInMillis < 15_000) {
                logger.warning { "超时时间请设置超过 15_000 ms " }
            }
            super.client.config {
                install(HttpTimeout) {
                    socketTimeoutMillis = socketTimeoutInMillis
                    connectTimeoutMillis = connectionTimeoutInMillis
                    requestTimeoutMillis = requestTimeoutInMillis
                }
                engine {
                    this as OkHttpConfig
                    config {
                        if (proxyUrl.isNotBlank()) proxy(with(Url(proxyUrl)) {
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
        }
    })
}

internal val NoCensorPermission: Permission by lazy {
    PermissionService.INSTANCE.register(
        id = MiraiAntiPornPlugin.permissionId("no-censor"),
        description = "跳过检测",
        parent = MiraiAntiPornPlugin.parentPermission
    )
}

internal val logger get() = MiraiAntiPornPlugin.logger

internal val config get() = ContentCensorConfig

internal suspend fun censor(message: MessageChain): CensorResult? {
    // Text Censor
    if (message.content.isNotBlank() && config.plain) {
        return censor.text(plain = message.content)
    }
    // Image Censor
    if (config.image) {
        for (image in message.filterIsInstance<Image>()) {
            return censor.image(url = image.queryUrl(), gif = image.imageType == ImageType.GIF)
        }
    }
    // Audio Censor
    if (config.audio) {
        for (audio in message.filterIsInstance<OnlineAudio>()) {
            val url = audio.urlForDownload
            val format = audio.codec.formatName
            return censor.voice(url = url, format = format, rawText = true, split = false)
        }
    }
    // Forward
    for (node in (message.firstIsInstanceOrNull<ForwardMessage>() ?: return null).nodeList) {
        return censor(node.messageChain)
    }
    return null
}

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