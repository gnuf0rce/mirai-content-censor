package io.github.gnuf0rce.mirai.plugin

import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.*
import xyz.cssxsh.baidu.aip.censor.*
import kotlin.coroutines.*
import kotlin.coroutines.cancellation.*

object AntiPornListener : SimpleListenerHost() {

    @EventHandler
    suspend fun GroupMessageEvent.handle() {
        if (group.botAsMember.permission > sender.permission) {
            censor(message = message)
        }
    }

    private suspend fun GroupMessageEvent.censor(message: MessageChain) {
        // Text Censor
        if (message.content.isNotBlank() && config.plain) {
            manage(censor.text(plain = message.content))
        }
        // Image Censor
        if (config.image) {
            for (image in message.filterIsInstance<Image>()) {
                manage(censor.image(url = image.queryUrl(), gif = image.imageType == ImageType.GIF))
            }
        }
        // Audio Censor
        if (config.audio) {
            for (audio in message.filterIsInstance<OnlineAudio>()) {
                val url = audio.urlForDownload
                val format = audio.codec.formatName
                manage(censor.voice(url = url, format = format, rawText = true, split = false))
            }
        }
        // Forward
        for (node in (message.firstIsInstanceOrNull<ForwardMessage>() ?: return).nodeList) {
            censor(node.messageChain)
        }
    }

    @OptIn(ConsoleExperimentalApi::class)
    private suspend fun GroupMessageEvent.manage(result: CensorResult) {
        when (result.conclusionType) {
            ConclusionType.COMPLIANCE -> {
                // 1.合规
            }
            ConclusionType.NON_COMPLIANCE -> {
                // 2.不合规
                logger.info { "${sender.render()} 消息不合规, ${result.render()}" }
                sender.mute(maxOf(config.mute * result.count(), config.mute))
                group.sendMessage(At(sender) + result.message())
                message.recall()
            }
            ConclusionType.SUSPECTED -> {
                // 3.疑似
                logger.info { "${sender.render()} 消息疑似, ${result.render()}" }
                message.recall()
                group.sendMessage(At(sender) + result.message())
            }
            ConclusionType.NONE, ConclusionType.FAILURE -> {
                // 0.请求失败
                // 4.审核失败
                logger.warning { result.toString() }
            }
        }
    }

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        when (exception) {
            is CancellationException -> {
                // ...
            }
            is ExceptionInEventHandlerException -> {
                logger.warning({ "AntiPornListener handle 出错" }, exception.cause)
            }
            else -> {
                logger.warning({ "AntiPornListener 出错" }, exception)
            }
        }
    }
}