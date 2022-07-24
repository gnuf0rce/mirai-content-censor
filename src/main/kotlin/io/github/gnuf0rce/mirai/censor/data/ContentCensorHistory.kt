package io.github.gnuf0rce.mirai.censor.data

import io.github.gnuf0rce.mirai.censor.entry.*
import net.mamoe.mirai.console.data.*

internal object ContentCensorHistory : AutoSavePluginData("ContentCensorHistory") {
    @ValueName("content_censor_records")
    val records: MutableList<ContentCensorRecord> by value()
}