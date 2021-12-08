package io.github.gnuf0rce.mirai.plugin

import io.github.gnuf0rce.mirai.plugin.command.*
import io.github.gnuf0rce.mirai.plugin.data.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.utils.*

object MiraiAntiPornPlugin : KotlinPlugin(
    JvmPluginDescription("io.github.gnuf0rce.anti-porn", "1.1.0") {
        name("anti-porn")
        author("cssxsh")
    }
) {

    override fun onEnable() {
        ContentCensorConfig.reload()
        ContentCensorToken.reload()
        AntiPornListener.registerTo(globalEventChannel())
        AntiPornCensorCommand.register()

        logger.info { "关闭审查请赋予权限 ${NoCensorPermission.id} 于用户" }
    }

    override fun onDisable() {
        AntiPornListener.cancelAll()
    }
}