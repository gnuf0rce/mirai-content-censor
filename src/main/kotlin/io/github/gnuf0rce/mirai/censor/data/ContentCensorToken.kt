package io.github.gnuf0rce.mirai.censor.data

import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.data.SerializableValue.Companion.serializableValueWith
import net.mamoe.mirai.console.internal.data.*
import xyz.cssxsh.baidu.*
import java.time.*

internal object ContentCensorToken : AutoSavePluginData("ContentCensorToken"), BaiduAuthToken {
    @ValueName("expires")
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    override var expires: OffsetDateTime by LazyReferenceValueImpl<OffsetDateTime>()
        .serializableValueWith(OffsetDateTimeSerializer)
        .apply { value = OffsetDateTime.MIN }

    @ValueName("access_token")
    override var accessToken: String by value("")

    @ValueName("refresh_token")
    override var refreshToken: String by value("")
}