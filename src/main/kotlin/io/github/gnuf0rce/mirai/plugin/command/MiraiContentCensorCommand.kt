package io.github.gnuf0rce.mirai.plugin.command

import io.github.gnuf0rce.mirai.plugin.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.message.data.*

object MiraiContentCensorCommand : SimpleCommand(
    owner = MiraiContentCensorPlugin,
    "censor",
    description = "测试内容是否合法"
) {

    @Handler
    suspend fun CommandSenderOnMessage<*>.handle() {
        val message = with(fromEvent) {
            message.findIsInstance<QuoteReply>()?.source?.originalMessage ?: message
        }
        val results = censor(message = message)
        val reply = if (results.isEmpty()) {
            "没有可以检测的内容"
        } else {
            results.joinToString("\n") { result ->
                "${result.conclusion}: ${result.render()}"
            }
        }
        sendMessage(reply)
    }
}