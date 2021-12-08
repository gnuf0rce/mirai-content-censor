package io.github.gnuf0rce.mirai.plugin.data

import net.mamoe.mirai.console.data.*

object ContentCensorHistory : AutoSavePluginData("ContentCensorHistory") {
    val records: MutableMap<Long, List<String>> by value()
}