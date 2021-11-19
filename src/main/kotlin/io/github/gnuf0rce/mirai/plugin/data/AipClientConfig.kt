package io.github.gnuf0rce.mirai.plugin.data

import xyz.cssxsh.baidu.*

interface AipClientConfig: BaiduAuthConfig {
    val connectionTimeoutInMillis: Long
    val socketTimeoutInMillis: Long
    val proxy: String
}