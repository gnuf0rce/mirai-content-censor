package io.github.gnuf0rce.mirai.censor.data

import xyz.cssxsh.baidu.oauth.*

public interface AipClientConfig : BaiduAuthConfig {
    public val connectionTimeoutInMillis: Long
    public val socketTimeoutInMillis: Long
    public val requestTimeoutInMillis: Long
    public val proxyUrl: String
}