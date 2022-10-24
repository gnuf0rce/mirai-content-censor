package io.github.gnuf0rce.mirai.censor

import io.github.kasukusakura.silkcodec.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import net.mamoe.mirai.message.data.*
import xyz.cssxsh.baidu.aip.*
import java.io.ByteArrayOutputStream
import java.nio.file.*
import kotlin.io.path.*

public object MiraiContentCensor : AipContentCensor(client = MiraiBaiduAipClient) {

    internal const val AUDIO_CACHE_PATH = "io.github.gnuf0rce.mirai.censor.audio"

    public suspend fun pcm(audio: OnlineAudio): ByteArray {
        val source = Path(System.getProperty(AUDIO_CACHE_PATH, "."), audio.filename)
        if (source.exists().not()) {
            client.useHttpClient { http ->
                val channel = http.get(audio.urlForDownload).bodyAsChannel()
                val temp = runInterruptible(Dispatchers.IO) {
                    Files.createTempFile(audio.filename, "audio")
                }

                temp.outputStream().use { output ->
                    channel.copyTo(output)
                }
                temp.moveTo(source)
            }
        }

        val output = ByteArrayOutputStream(audio.length.toInt())
        source.inputStream().use { input ->
            SilkCoder.decode(input, output)
        }
        return output.toByteArray()
    }
}