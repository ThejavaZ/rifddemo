package dev.javiersg.rfiddemo.data.local.mapper

import dev.javiersg.rfiddemo.data.local.entity.TagEntity
import dev.javiersg.rfiddemo.domain.model.RfidTag

const val DEFAULT_ANTENNA = 1

fun TagEntity.toDomain(): RfidTag =
    RfidTag(
        epc = epc,
        rssi = rssi,
        timestamp = lastSeenTimestamp,
    )

fun RfidTag.toEntity(): TagEntity =
    TagEntity(
        epc = epc,
        rssi = rssi,
        antenna = DEFAULT_ANTENNA,
        readCount = 1,
        lastSeenTimestamp = timestamp,
    )
