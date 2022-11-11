package io.github.gnuf0rce.mirai.censor.data

public interface HandleConfig {
    public val mute: Int
    public val recall: Int
    public val plain: Boolean
    public val image: Boolean
    public val audio: Boolean
    public val download: Boolean
}