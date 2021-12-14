package io.github.gnuf0rce.mirai.plugin.command

import io.github.gnuf0rce.mirai.plugin.*
import io.github.gnuf0rce.mirai.plugin.data.ContentCensorHistory
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
        sendMessage(list?.joinToString("\n") { code ->
            code.toByteArray().toByteString().base64()
        } ?: "没有记录")
    }
}