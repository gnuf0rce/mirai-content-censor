package io.github.gnuf0rce.mirai.plugin

import com.baidu.aip.contentcensor.*
import io.github.gnuf0rce.mirai.plugin.data.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.json.*
import java.net.*

internal val censor by lazy {
    MiraiAntiPornPlugin.runCatching {
        AipContentCensor(config = ContentCensorConfig.apply { reload() })
    }.onFailure {
        MiraiAntiPornPlugin.logger.warning(it)
    }.getOrThrow()
}

internal val logger get() = MiraiAntiPornPlugin.logger

internal val config: HandleConfig get() = ContentCensorConfig

internal fun AipContentCensor(config: AipClientConfig): AipContentCensor = with(config) {
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

@OptIn(ExperimentalSerializationApi::class)
internal fun JSONObject.parser(): ContentCensorResult {
    check("error_code" !in keys().asSequence()) { "审核API错误, ${toString()}" }
    return JsonParser.decodeFromString(toString())
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