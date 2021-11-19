package io.github.gnuf0rce.mirai.plugin.data

import net.mamoe.mirai.console.data.*

object ContentCensorToken : AutoSavePluginData("ContentCensorToken") {
    @ValueName("access_token")
    internal var accessToken: String by value("")

    @ValueName("refresh_token")
    internal var refreshToken: String by value("")
}