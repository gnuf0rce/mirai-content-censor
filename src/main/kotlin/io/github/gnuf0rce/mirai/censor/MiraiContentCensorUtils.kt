package io.github.gnuf0rce.mirai.censor

import io.github.gnuf0rce.mirai.censor.data.*
import kotlinx.coroutines.*
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageSource.Key.recallIn
import net.mamoe.mirai.utils.*
import xyz.cssxsh.baidu.aip.censor.*
import java.util.*
import kotlin.collections.*
import kotlin.io.path.*

internal val NoCensorPermission: Permission by lazy {
    with(PermissionService.INSTANCE) {
        try {
            register(
                id = MiraiContentCensorPlugin.permissionId("no-censor"),
                description = "跳过检测",
                parent = MiraiContentCensorPlugin.parentPermission
            )
        } catch (_: UninitializedPropertyAccessException) {
            rootPermission
        }
    }
}

internal val logger by lazy {
    try {
        MiraiContentCensorPlugin.logger
    } catch (_: UninitializedPropertyAccessException) {
        MiraiLogger.Factory.create(MiraiContentCensor::class)
    }
}

internal val config get() = ContentCensorConfig

internal val plains: MutableMap<String, CensorResult> = WeakHashMap(1024)

internal val images: MutableMap<String, CensorResult> = WeakHashMap(1024)

internal val wait: MutableMap<Long, StringBuilder> = WeakHashMap(1024)

public suspend fun censor(message: MessageChain, config: HandleConfig = ContentCensorConfig): List<CensorResult> {
    val results = ArrayList<CensorResult>()
    // Text Censor
    if (config.plain && message.anyIsInstance<PlainText>()) {
        val content = message.joinToString(" ") { element ->
            when (element) {
                is PlainText -> element.content
                is Face -> element.contentToString()
                is MarketFace -> element.name
                else -> ""
            }
        }
        val source = message.sourceOrNull
        val result = if (content.length <= 3 && source != null) {
            // XXX: record id
            val id = source.targetId xor source.fromId
            val builder = wait.getOrPut(id, ::StringBuilder)
            builder.appendLine(content)
            if (builder.length >= 12) {
                val current = builder.toString()
                builder.clear()
                plains.getOrPut(current) {
                    MiraiContentCensor.text(plain = current)
                }
            }
            null
        } else {
            plains.getOrPut(content) {
                MiraiContentCensor.text(plain = content)
            }
        }

        if (result != null) results.add(result)
    }
    // Image Censor
    if (config.image) {
        for (image in message.filterIsInstance<Image>()) {
            val result = images.getOrPut(image.imageId) {
                if (config.download) {
                    val file = MiraiContentCensor.download(message = image)
                    MiraiContentCensor.image(bytes = file.readBytes(), gif = image.imageType == ImageType.GIF)
                } else {
                    val url = image.queryUrl()
                    MiraiContentCensor.image(url = url, gif = image.imageType == ImageType.GIF)
                }
            }

            results.add(result)
        }
    }
    // Audio Censor
    if (config.audio) {
        val extension = VoiceExtension(rawText = true, split = false)
        for (audio in message.filterIsInstance<OnlineAudio>()) {
            val result = when (audio.codec) {
                AudioCodec.AMR -> {
                    if (config.download) {
                        val file = MiraiContentCensor.download(message = audio)
                        MiraiContentCensor.voice(bytes = file.readBytes(), format = "amr", extension = extension)
                    } else {
                        MiraiContentCensor.voice(url = audio.urlForDownload, format = "amr", extension = extension)
                    }
                }
                AudioCodec.SILK -> {
                    val source = MiraiContentCensor.pcm(audio = audio)
                    MiraiContentCensor.voice(bytes = source, format = "pcm", extension = extension)
                }
            }
            results.add(result)
        }
    }
    // Forward
    for (node in message.firstIsInstanceOrNull<ForwardMessage>()?.nodeList.orEmpty()) {
        results.addAll(censor(message = node.messageChain, config = config))
    }

    supervisorScope {
        launch {
            // 记录结果
            MiraiContentCensorRecorder.record(message = message, results = results)
        }
    }

    return results
}

private fun CensorItem.render(): String {
    return when (this) {
        is CensorItem.Star -> "(${datasetName}, ${probability}, ${name})"
        is CensorItem.Hit -> "(${datasetName}, ${probability}, ${words})"
        is CensorResult.Image.Record -> "(${datasetName}, ${probability}, ${(stars + hits).joinToString { it.render() }})"
    }
}

public fun CensorResult.render(): String {
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

public fun CensorResult.count(): Int {
    return when (this) {
        is CensorResult.Image -> data.size
        is CensorResult.Text -> data.size
        is CensorResult.Video -> frames.size
        is CensorResult.Voice -> data.size
    }
}

public fun CensorResult.message(): String {
    return when (this) {
        is CensorResult.Image -> data.joinToString { it.message }
        is CensorResult.Text -> data.joinToString { it.message }
        is CensorResult.Video -> frames.joinToString { record -> record.data.joinToString { it.message } }
        is CensorResult.Voice -> data.joinToString { record -> record.auditData.joinToString { it.message } }
    }
}

public suspend fun GroupMessageEvent.manage(results: List<CensorResult>): Boolean {
    var count = 0
    for (result in results) {
        when (result.conclusionType) {
            // 1.合规
            ConclusionType.COMPLIANCE -> Unit
            // 2.不合规
            ConclusionType.NON_COMPLIANCE -> {
                logger.info { "${sender.render()} 消息不合规, ${result.render()}" }
                count += result.count().coerceAtLeast(1)
            }
            // 3.疑似
            ConclusionType.SUSPECTED -> {
                logger.info { "${sender.render()} 消息疑似, ${result.render()}" }
            }
            // 0.请求失败 4.审核失败
            ConclusionType.NONE, ConclusionType.FAILURE -> {
                logger.warning { "${sender.render()} 消息处理失败, $result" }
            }
        }
    }

    if (results.none { it.conclusionType == ConclusionType.NON_COMPLIANCE || it.conclusionType == ConclusionType.SUSPECTED }) {
        // 没有发现违规时返回
        return false
    }

    // 撤回原消息
    message.recallIn(config.recall * 1000L)
    // 发送提示
    group.sendMessage(At(sender) + results.joinToString { it.message() })
    // 禁言
    if (count > 0) sender.mute(config.mute * count)

    return true
}