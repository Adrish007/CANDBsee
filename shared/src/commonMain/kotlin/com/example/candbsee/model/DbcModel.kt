package com.example.candbsee.model

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * Defines the type of DBC object an attribute applies to.
 */
enum class AttributeObjectType {
    NETWORK, NODE, MESSAGE, SIGNAL, ENVIRONMENT
}

/**
 * Defines the underlying data type of a DBC attribute.
 */
enum class AttributeValueType(val keyword: String) {
    INT("INT"),
    HEX("HEX"),
    FLOAT("FLOAT"),
    STRING("STRING"),
    ENUM("ENUM")
}

/**
 * Strongly typed definition of an attribute (BA_DEF_) and its default value (BA_DEF_DEF_).
 */
data class AttributeDefinition(
    val name: String,
    val objectType: AttributeObjectType,
    val valueType: AttributeValueType,
    val minimum: Double = 0.0,
    val maximum: Double = 0.0,
    val enumValues: List<String> = emptyList(),
    val defaultValue: String = ""
)

/**
 * Core Domain Model representing a CAN Database (DBC).
 */
data class DbcNetwork(
    val fileName: String = "Untitled.dbc",
    val version: String = "1.0",
    val bitTiming: String = "",
    val nodes: List<DbcNode> = emptyList(),
    val messages: List<DbcMessage> = emptyList(),
    val comments: Map<String, String> = emptyMap(),
    val valueTables: Map<String, List<ValueEntry>> = emptyMap(),
    val attributeDefinitions: Map<String, AttributeDefinition> = emptyMap(),
    val attributes: Map<String, String> = emptyMap() // Global attributes
) {
    val allSignals: List<Pair<DbcMessage, DbcSignal>>
        get() = messages.flatMap { msg -> msg.signals.map { sig -> msg to sig } }

    /**
     * Gets all messages transmitted by a specific node (ECU)
     */
    fun getTransmittedMessages(nodeName: String): List<DbcMessage> {
        return messages.filter { it.transmitter.equals(nodeName, ignoreCase = true) }
    }

    /**
     * Gets all pairs of (Message, Signal) received by a specific node (ECU)
     */
    fun getReceivedSignals(nodeName: String): List<Pair<DbcMessage, DbcSignal>> {
        return allSignals.filter { (_, sig) ->
            sig.receivers.any { it.equals(nodeName, ignoreCase = true) }
        }
    }

    /**
     * Gets all unique messages that contain at least one signal received by [nodeName]
     */
    fun getReceivedMessages(nodeName: String): List<DbcMessage> {
        return getReceivedSignals(nodeName).map { it.first }.distinct()
    }
}

data class DbcNode(
    val name: String,
    val comment: String? = null,
    val attributes: Map<String, String> = emptyMap()
)

data class DbcMessage(
    val id: Long,
    val name: String,
    val dlc: Int, // Length in bytes (usually 1..8, up to 64 for CAN-FD)
    val transmitter: String,
    val signals: List<DbcSignal> = emptyList(),
    val comment: String? = null,
    val attributes: Map<String, String> = emptyMap()
) {
    /**
     * CAN ID in DBC can have bit 31 set (0x80000000) indicating 29-bit Extended ID.
     */
    val isExtendedId: Boolean
        get() = (id and 0x80000000L) != 0L

    val cleanId: Long
        get() = if (isExtendedId) id and 0x1FFFFFFFL else id and 0x7FFL

    val hexId: String
        get() = "0x" + cleanId.toString(16).uppercase().padStart(if (isExtendedId) 8 else 3, '0')

    val totalBits: Int
        get() = dlc * 8

    val usedBits: Int
        get() = signals.sumOf { it.length }

    val utilizationPercentage: Float
        get() = if (totalBits > 0) (usedBits.toFloat() / totalBits.toFloat() * 100f).coerceIn(0f, 100f) else 0f
}

enum class Endianness(val label: String, val code: String) {
    LITTLE_ENDIAN("Intel (Little Endian)", "@1"),
    BIG_ENDIAN("Motorola (Big Endian)", "@0")
}

enum class ValueType(val symbol: String, val label: String) {
    UNSIGNED("+", "Unsigned ( + )"),
    SIGNED("-", "Signed ( - )"),
    IEEE_FLOAT("+", "IEEE Float") // Simplified representation in standard DBC
}

data class DbcSignal(
    val name: String,
    val multiplexing: String = "", // e.g. "M", "m0", "" for standard
    val startBit: Int,
    val length: Int,
    val endianness: Endianness = Endianness.LITTLE_ENDIAN,
    val valueType: ValueType = ValueType.UNSIGNED,
    val factor: Double = 1.0,
    val offset: Double = 0.0,
    val minimum: Double = 0.0,
    val maximum: Double = 0.0,
    val unit: String = "",
    val receivers: List<String> = emptyList(),
    val valueTable: List<ValueEntry> = emptyList(),
    val comment: String? = null,
    val attributes: Map<String, String> = emptyMap()
) {
    val isLittleEndian: Boolean
        get() = endianness == Endianness.LITTLE_ENDIAN

    val receiversDisplay: String
        get() = if (receivers.isEmpty()) "Vector__XXX" else receivers.joinToString(", ")

    val formulaDisplay: String
        get() = buildString {
            append("Physical = Raw")
            if (abs(factor - 1.0) > 0.000001) append(" × $factor")
            if (abs(offset) > 0.000001) {
                if (offset < 0) append(" - ${abs(offset)}") else append(" + $offset")
            }
            if (unit.isNotBlank()) append(" [$unit]")
        }
        
    /**
     * Calculates the set of bit indices (0..63) occupied by this signal in an 8-byte frame.
     */
    fun occupiedBits(maxBits: Int = 64): Set<Int> {
        val bits = mutableSetOf<Int>()
        var currentBit = startBit
        if (endianness == Endianness.LITTLE_ENDIAN) {
            // Intel format: counts upward continuously
            for (i in 0 until length) {
                if (currentBit in 0 until maxBits) {
                    bits.add(currentBit)
                }
                currentBit++
            }
        } else {
            // Motorola format: start bit is usually MSB in standard Vector convention, counts down within bytes
            for (i in 0 until length) {
                if (currentBit in 0 until maxBits) {
                    bits.add(currentBit)
                }
                val byteIndex = currentBit / 8
                val bitInByte = currentBit % 8
                if (bitInByte == 0) {
                    currentBit = (byteIndex + 1) * 8 + 7
                } else {
                    currentBit--
                }
            }
        }
        return bits
    }
}

data class ValueEntry(
    val value: Long,
    val description: String
)

/**
 * Assigns an aesthetic harmonious color to each signal in a message for visual bit mapping.
 */
object SignalColorPalette {
    val colors = listOf(
        Color(0xFF14B8A6), // Teal
        Color(0xFF06B6D4), // Cyan
        Color(0xFF3B82F6), // Blue
        Color(0xFF8B5CF6), // Violet
        Color(0xFFD946EF), // Fuchsia
        Color(0xFFF43F5E), // Rose
        Color(0xFFF97316), // Orange
        Color(0xFFEAB308), // Amber
        Color(0xFF10B981), // Emerald
        Color(0xFF6366F1)  // Indigo
    )

    fun getColor(index: Int): Color {
        return colors[abs(index) % colors.size]
    }
}
