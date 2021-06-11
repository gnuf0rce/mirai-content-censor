package io.github.gnuf0rce.mirai.plugin

import com.baidu.aip.contentcensor.AipContentCensor
import io.github.gnuf0rce.mirai.plugin.data.ContentCensorConfig
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

object MiraiAntiPornPlugin : KotlinPlugin(
    JvmPluginDescription("io.github.gnuf0rce.anti-porn", "1.0.0-dev-1") {
        name("anti-porn")
        author("cssxsh")
    }
) {
    val client: AipContentCensor by lazy {
        runCatching {
            ContentCensorConfig.reload()
            AipContentCensor(config = ContentCensorConfig)
        }.onFailure {
            logger.warning(it)
        }.getOrThrow()
    }

    @ConsoleExperimentalApi
    override fun onEnable() {
        ContentCensorConfig.reload()

        AntiPornSubscriber.start()
    }

    @ConsoleExperimentalApi
    override fun onDisable() {
        AntiPornSubscriber.stop()
    }
}