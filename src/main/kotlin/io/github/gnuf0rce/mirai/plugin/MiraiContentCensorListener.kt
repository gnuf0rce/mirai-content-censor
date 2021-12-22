package io.github.gnuf0rce.mirai.plugin

import io.github.gnuf0rce.mirai.plugin.data.*
import net.mamoe.mirai.console.permission.PermissionService.Companion.testPermission
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.recallIn
import net.mamoe.mirai.utils.*
import xyz.cssxsh.baidu.aip.censor.*
import kotlin.coroutines.*
import kotlin.coroutines.cancellation.*

object MiraiContentCensorListener : SimpleListenerHost() {

    @EventHandler
    suspend fun GroupMessageEvent.handle() {
        if (NoCensorPermission.testPermission(sender.permitteeId).not()
            && group.botAsMember.permission > sender.permission
        ) {
            manage(results = censor(message = message))
        }
    }

    @OptIn(ConsoleExperimentalApi::class)
    private suspend fun GroupMessageEvent.manage(results: List<CensorResult>) {
        var count = 0
        for (result in results) {
            when (result.conclusionType) {
                ConclusionType.COMPLIANCE -> {
                    // 1.合规
                }
                ConclusionType.NON_COMPLIANCE -> {
                    // 2.不合规
                    logger.info { "${sender.render()} 消息不合规, ${result.render()}" }
                    count += result.count().coerceAtLeast(1)
                }
                ConclusionType.SUSPECTED -> {
                    // 3.疑似
                    logger.info { "${sender.render()} 消息疑似, ${result.render()}" }
                }
                ConclusionType.NONE, ConclusionType.FAILURE -> {
                    // 0.请求失败
                    // 4.审核失败
                    logger.warning { "${sender.render()} 消息处理失败, $result" }
                }
            }
        }

        if (results.none { it.conclusionType == ConclusionType.NON_COMPLIANCE || it.conclusionType == ConclusionType.SUSPECTED }) {
            // 没有发现违规时返回
            return
        }

        // 撤回原消息
        message.recallIn(config.recall * 1000L)
        // 发送提示
        group.sendMessage(At(sender) + results.joinToString { it.message() })
        // 记录结果
        ContentCensorHistory.records.compute(sender.id) { _, list -> list.orEmpty() + message.serializeToMiraiCode() }
        // 禁言
        if (count > 0) sender.mute(config.mute * count)
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