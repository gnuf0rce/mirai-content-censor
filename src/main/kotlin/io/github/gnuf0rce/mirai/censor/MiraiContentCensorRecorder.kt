package io.github.gnuf0rce.mirai.censor

import io.github.gnuf0rce.mirai.censor.data.*
import io.github.gnuf0rce.mirai.censor.entry.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import net.mamoe.mirai.message.data.*
import org.hibernate.*
import xyz.cssxsh.baidu.aip.censor.*
import xyz.cssxsh.hibernate.*
import xyz.cssxsh.mirai.hibernate.*

public object MiraiContentCensorRecorder {

    private var factory: java.io.Closeable? = null

    public fun enable() {
        jakarta.persistence.Entity::class.java
        factory = MiraiHibernateConfiguration(plugin = MiraiContentCensorPlugin)
            .buildSessionFactory()
    }

    public fun disable() {
        factory?.close()
    }

    public fun database(): String? {
        val factory = factory ?: return null

        factory as SessionFactory

        return factory.fromSession { session -> session.getDatabaseMetaData() }.url
    }

    public fun record(message: MessageChain, results: List<CensorResult>) {

        val source = message.sourceOrNull
        val record = ContentCensorRecord(
            fromId = source?.fromId ?: 0,
            targetId = source?.targetId ?: 0,
            time = source?.time ?: (System.currentTimeMillis() / 1000).toInt(),
            ids = source?.ids?.joinToString(","),
            results = Json.encodeToString(results)
        )

        val factory = factory

        if (factory != null) {
            factory as SessionFactory

            factory.fromTransaction { session -> session.persist(record) }
        } else {
            ContentCensorHistory.records.add(record)
        }
    }

    public fun from(fromId: Long, start: Int, end: Int): List<ContentCensorRecord> {
        val factory = factory

        return if (factory != null) {
            factory as SessionFactory

            factory.fromSession { session ->
                session.withCriteria<ContentCensorRecord> { criteria ->
                    val root = criteria.from<ContentCensorRecord>()

                    criteria.select(root)
                        .where(equal(root.get<Long>("fromId"), fromId), between(root.get("time"), start, end))
                        .orderBy(desc(root.get<Int>("time")))
                }.list()
            }
        } else {
            ContentCensorHistory.records
                .asSequence()
                .filter { it.fromId == fromId && it.time in start..end }
                .sortedByDescending { it.time }
                .toList()
        }
    }

    public fun target(targetId: Long, start: Int, end: Int): List<ContentCensorRecord> {
        val factory = factory

        return if (factory != null) {
            factory as SessionFactory

            factory.fromSession { session ->
                session.withCriteria<ContentCensorRecord> { criteria ->
                    val root = criteria.from<ContentCensorRecord>()

                    criteria.select(root)
                        .where(equal(root.get<Long>("targetId"), targetId), between(root.get("time"), start, end))
                        .orderBy(desc(root.get<Int>("time")))
                }.list()
            }
        } else {
            ContentCensorHistory.records
                .asSequence()
                .filter { it.targetId == targetId && it.time in start..end }
                .sortedByDescending { it.time }
                .toList()
        }
    }
}