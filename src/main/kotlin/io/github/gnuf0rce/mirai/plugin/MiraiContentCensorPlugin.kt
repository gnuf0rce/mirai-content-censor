package io.github.gnuf0rce.mirai.plugin

import io.github.gnuf0rce.mirai.plugin.command.*
import io.github.gnuf0rce.mirai.plugin.data.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.utils.*

object MiraiContentCensorPlugin : KotlinPlugin(
    JvmPluginDescription("io.github.gnuf0rce.content-censor", "1.2.1") {
        name("content-censor")
        author("cssxsh")
    }
) {

    override fun onEnable() {
        ContentCensorConfig.reload()
        ContentCensorToken.reload()
        ContentCensorHistory.reload()
        MiraiContentCensorListener.registerTo(globalEventChannel())
        MiraiContentCensorCommand.register()
        MiraiCensorRecordCommand.register()

        logger.info { "关闭审查请赋予权限 ${NoCensorPermission.id} 于用户" }
    }

    override fun onDisable() {
        MiraiContentCensorListener.cancelAll()
        MiraiContentCensorCommand.unregister()
        MiraiCensorRecordCommand.unregister()
    }
}