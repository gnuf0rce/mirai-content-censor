package io.github.gnuf0rce.mirai.plugin

import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.*
import xyz.cssxsh.baidu.aip.censor.*
import kotlin.coroutines.*
import kotlin.coroutines.cancellation.*

object AntiPornListener : SimpleListenerHost() {

    @EventHandler
    suspend fun GroupMessageEvent.handle() {
        if (group.botAsMember.permission > sender.permission) {
            manage(result = censor(message = message) ?: return)
        }
    }

    @OptIn(ConsoleExperimentalApi::class)
    private suspend fun GroupMessageEvent.manage(result: CensorResult) {
        when (result.conclusionType) {
            ConclusionType.COMPLIANCE -> {
                // 1.合规
            }
            ConclusionType.NON_COMPLIANCE -> {
                // 2.不合规
                logger.info { "${sender.render()} 消息不合规, ${result.render()}" }
                sender.mute(maxOf(config.mute * result.count(), config.mute))
                group.sendMessage(At(sender) + result.message())
                message.recall()
            }
            ConclusionType.SUSPECTED -> {
                // 3.疑似
                logger.info { "${sender.render()} 消息疑似, ${result.render()}" }
                message.recall()
                group.sendMessage(At(sender) + result.message())
            }
            ConclusionType.NONE, ConclusionType.FAILURE -> {
                // 0.请求失败
                // 4.审核失败
                logger.warning { result.toString() }
            }
        }
    }

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        when (exception) {
            is CancellationException -> {
                // ...
            }
            is ExceptionInEventHandlerException -> {
                logger.warning({ "AntiPornListener handle 出错" }, exception.cause)
            }
            else -> {
                logger.warning({ "AntiPornListener 出错" }, exception)
            }
        }
    }
}