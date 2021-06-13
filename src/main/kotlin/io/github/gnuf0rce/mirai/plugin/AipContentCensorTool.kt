package io.github.gnuf0rce.mirai.plugin

import com.baidu.aip.contentcensor.AipContentCensor
import com.baidu.aip.contentcensor.EImgType
import io.github.gnuf0rce.mirai.plugin.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.MessageSource.Key.recallIn
import net.mamoe.mirai.utils.*
import org.json.JSONObject
import java.net.URI

private val client by MiraiAntiPornPlugin::client

private val logger by MiraiAntiPornPlugin::logger

internal fun AipContentCensor(config: AipClientConfig): AipContentCensor = config.run {
    check(listOf(appId, apiKey, secretKey).none(String::isBlank)) {
        "请按照文档步骤申请API_KEY，并填入ContentCensor配置文件 https://ai.baidu.com/ai-doc/ANTIPORN/Wkhu9d5iy"
    }
    AipContentCensor(appId, apiKey, secretKey).apply {
        setConnectionTimeoutInMillis(connectionTimeoutInMillis)
        setSocketTimeoutInMillis(socketTimeoutInMillis)
        val uri = URI(proxy)
        when (uri.scheme) {
            "http" -> setHttpProxy(uri.host, uri.port.takeIf { it > 0 } ?: 80)
            "socket" -> setSocketProxy(uri.host, uri.port.takeIf { it > 0 } ?: 1080)
        }
    }
}

private val JsonParser = Json {
    ignoreUnknownKeys = true
}

internal fun JSONObject.parser(): ContentCensorResult {
    check("error_code" !in keys().asSequence()) { "审核API错误, ${toString()}" }
    return JsonParser.decodeFromString(toString())
}

internal suspend fun GroupMessageEvent.handle(result: ContentCensorResult) {
    when(result.conclusionType) {
        1 -> {
            // 1.合规
        }
        2 -> {// 2.不合规
            message.recall()
            sender.mute(60 * result.data.size)
            group.sendMessage(At(sender) + result.data.joinToString { it.msg })
        }
        3 -> {// 3.疑似
            message.recallIn(10 * 1000)
            group.sendMessage(message.quote() + result.data.joinToString { it.msg })
        }
        else -> {// 4.审核失败
            logger.warning { result.conclusion }
        }
    }
}

internal suspend fun GroupMessageEvent.handle(message: MessageChain) {
    // Text Censor
    if (message.content.isNotBlank()) {
        handle(client.textCensorUserDefined(message.content).parser())
    }
    // Image Censor
    message.filterIsInstance<Image>().forEach {
        handle(client.imageCensorUserDefined(it.queryUrl(), EImgType.URL, null).parser())
    }
    // Forward
    message.filterIsInstance<ForwardMessage>().flatMap(ForwardMessage::nodeList).forEach { node ->
        handle(node.messageChain)
    }
}

@ConsoleExperimentalApi
object AntiPornSubscriber : CoroutineScope by MiraiAntiPornPlugin.childScope("AntiPorn") {

    fun start(): Unit = globalEventChannel().run {
        subscribeGroupMessages {
            content { sender.isOperator().not() && group.botAsMember.isOperator() }.invoke { handle(message) }
        }
    }

    fun stop() {
        coroutineContext.cancelChildren()
    }
}

@Serializable
data class ContentCensorResult(
    @SerialName("conclusion")
    val conclusion: String,
    @SerialName("conclusionType")
    val conclusionType: Int,
    @SerialName("data")
    val `data`: List<ContentCensorData> = emptyList(),
    @SerialName("log_id")
    val logId: Long,
    @SerialName("isHitMd5")
    val isHitMd5: Boolean = false
)

@Serializable
data class ContentCensorData(
    @SerialName("conclusion")
    val conclusion: String,
    @SerialName("conclusionType")
    val conclusionType: Int,
    @SerialName("hits")
    val hits: List<ContentCensorHit> = emptyList(),
    @SerialName("msg")
    val msg: String,
    @SerialName("subType")
    val subType: Int,
    @SerialName("type")
    val type: Int
)

@Serializable
data class ContentCensorHit(
    @SerialName("datasetName")
    val datasetName: String,
    @SerialName("probability")
    val probability: Double,
    @SerialName("words")
    val words: List<String>
)