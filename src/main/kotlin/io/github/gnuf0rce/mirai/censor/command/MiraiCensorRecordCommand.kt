package io.github.gnuf0rce.mirai.censor.command

import io.github.gnuf0rce.mirai.censor.*
import io.github.gnuf0rce.mirai.censor.data.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.contact.*
import okio.ByteString.Companion.toByteString

object MiraiCensorRecordCommand : SimpleCommand(
    owner = MiraiContentCensorPlugin,
    "censor-record",
    description = "查看历史记录"
) {

    @Handler
    suspend fun CommandSender.handle(sender: Contact) {
        val list = ContentCensorHistory.records[sender.id]
        if (list.isNullOrEmpty()) {
            sendMessage("没有记录")
            return
        }
        if (this is ConsoleCommandSender) {
            sendMessage(
                list.joinToString("\n")
            )
        } else {
            sendMessage(
                list.joinToString("\n") { code -> code.toByteArray().toByteString().base64() }
            )
        }
    }
}