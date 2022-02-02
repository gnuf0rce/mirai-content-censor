package io.github.gnuf0rce.mirai.plugin

import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.permission.PermissionService.Companion.testPermission
import net.mamoe.mirai.event.events.*
import xyz.cssxsh.mirai.spi.*

internal class MiraiContentCensorService : ContentCensor {

    override suspend fun handle(event: GroupMessageEvent): Boolean = with(event) {
        !NoCensorPermission.testPermission(toCommandSender()) && manage(results = censor(message = message))
    }

    override suspend fun handle(event: NudgeEvent): Boolean = false

    override val level: Int = 10

    override val id: String = "baidu"
}