package io.github.gnuf0rce.mirai.censor.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.util.*
import java.nio.file.*

public object ContentCensorConfig : ReadOnlyPluginConfig("ContentCensor"), AipClientConfig, HandleConfig {
    @ValueName("app_name")
    @ValueDescription("百度AI客户端 APP_NAME")
    override val appName: String by value(System.getProperty("io.github.gnuf0rce.mirai.censor.name", ""))

    @ValueName("app_id")
    @ValueDescription("百度AI客户端 APP_ID")
    override val appId: Long by value(System.getProperty("io.github.gnuf0rce.mirai.censor.id", "0").toLong())

    @ValueName("api_key")
    @ValueDescription("百度AI客户端 API_KEY")
    override val appKey: String by value(System.getProperty("io.github.gnuf0rce.mirai.censor.key", ""))

    @ValueName("secret_key")
    @ValueDescription("百度AI客户端 SECRET_KEY")
    override val secretKey: String by value(System.getProperty("io.github.gnuf0rce.mirai.censor.secret", ""))

    @ValueName("connection_timeout_in_millis")
    @ValueDescription("百度AI客户端 连接超时 毫秒")
    override val connectionTimeoutInMillis: Long by value(30_000L)

    @ValueName("socket_timeout_in_millis")
    @ValueDescription("百度AI客户端 端口超时 毫秒")
    override val socketTimeoutInMillis: Long by value(30_000L)

    @ValueName("request_timeout_in_millis")
    @ValueDescription("百度AI客户端 端口超时 毫秒")
    override val requestTimeoutInMillis: Long by value(180_000L)

    @ValueName("proxy")
    @ValueDescription("百度AI客户端 代理, 格式 http://127.0.0.1:8080 或 socket://127.0.0.1:1080")
    override val proxyUrl: String by value("")

    @ValueName("mute")
    @ValueDescription("禁言时间，单位秒")
    override val mute: Int by value(60)

    @ValueName("recall")
    @ValueDescription("撤回消息的延时")
    override val recall: Int by value(0)

    @ValueName("plain")
    @ValueDescription("是否检查文本")
    override val plain: Boolean by value(true)

    @ValueName("image")
    @ValueDescription("是否检查图片")
    override val image: Boolean by value(true)

    @ValueName("audio")
    @ValueDescription("是否检查语音")
    override val audio: Boolean by value(false)

    @ValueName("download")
    @ValueDescription("下载文件再上传(语音/图片)")
    override val download: Boolean by value(false)

    @OptIn(ConsoleExperimentalApi::class)
    override fun onInit(owner: PluginDataHolder, storage: PluginDataStorage) {
        // 判断是在插件中加载
        val plugin = owner as? JvmPlugin ?: return
        // 判断是第一次加载
        if (plugin.isEnabled) return

        // 生产流
        val shared = MutableSharedFlow<Pair<Path, WatchEvent.Kind<*>>>()

        // 处理内容变更
        plugin.launch(CoroutineName(name = "Reload(config=${saveName})")) {
            // 取样 5000ms, 避免过度触发 load
            @OptIn(FlowPreview::class)
            shared.sample(periodMillis = 5_000).collect { (path, kind) ->
                plugin.logger.debug("[${kind.name()}] ${path.toUri()}")
                launch {
                    // 等待 3000ms, 避免过早触发 load
                    delay(timeMillis = 3_000)
                    with(plugin) {
                        logger.info("[${kind.name()}] auto load config $saveName")
                        loader.configStorage.load(holder = owner, instance = this@ContentCensorConfig)
                    }
                }
            }
        }

        // 监视内容变更
        plugin.launch(CoroutineName(name = "Watch(config=${saveName})")) {
            @Suppress("USELESS_IS_CHECK")
            val folder = when (this@ContentCensorConfig) {
                is ReadOnlyPluginConfig -> plugin.configFolderPath
                is ReadOnlyPluginData -> plugin.dataFolderPath
                else -> return@launch
            }
            // 创建监听器
            val watcher = try {
                runInterruptible(Dispatchers.IO) {
                    folder.fileSystem.newWatchService()
                }
            } catch (cause: java.io.IOException) {
                plugin.logger.warning("文件监视器创建失败", cause)
                return@launch
            }
            // 注册监听器
            val watch = runInterruptible(Dispatchers.IO) {
                folder.register(
                    watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
                )
            }
            plugin.logger.info("watch config $saveName")

            // 循环获取变更
            while (isActive) {
                val key = runInterruptible(Dispatchers.IO, watcher::take)
                for (event in key.pollEvents()) {
                    val path = event.context() as? Path ?: continue
                    if (path.toString() != "${saveName}.${saveType.extension}") continue
                    val file = folder.resolve(path)

                    shared.emit(value = file to event.kind())
                }
                key.reset()
            }
            watch.cancel()
        }
    }
}