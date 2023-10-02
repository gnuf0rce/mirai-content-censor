package io.github.gnuf0rce.mirai.censor

import io.github.kasukusakura.silkcodec.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import xyz.cssxsh.baidu.aip.*
import java.io.ByteArrayOutputStream
import java.nio.file.*
import kotlin.io.path.*

public object MiraiContentCensor : AipContentCensor(client = MiraiBaiduAipClient) {

    internal const val IMAGE_CACHE_PATH = "io.github.gnuf0rce.mirai.censor.image"

    internal const val AUDIO_CACHE_PATH = "io.github.gnuf0rce.mirai.censor.audio"

    public suspend fun download(message: MessageContent): Path {
        return when (message) {
            is Image -> {
                val source = Path(System.getProperty(IMAGE_CACHE_PATH, "."), message.imageId)
                if (source.exists().not()) {
                    client.useHttpClient { http ->
                        val url = message.queryUrl()
                        val channel = http.get(url).bodyAsChannel()
                        val temp = runInterruptible(Dispatchers.IO) {
                            Files.createTempFile(message.imageId, "image")
                        }
                        channel.joinTo(temp.toFile().writeChannel(), true)
                        temp.moveTo(source)
                    }
                }
                source
            }
            is OnlineAudio -> {
                val source = Path(System.getProperty(AUDIO_CACHE_PATH, "."), message.filename)
                if (source.exists().not()) {
                    client.useHttpClient { http ->
                        val channel = http.get(message.urlForDownload).bodyAsChannel()
                        val temp = runInterruptible(Dispatchers.IO) {
                            Files.createTempFile(message.filename, "audio")
                        }
                        channel.joinTo(temp.toFile().writeChannel(), true)
                        temp.moveTo(source)
                    }
                }
                source
            }
            else -> throw IllegalArgumentException("type: ${message::class.qualifiedName}")
        }
    }

    public suspend fun pcm(audio: OnlineAudio): ByteArray {
        val source = download(audio)

        val output = ByteArrayOutputStream(audio.length.toInt())
        source.inputStream().use { input ->
            SilkCoder.decode(input, output)
        }
        return output.toByteArray()
    }
}