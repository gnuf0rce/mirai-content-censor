package io.github.gnuf0rce.mirai.censor.entry

import jakarta.persistence.*

@Entity
@Table(name = "content_censor_record")
@kotlinx.serialization.Serializable
public data class ContentCensorRecord(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "from_id", nullable = false, updatable = false)
    val fromId: Long = 0,
    @Column(name = "target_id", nullable = false, updatable = false)
    val targetId: Long = 0,
    @Column(name = "time", nullable = false, updatable = false)
    val time: Int = 0,
    @Column(name = "ids", nullable = true, updatable = false)
    val ids: String? = null,
    @Column(name = "results", nullable = false, updatable = false, columnDefinition = "text")
    val results: String = "[]",
) : java.io.Serializable