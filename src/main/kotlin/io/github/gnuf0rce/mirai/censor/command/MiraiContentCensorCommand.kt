package io.github.gnuf0rce.mirai.censor.command

import io.github.gnuf0rce.mirai.censor.*
import net.mamoe.mirai.console.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.code.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.*

internal object MiraiContentCensorCommand : SimpleCommand(
    owner = MiraiContentCensorPlugin,
    "censor",
    description = "测试内容是否有违规"
) {

    private suspend fun CommandSender.request(hint: String, contact: Contact? = null): MessageChain = when (this) {
        is ConsoleCommandSender -> {
            val code = MiraiConsole.requestInput(hint)
            MiraiCode.deserializeMiraiCode(code, contact)
        }
        is CommandSenderOnMessage<*> -> {
            sendMessage(hint)
            fromEvent.nextMessage()
        }
        else -> throw IllegalStateException("未知环境 $this")
    }

    @Handler
    suspend fun CommandSender.handle() {
        val message = request(hint = "请输入要检查的消息")
        val results = censor(message = message)
        val reply = if (results.isEmpty()) {
            "没有违规"
        } else {
            results.joinToString("\n") { result ->
                "${result.conclusion}: ${result.render()}"
            }
        }
        sendMessage(reply)
    }
}