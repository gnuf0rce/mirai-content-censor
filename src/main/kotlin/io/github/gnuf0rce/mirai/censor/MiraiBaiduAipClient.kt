package io.github.gnuf0rce.mirai.censor

import io.github.gnuf0rce.mirai.censor.data.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.baidu.aip.*
import xyz.cssxsh.baidu.oauth.*
import java.net.*
import kotlin.coroutines.*

public object MiraiBaiduAipClient : BaiduAipClient(config = config), CoroutineScope {

    override val coroutineContext: CoroutineContext by lazy {
        try {
            MiraiContentCensorPlugin.coroutineContext + CoroutineName("BaiduAipContentCensor")
        } catch (_: UninitializedPropertyAccessException) {
            CoroutineExceptionHandler { _, throwable ->
                if (throwable.unwrapCancellationException() !is CancellationException) {
                    logger.error("Exception in coroutine BaiduAipContentCensor", throwable)
                }
            } + CoroutineName("BaiduAipContentCensor")
        }
    }

    override val status: BaiduAuthStatus get() = ContentCensorToken

    override val apiIgnore: suspend (Throwable) -> Boolean = { throwable ->
        when (throwable) {
            is UnknownHostException,
            is NoRouteToHostException -> false
            is IOException -> {
                logger.warning({ "AipContentCensor Ignore" }, throwable)
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
                (this as OkHttpConfig).config {
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
}