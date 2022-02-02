package io.github.gnuf0rce.mirai.plugin.data

import net.mamoe.mirai.console.data.*

object ContentCensorConfig : ReadOnlyPluginConfig("ContentCensor"), AipClientConfig, HandleConfig {
    @ValueName("app_name")
    @ValueDescription("百度AI客户端 APP_NAME")
    override val appName: String by value("")

    @ValueName("app_id")
    @ValueDescription("百度AI客户端 APP_ID")
    override val appId: Long by value(0L)

    @ValueName("api_key")
    @ValueDescription("百度AI客户端 API_KEY")
    override val appKey: String by value("")

    @ValueName("secret_key")
    @ValueDescription("百度AI客户端 SECRET_KEY")
    override val secretKey: String by value("")

    @ValueName("connection_timeout_in_millis")
    @ValueDescription("百度AI客户端 连接超时 毫秒")
    override val connectionTimeoutInMillis: Long by value(30_000L)

    @ValueName("socket_timeout_in_millis")
    @ValueDescription("百度AI客户端 端口超时 毫秒")
    override val socketTimeoutInMillis: Long by value(30_000L)

    @ValueName("request_timeout_in_millis")
    @ValueDescription("百度AI客户端 端口超时 毫秒")
    override val requestTimeoutInMillis: Long by value(180_000L)

    @ValueName("proxy")
    @ValueDescription("百度AI客户端 代理, 格式 http://127.0.0.1:8080 或 socket://127.0.0.1:1080")
    override val proxyUrl: String by value("")

    @ValueName("mute")
    @ValueDescription("禁言时间，单位秒")
    override val mute: Int by value(60)

    @ValueName("recall")
    @ValueDescription("撤回消息的延时")
    override val recall: Int by value(0)

    @ValueName("plain")
    @ValueDescription("是否检查文本")
    override val plain: Boolean by value(true)

    @ValueName("image")
    @ValueDescription("是否检查图片")
    override val image: Boolean by value(true)

    @ValueName("audio")
    @ValueDescription("是否检查语音")
    override val audio: Boolean by value(false)
}