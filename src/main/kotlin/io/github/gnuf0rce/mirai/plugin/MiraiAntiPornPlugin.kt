package io.github.gnuf0rce.mirai.plugin

import io.github.gnuf0rce.mirai.plugin.data.*
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.event.*

object MiraiAntiPornPlugin : KotlinPlugin(
    JvmPluginDescription("io.github.gnuf0rce.anti-porn", "1.0.1") {
        name("anti-porn")
        author("cssxsh")
    }
) {

    override fun onEnable() {
        ContentCensorConfig.reload()
        ContentCensorToken.reload()
        AntiPornListener.registerTo(globalEventChannel())
    }

    override fun onDisable() {
        AntiPornListener.cancelAll()
    }
}