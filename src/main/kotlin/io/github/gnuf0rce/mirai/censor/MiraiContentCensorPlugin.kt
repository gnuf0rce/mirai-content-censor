package io.github.gnuf0rce.mirai.censor

import io.github.gnuf0rce.mirai.censor.command.*
import io.github.gnuf0rce.mirai.censor.data.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.*
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.*

public object MiraiContentCensorPlugin : KotlinPlugin(
    JvmPluginDescription("io.github.gnuf0rce.content-censor", "1.3.5") {
        name("content-censor")
        author("cssxsh")

        dependsOn("xyz.cssxsh.mirai.mirai-administrator", ">= 1.1.0", true)
        dependsOn("xyz.cssxsh.mirai.plugin.mirai-hibernate-plugin", true)
    }
) {

    override fun onEnable() {
        // XXX: mirai console version check
        check(SemVersion.parseRangeRequirement(">= 2.12.0-RC").test(MiraiConsole.version)) {
            "$name $version 需要 Mirai-Console 版本 >= 2.12.0，目前版本是 ${MiraiConsole.version}"
        }

        try {
            MiraiContentCensorRecorder.enable()
            logger.info { "审核记录将记录到数据库 ${MiraiContentCensorRecorder.database()}" }
        } catch (_: NoClassDefFoundError) {
            logger.info { "审核记录将记录到 ContentCensorHistory.yml" }
            ContentCensorHistory.reload()
        }

        ContentCensorConfig.reload()
        ContentCensorToken.reload()
        MiraiContentCensorCommand.register()
        MiraiCensorRecordCommand.register()

        logger.info { "关闭审查请赋予权限 ${NoCensorPermission.id} 于用户" }

        try {
            MiraiAdministrator
            logger.info { "插件已桥接至 mirai-administrator" }
        } catch (_: NoClassDefFoundError) {
            logger.warning { "未安装 mirai-administrator" }
            MiraiContentCensorListener.registerTo(globalEventChannel())
        }
    }

    override fun onDisable() {
        MiraiContentCensorListener.cancelAll()
        MiraiContentCensorCommand.unregister()
        MiraiCensorRecordCommand.unregister()

        MiraiContentCensorRecorder.disable()
    }
}