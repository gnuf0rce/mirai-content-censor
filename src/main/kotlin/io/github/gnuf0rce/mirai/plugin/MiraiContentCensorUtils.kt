package io.github.gnuf0rce.mirai.plugin

import io.github.gnuf0rce.mirai.plugin.data.*
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import xyz.cssxsh.baidu.aip.censor.*

internal val NoCensorPermission: Permission by lazy {
    PermissionService.INSTANCE.register(
        id = MiraiContentCensorPlugin.permissionId("no-censor"),
        description = "跳过检测",
        parent = MiraiContentCensorPlugin.parentPermission
    )
}

internal val logger get() = MiraiContentCensorPlugin.logger

internal val config get() = ContentCensorConfig

internal suspend fun censor(message: MessageChain): List<CensorResult> {
    val results = ArrayList<CensorResult>()
    // Text Censor
    if (message.content.isNotBlank() && config.plain) {
        results.add(MiraiContentCensor.text(plain = message.content))
    }
    // Image Censor
    if (config.image) {
        for (image in message.filterIsInstance<Image>()) {
            results.add(MiraiContentCensor.image(url = image.queryUrl(), gif = image.imageType == ImageType.GIF))
        }
    }
    // Audio Censor
    if (config.audio) {
        for (audio in message.filterIsInstance<OnlineAudio>()) {
            val url = audio.urlForDownload
            val format = audio.codec.formatName
            results.add(MiraiContentCensor.voice(url = url, format = format, rawText = true, split = false))
        }
    }
    // Forward
    for (node in message.firstIsInstanceOrNull<ForwardMessage>()?.nodeList.orEmpty()) {
        results.addAll(censor(message = node.messageChain))
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