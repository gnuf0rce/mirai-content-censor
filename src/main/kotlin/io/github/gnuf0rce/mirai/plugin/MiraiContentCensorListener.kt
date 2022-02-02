package io.github.gnuf0rce.mirai.plugin

import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.permission.PermissionService.Companion.testPermission
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.utils.*
import kotlin.coroutines.*
import kotlin.coroutines.cancellation.*

object MiraiContentCensorListener : SimpleListenerHost() {

    @EventHandler
    suspend fun GroupMessageEvent.handle() {
        if (NoCensorPermission.testPermission(toCommandSender()) || group.botAsMember.permission <= sender.permission) return

        manage(results = censor(message = message))
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