package io.github.gnuf0rce.mirai.censor.command

import io.github.gnuf0rce.mirai.censor.*
import io.github.gnuf0rce.mirai.censor.entry.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.message.data.*
import xyz.cssxsh.baidu.aip.censor.*
import java.time.*

internal object MiraiCensorRecordCommand : CompositeCommand(
    owner = MiraiContentCensorPlugin,
    "censor-record",
    description = "查看历史记录"
) {

    private fun List<ContentCensorRecord>.render(): Message {
        if (isEmpty()) {
            return "没有记录".toPlainText()
        }
        return buildMessageChain {
            for (record in this@render) {
                appendLine("=== from ${record.fromId} to ${record.targetId} at ${record.time} ===")
                val results = Json.decodeFromString<List<CensorResult>>(record.results)
                for (result in results) {
                    appendLine(result.render())
                }
            }
        }
    }

    @SubCommand
    suspend fun CommandSender.from(sender: Long, date: LocalDate) {
        val start = date
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.of("+8"))
            .toInt()
        val end = date
            .plusDays(1)
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.of("+8"))
            .toInt()
        val list = MiraiContentCensorRecorder.from(sender, start, end)
        sendMessage(list.render())
    }
    @SubCommand
    suspend fun CommandSender.target(subject: Long, date: LocalDate) {
        val start = date
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.of("+8"))
            .toInt()
        val end = date
            .plusDays(1)
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.of("+8"))
            .toInt()
        val list = MiraiContentCensorRecorder.target(subject, start, end)
        sendMessage(list.render())
    }
}