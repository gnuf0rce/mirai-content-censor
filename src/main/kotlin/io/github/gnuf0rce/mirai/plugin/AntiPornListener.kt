package io.github.gnuf0rce.mirai.plugin

import com.baidu.aip.contentcensor.*
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.*
import org.json.*

object AntiPornListener : SimpleListenerHost() {

    @EventHandler
    suspend fun GroupMessageEvent.handle() {
        if (group.botAsMember.permission > sender.permission) censor(message = message)
    }

    private suspend fun GroupMessageEvent.censor(message: MessageChain) {
        // Text Censor
        if (message.content.isNotBlank() && config.plain) {
            manage(censor.textCensorUserDefined(message.content))
        }
        // Image Censor
        if (config.image) {
            for (image in message.filterIsInstance<Image>()) {
                manage(censor.imageCensorUserDefined(image.queryUrl(), EImgType.URL, null))
            }
        }
        // Audio Censor
        if (config.audio) {
            for (audio in message.filterIsInstance<OnlineAudio>()) {
                manage(censor.voiceCensorUserDefined(audio.urlForDownload, EImgType.URL, audio.codec.formatName, null))
            }
        }
        // Forward
        for (node in (message.firstIsInstanceOrNull<ForwardMessage>() ?: return).nodeList) {
            censor(node.messageChain)
        }
    }

    @OptIn(ConsoleExperimentalApi::class)
    private suspend fun GroupMessageEvent.manage(json: JSONObject) {
        val result = try {
            ContentCensorResult.parser(json)
        } catch (cause: Throwable) {
            logger.warning({ "审核结果解析错误" }, cause)
            return
        }
        when (result.conclusionType) {
            1 -> {
                // 1.合规
            }
            2 -> {
                // 2.不合规
                logger.info { "${sender.render()} 消息不合规, ${result.render()}" }
                message.recall()
                sender.mute(maxOf(config.mute * result.data.size, config.mute))
                group.sendMessage(At(sender) + result.data.joinToString { it.msg })
            }
            3 -> {
                // 3.疑似
                logger.info { "${sender.render()} 消息疑似, ${result.render()}" }
                message.recall()
                group.sendMessage(At(sender) + result.data.joinToString { it.msg })
            }
            else -> {
                // 4.审核失败
                logger.warning { result.conclusion }
            }
        }
    }
}