package io.github.gnuf0rce.mirai.censor.data

import net.mamoe.mirai.console.data.*

internal object ContentCensorHistory : AutoSavePluginData("ContentCensorHistory") {
    val records: MutableMap<Long, List<String>> by value()
}