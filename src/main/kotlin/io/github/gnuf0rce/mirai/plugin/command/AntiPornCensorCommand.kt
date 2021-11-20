package io.github.gnuf0rce.mirai.plugin.command

import io.github.gnuf0rce.mirai.plugin.*
import net.mamoe.mirai.console.command.*

object AntiPornCensorCommand: SimpleCommand(
    owner = MiraiAntiPornPlugin,
"censor",
    description = "测试内容是否合法"
) {

    @Handler
    suspend fun CommandSenderOnMessage<*>.handle() {
        val result = censor(fromEvent.message)
        sendMessage("${result?.conclusion}: ${result?.render() ?: "没有可以解析的内容"}")
    }
}