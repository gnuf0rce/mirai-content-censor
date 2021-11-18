package io.github.gnuf0rce.mirai.plugin

import com.baidu.aip.contentcensor.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.*

object AntiPornListener : SimpleListenerHost() {

    @EventHandler
    suspend fun GroupMessageEvent.handle() = censor(message = message)

    private suspend fun GroupMessageEvent.censor(message: MessageChain) {
        // Text Censor
        if (message.content.isNotBlank()) {
            manage(censor.textCensorUserDefined(message.content).parser())
        }
        // Image Censor
        if (config.image) {
            for (image in message.filterIsInstance<Image>()) {
                manage(censor.imageCensorUserDefined(image.queryUrl(), EImgType.URL, null).parser())
            }
        }
        // Forward
        val forward = message.firstIsInstanceOrNull<ForwardMessage>()
        if (forward != null) {
            for (node in forward.nodeList) {
                censor(node.messageChain)
            }
        }
    }

    private suspend fun GroupMessageEvent.manage(result: ContentCensorResult) {
        when (result.conclusionType) {
            1 -> {
                // 1.合规
            }
            2 -> {
                // 2.不合规
                logger.info {
                    "${sender.render()} 消息不合规, ${result.data.flatMap(ContentCensorData::hits)}"
                }
                message.recall()
                sender.mute(config.mute)
                group.sendMessage(At(sender) + result.data.joinToString { it.msg })
            }
            3 -> {
                // 3.疑似
                logger.info {
                    "${sender.render()} 消息疑似, ${result.data.flatMap(ContentCensorData::hits)}"
                }
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