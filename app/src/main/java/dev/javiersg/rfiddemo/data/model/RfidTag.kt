package dev.javiersg.rfiddemo.data.model

data class RfidTag(
    val epc: String,
    val rssi: Int,
    val antenna: Int = 1,
    val peakCount: Int = 1,
    val lastSeen: Long = System.currentTimeMillis()
)