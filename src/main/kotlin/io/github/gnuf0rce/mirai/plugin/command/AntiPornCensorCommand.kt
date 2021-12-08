package io.github.gnuf0rce.mirai.plugin.command

import io.github.gnuf0rce.mirai.plugin.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.message.data.*

object AntiPornCensorCommand : SimpleCommand(
    owner = MiraiAntiPornPlugin,
    "censor",
    description = "测试内容是否合法"
) {

    @Handler
    suspend fun CommandSenderOnMessage<*>.handle() {
        val message = with(fromEvent) {
            message.findIsInstance<QuoteReply>()?.source?.originalMessage ?: message
        }
        val reply = with(censor(message)) {
            if (this == null) {
                "没有可以检测的内容"
            } else {
                "${conclusion}: ${render()}"
            }
        }
        sendMessage(reply)
    }
}