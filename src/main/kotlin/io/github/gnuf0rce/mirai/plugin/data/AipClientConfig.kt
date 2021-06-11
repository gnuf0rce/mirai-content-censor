package io.github.gnuf0rce.mirai.plugin.data


interface AipClientConfig {
    val appId: String
    val apiKey: String
    val secretKey: String
    val connectionTimeoutInMillis: Int
    val socketTimeoutInMillis: Int
    val proxy: String
}