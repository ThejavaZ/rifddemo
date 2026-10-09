package dev.javiersg.rfiddemo.data.api.mapper

import dev.javiersg.rfiddemo.data.api.dto.InventoryResponseDto
import dev.javiersg.rfiddemo.data.api.dto.TagDto
import dev.javiersg.rfiddemo.data.database.entity.InventoryEntity
import dev.javiersg.rfiddemo.data.database.entity.TagEntity
import dev.javiersg.rfiddemo.data.database.mapper.DEFAULT_ANTENNA
import dev.javiersg.rfiddemo.domain.model.InventoryItem
import dev.javiersg.rfiddemo.domain.model.RfidTag
import dev.javiersg.rfiddemo.domain.model.SyncStatus

fun TagEntity.toDto(): TagDto =
    TagDto(
        epc = epc,
        rssi = rssi,
        antenna = antenna,
        readCount = readCount,
        lastSeenTimestamp = lastSeenTimestamp,
    )

fun TagDto.toEntity(): TagEntity =
    TagEntity(
        epc = epc,
        rssi = rssi,
        antenna = antenna,
        readCount = readCount,
        lastSeenTimestamp = lastSeenTimestamp,
        syncStatus = SyncStatus.PENDING,
    )

fun TagDto.toDomain(): RfidTag =
    RfidTag(
        epc = epc,
        rssi = rssi,
        timestamp = lastSeenTimestamp,
    )

fun RfidTag.toDto(): TagDto =
    TagDto(
        epc = epc,
        rssi = rssi,
        antenna = DEFAULT_ANTENNA,
        readCount = 1,
        lastSeenTimestamp = timestamp,
    )

fun InventoryResponseDto.toDomain(): InventoryItem =
    InventoryItem(
        id = id,
        epc = epc,
        name = name,
        quantity = quantity,
    )

fun InventoryItem.toEntity(): InventoryEntity =
    InventoryEntity(
        id = id,
        epc = epc,
        name = name,
        quantity = quantity,
    )

fun InventoryEntity.toDomain(): InventoryItem =
    InventoryItem(
        id = id,
        epc = epc,
        name = name,
        quantity = quantity,
    )
