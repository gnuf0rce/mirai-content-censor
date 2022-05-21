package io.github.gnuf0rce.mirai.censor.data

interface HandleConfig {
    val mute: Int
    val recall: Int
    val plain: Boolean
    val image: Boolean
    val audio: Boolean
}