package dev.javiersg.rfiddemo.domain.model

data class RfidTag(
    val epc: String, // Electronic Product Code (Hexadecimal único)
    val rssi: Int, // Received Signal Strength Indicator (dBm)
    val timestamp: Long = System.currentTimeMillis(),
)
