package com.example.candbsee.parser

import com.example.candbsee.model.*

/**
 * Robust CAN DBC syntax parser and serializer.
 */
object DbcParser {

    /**
     * Parses a raw DBC file content string into a complete [DbcNetwork] domain representation.
     */
    fun parse(fileName: String, content: String): DbcNetwork {
        var version = "1.0"
        val nodes = mutableMapOf<String, DbcNode>()
        val messages = mutableListOf<DbcMessage>()
        var currentMessage: DbcMessage? = null
        val currentSignals = mutableListOf<DbcSignal>()
        val valueTables = mutableMapOf<Pair<Long, String>, List<ValueEntry>>()
        val comments = mutableMapOf<String, String>()
        val signalComments = mutableMapOf<Pair<Long, String>, String>()
        
        val attributeDefs = mutableMapOf<String, AttributeDefinition>()
        val globalAttributes = mutableMapOf<String, String>()
        val nodeAttributes = mutableMapOf<String, MutableMap<String, String>>()
        val messageAttributes = mutableMapOf<Long, MutableMap<String, String>>()
        val signalAttributes = mutableMapOf<Pair<Long, String>, MutableMap<String, String>>()

        val lines = content.lines().map { it.trim() }
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            when {
                line.startsWith("VERSION", ignoreCase = true) -> {
                    val parts = line.split("\"")
                    if (parts.size >= 2) {
                        version = parts[1]
                    }
                }
                line.startsWith("BU_:") -> {
                    val nodeNames = line.substringAfter("BU_:").trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
                    for (name in nodeNames) {
                        if (name != "Vector__XXX") {
                            nodes[name] = DbcNode(name = name)
                        }
                    }
                }
                line.startsWith("BO_ ") -> {
                    if (currentMessage != null) {
                        messages.add(currentMessage.copy(signals = currentSignals.toList()))
                        currentSignals.clear()
                    }
                    try {
                        val parts = line.split("\\s+".toRegex())
                        val id = parts[1].toLong()
                        val name = parts[2].removeSuffix(":")
                        val dlc = parts[3].toInt()
                        val transmitter = if (parts.size > 4) parts[4] else "Vector__XXX"
                        currentMessage = DbcMessage(id = id, name = name, dlc = dlc, transmitter = transmitter)
                        if (transmitter != "Vector__XXX" && !nodes.containsKey(transmitter)) {
                            nodes[transmitter] = DbcNode(name = transmitter)
                        }
                    } catch (e: Exception) {}
                }
                line.startsWith("SG_ ") && currentMessage != null -> {
                    try {
                        val parsedSignal = parseSignalLine(line)
                        if (parsedSignal != null) {
                            currentSignals.add(parsedSignal)
                            for (receiver in parsedSignal.receivers) {
                                if (receiver.isNotBlank() && receiver != "Vector__XXX" && !nodes.containsKey(receiver)) {
                                    nodes[receiver] = DbcNode(name = receiver)
                                }
                            }
                        }
                    } catch (e: Exception) {}
                }
                line.startsWith("VAL_ ") -> {
                    try {
                        val tokens = line.removePrefix("VAL_").trim().removeSuffix(";").trim()
                        val parts = tokens.split("\\s+".toRegex())
                        if (parts.size >= 4) {
                            val msgId = parts[0].toLongOrNull()
                            if (msgId != null) {
                                val signalName = parts[1]
                                val entries = parseValueEntries(tokens.substringAfter(signalName).trim())
                                valueTables[Pair(msgId, signalName)] = entries
                            }
                        }
                    } catch (e: Exception) {}
                }
                line.startsWith("BA_DEF_ ") -> {
                    try {
                        val parsedDef = parseAttributeDef(line)
                        if (parsedDef != null) {
                            attributeDefs[parsedDef.name] = parsedDef
                        }
                    } catch (e: Exception) {}
                }
                line.startsWith("BA_DEF_DEF_ ") -> {
                    try {
                        val name = extractBetween(line, '"', '"')
                        if (name != null) {
                            val def = attributeDefs[name]
                            if (def != null) {
                                val defaultVal = line.substringAfterLast('"').removeSuffix(";").trim()
                                attributeDefs[name] = def.copy(defaultValue = defaultVal)
                            }
                        }
                    } catch (e: Exception) {}
                }
                line.startsWith("BA_ ") -> {
                    try {
                        val name = extractBetween(line, '"', '"')
                        if (name != null) {
                            val rest = line.substringAfterLast('"').removeSuffix(";").trim()
                            val parts = rest.split("\\s+".toRegex())
                            if (parts.isNotEmpty() && parts[0] == "BO_") {
                                val id = parts.getOrNull(1)?.toLongOrNull()
                                val value = parts.getOrNull(2) ?: ""
                                if (id != null) {
                                    messageAttributes.getOrPut(id) { mutableMapOf() }[name] = value
                                }
                            } else if (parts.isNotEmpty() && parts[0] == "BU_") {
                                val nodeName = parts.getOrNull(1)
                                val value = parts.getOrNull(2) ?: ""
                                if (nodeName != null) {
                                    nodeAttributes.getOrPut(nodeName) { mutableMapOf() }[name] = value
                                }
                            } else if (parts.isNotEmpty() && parts[0] == "SG_") {
                                val id = parts.getOrNull(1)?.toLongOrNull()
                                val sigName = parts.getOrNull(2)
                                val value = parts.getOrNull(3) ?: ""
                                if (id != null && sigName != null) {
                                    signalAttributes.getOrPut(Pair(id, sigName)) { mutableMapOf() }[name] = value
                                }
                            } else if (parts.isNotEmpty()) {
                                val value = parts.joinToString(" ")
                                globalAttributes[name] = value
                            }
                        }
                    } catch (e: Exception) {}
                }
                line.startsWith("CM_ BO_") -> {
                    val commentText = extractCommentText(line)
                    val parts = line.split("\\s+".toRegex())
                    if (parts.size >= 3) {
                        val id = parts[2].toLongOrNull()
                        if (id != null && commentText != null) {
                            comments["BO_$id"] = commentText
                        }
                    }
                }
                line.startsWith("CM_ SG_") -> {
                    val commentText = extractCommentText(line)
                    val parts = line.split("\\s+".toRegex())
                    if (parts.size >= 4) {
                        val id = parts[2].toLongOrNull()
                        val sigName = parts[3]
                        if (id != null && commentText != null) {
                            signalComments[Pair(id, sigName)] = commentText
                        }
                    }
                }
                line.startsWith("CM_ BU_") -> {
                    val commentText = extractCommentText(line)
                    val parts = line.split("\\s+".toRegex())
                    if (parts.size >= 3) {
                        val nodeName = parts[2]
                        if (commentText != null) {
                            comments["BU_$nodeName"] = commentText
                        }
                    }
                }
            }
            i++
        }
        if (currentMessage != null) {
            messages.add(currentMessage.copy(signals = currentSignals.toList()))
        }

        val enrichedNodes = nodes.values.map { node ->
            node.copy(
                comment = comments["BU_${node.name}"],
                attributes = nodeAttributes[node.name] ?: emptyMap()
            )
        }.sortedBy { it.name }

        val enrichedMessages = messages.map { msg ->
            val msgComment = comments["BO_${msg.id}"]
            val msgAttrs = messageAttributes[msg.id] ?: emptyMap()
            
            val enrichedSignals = msg.signals.map { sig ->
                val sigComment = signalComments[Pair(msg.id, sig.name)]
                val valTable = valueTables[Pair(msg.id, sig.name)] ?: emptyList()
                val sigAttrs = signalAttributes[Pair(msg.id, sig.name)] ?: emptyMap()
                sig.copy(comment = sigComment, valueTable = valTable, attributes = sigAttrs)
            }
            msg.copy(signals = enrichedSignals, comment = msgComment, attributes = msgAttrs)
        }.sortedBy { it.cleanId }

        return DbcNetwork(
            fileName = fileName,
            version = version,
            nodes = enrichedNodes,
            messages = enrichedMessages,
            comments = comments,
            valueTables = emptyMap(),
            attributeDefinitions = attributeDefs,
            attributes = globalAttributes
        )
    }

    private fun parseAttributeDef(line: String): AttributeDefinition? {
        val tokens = line.removePrefix("BA_DEF_ ").trim().removeSuffix(";").trim()
        val parts = tokens.split("\\s+".toRegex())
        
        var objectType = AttributeObjectType.NETWORK
        
        when (parts[0]) {
            "BU_" -> { objectType = AttributeObjectType.NODE }
            "BO_" -> { objectType = AttributeObjectType.MESSAGE }
            "SG_" -> { objectType = AttributeObjectType.SIGNAL }
            "EV_" -> { objectType = AttributeObjectType.ENVIRONMENT }
        }
        
        val name = extractBetween(tokens, '"', '"') ?: return null
        
        val afterNameIndex = tokens.indexOf('"', tokens.indexOf('"') + 1) + 1
        val afterNameStr = tokens.substring(afterNameIndex).trim()
        val afterNameParts = afterNameStr.split("\\s+".toRegex())
        
        if (afterNameParts.isEmpty()) return null
        val typeStr = afterNameParts[0]
        
        val valueType = when (typeStr) {
            "INT" -> AttributeValueType.INT
            "HEX" -> AttributeValueType.HEX
            "FLOAT" -> AttributeValueType.FLOAT
            "STRING" -> AttributeValueType.STRING
            "ENUM" -> AttributeValueType.ENUM
            else -> AttributeValueType.STRING
        }
        
        var min = 0.0
        var max = 0.0
        var enumVals = emptyList<String>()
        
        if (valueType == AttributeValueType.INT || valueType == AttributeValueType.FLOAT || valueType == AttributeValueType.HEX) {
            min = afterNameParts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
            max = afterNameParts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
        } else if (valueType == AttributeValueType.ENUM) {
            val enumStr = extractBetween(afterNameStr, '"', '"')
            if (enumStr != null) {
                enumVals = afterNameStr.split(",").mapNotNull { extractBetween(it, '"', '"') }
            }
        }
        
        return AttributeDefinition(
            name = name,
            objectType = objectType,
            valueType = valueType,
            minimum = min,
            maximum = max,
            enumValues = enumVals
        )
    }

    private fun parseSignalLine(line: String): DbcSignal? {
        val afterSg = line.removePrefix("SG_").trim()
        val colonIndex = afterSg.indexOf(':')
        if (colonIndex < 0) return null

        val nameAndMux = afterSg.substring(0, colonIndex).trim().split("\\s+".toRegex())
        val name = nameAndMux[0]
        val mux = if (nameAndMux.size > 1) nameAndMux[1] else ""

        val details = afterSg.substring(colonIndex + 1).trim()
        val atIndex = details.indexOf('@')
        if (atIndex < 0) return null

        val bitInfo = details.substring(0, atIndex).split('|')
        val startBit = bitInfo[0].trim().toInt()
        val length = bitInfo[1].trim().toInt()

        val endianAndSign = details.substring(atIndex + 1).trim()
        val isLittleEndian = endianAndSign.startsWith("1")
        val isSigned = endianAndSign.length > 1 && endianAndSign[1] == '-'

        val factorOffsetStr = extractBetween(details, '(', ')')
        val factorOffset = factorOffsetStr?.split(',') ?: listOf("1", "0")
        val factor = factorOffset.getOrNull(0)?.trim()?.toDoubleOrNull() ?: 1.0
        val offset = factorOffset.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0

        val minMaxStr = extractBetween(details, '[', ']')
        val minMax = minMaxStr?.split('|') ?: listOf("0", "0")
        val min = minMax.getOrNull(0)?.trim()?.toDoubleOrNull() ?: 0.0
        val max = minMax.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0

        val unit = extractBetween(details, '"', '"') ?: ""
        val receiversStr = details.substringAfterLast('"').trim()
        val receivers = if (receiversStr.isNotBlank()) {
            receiversStr.split(',').map { it.trim() }.filter { it.isNotBlank() && it != "Vector__XXX" }
        } else {
            emptyList()
        }

        return DbcSignal(
            name = name,
            multiplexing = mux,
            startBit = startBit,
            length = length,
            endianness = if (isLittleEndian) Endianness.LITTLE_ENDIAN else Endianness.BIG_ENDIAN,
            valueType = if (isSigned) ValueType.SIGNED else ValueType.UNSIGNED,
            factor = factor,
            offset = offset,
            minimum = min,
            maximum = max,
            unit = unit,
            receivers = receivers
        )
    }

    private fun parseValueEntries(str: String): List<ValueEntry> {
        val results = mutableListOf<ValueEntry>()
        var remaining = str
        while (remaining.isNotEmpty()) {
            val firstQuote = remaining.indexOf('"')
            if (firstQuote < 0) break
            val secondQuote = remaining.indexOf('"', firstQuote + 1)
            if (secondQuote < 0) break

            val numberPart = remaining.substring(0, firstQuote).trim()
            val valNum = numberPart.split("\\s+".toRegex()).lastOrNull()?.toLongOrNull()
            val desc = remaining.substring(firstQuote + 1, secondQuote)
            if (valNum != null) {
                results.add(ValueEntry(valNum, desc))
            }
            remaining = remaining.substring(secondQuote + 1).trim()
        }
        return results.sortedBy { it.value }
    }

    private fun extractBetween(source: String, startChar: Char, endChar: Char): String? {
        val s = source.indexOf(startChar)
        if (s < 0) return null
        val e = source.indexOf(endChar, s + 1)
        if (e < 0) return null
        return source.substring(s + 1, e)
    }

    private fun extractCommentText(line: String): String? {
        val first = line.indexOf('"')
        val last = line.lastIndexOf('"')
        return if (first in 0 until last) line.substring(first + 1, last) else null
    }

    fun serialize(network: DbcNetwork): String {
        return buildString {
            appendLine("VERSION \"${network.version}\"")
            appendLine()
            appendLine("NS_ : ")
            appendLine("\tNS_DESC_")
            appendLine("\tCM_")
            appendLine("\tBA_DEF_")
            appendLine("\tBA_")
            appendLine("\tVAL_")
            appendLine("\tCAT_DEF_")
            appendLine("\tCAT_")
            appendLine("BS_: ")
            appendLine()

            val nodeNames = if (network.nodes.isEmpty()) "Vector__XXX" else network.nodes.joinToString(" ") { it.name }
            appendLine("BU_: $nodeNames")
            appendLine()

            for (msg in network.messages) {
                appendLine("BO_ ${msg.id} ${msg.name}: ${msg.dlc} ${msg.transmitter}")
                for (sig in msg.signals) {
                    val muxStr = if (sig.multiplexing.isNotBlank()) " ${sig.multiplexing}" else ""
                    val endianChar = if (sig.isLittleEndian) "1" else "0"
                    val signChar = if (sig.valueType == ValueType.SIGNED) "-" else "+"
                    val factorStr = formatDouble(sig.factor)
                    val offsetStr = formatDouble(sig.offset)
                    val minStr = formatDouble(sig.minimum)
                    val maxStr = formatDouble(sig.maximum)
                    val receiversStr = if (sig.receivers.isEmpty()) "Vector__XXX" else sig.receivers.joinToString(",")
                    appendLine(" SG_ ${sig.name}$muxStr : ${sig.startBit}|${sig.length}@$endianChar$signChar ($factorStr,$offsetStr) [$minStr|$maxStr] \"${sig.unit}\" $receiversStr")
                }
                appendLine()
            }

            for (node in network.nodes) {
                if (!node.comment.isNullOrBlank()) {
                    appendLine("CM_ BU_ ${node.name} \"${node.comment}\";")
                }
            }
            for (msg in network.messages) {
                if (!msg.comment.isNullOrBlank()) {
                    appendLine("CM_ BO_ ${msg.id} \"${msg.comment}\";")
                }
                for (sig in msg.signals) {
                    if (!sig.comment.isNullOrBlank()) {
                        appendLine("CM_ SG_ ${msg.id} ${sig.name} \"${sig.comment}\";")
                    }
                }
            }
            appendLine()
            
            for (def in network.attributeDefinitions.values) {
                val objType = when (def.objectType) {
                    AttributeObjectType.NODE -> "BU_ "
                    AttributeObjectType.MESSAGE -> "BO_ "
                    AttributeObjectType.SIGNAL -> "SG_ "
                    AttributeObjectType.ENVIRONMENT -> "EV_ "
                    AttributeObjectType.NETWORK -> ""
                }
                val typeStr = def.valueType.keyword
                val rangeStr = if (def.valueType == AttributeValueType.INT || def.valueType == AttributeValueType.FLOAT || def.valueType == AttributeValueType.HEX) {
                    " ${formatDouble(def.minimum)} ${formatDouble(def.maximum)}"
                } else if (def.valueType == AttributeValueType.ENUM) {
                    " \"" + def.enumValues.joinToString("\",\"") + "\""
                } else {
                    ""
                }
                appendLine("BA_DEF_ $objType\"${def.name}\" $typeStr$rangeStr;")
            }
            
            for (def in network.attributeDefinitions.values) {
                if (def.defaultValue.isNotBlank()) {
                    val defaultValStr = if (def.valueType == AttributeValueType.STRING) "\"${def.defaultValue}\"" else def.defaultValue
                    appendLine("BA_DEF_DEF_ \"${def.name}\" $defaultValStr;")
                }
            }
            appendLine()
            
            for ((key, value) in network.attributes) {
                val def = network.attributeDefinitions[key]
                val valStr = if (def?.valueType == AttributeValueType.STRING) "\"$value\"" else value
                appendLine("BA_ \"$key\" $valStr;")
            }
            for (node in network.nodes) {
                for ((key, value) in node.attributes) {
                    val def = network.attributeDefinitions[key]
                    val valStr = if (def?.valueType == AttributeValueType.STRING) "\"$value\"" else value
                    appendLine("BA_ \"$key\" BU_ ${node.name} $valStr;")
                }
            }
            for (msg in network.messages) {
                for ((key, value) in msg.attributes) {
                    val def = network.attributeDefinitions[key]
                    val valStr = if (def?.valueType == AttributeValueType.STRING) "\"$value\"" else value
                    appendLine("BA_ \"$key\" BO_ ${msg.id} $valStr;")
                }
                for (sig in msg.signals) {
                    for ((key, value) in sig.attributes) {
                        val def = network.attributeDefinitions[key]
                        val valStr = if (def?.valueType == AttributeValueType.STRING) "\"$value\"" else value
                        appendLine("BA_ \"$key\" SG_ ${msg.id} ${sig.name} $valStr;")
                    }
                }
            }
            appendLine()

            for (msg in network.messages) {
                for (sig in msg.signals) {
                    if (sig.valueTable.isNotEmpty()) {
                        val valEntries = sig.valueTable.joinToString(" ") { "${it.value} \"${it.description}\"" }
                        appendLine("VAL_ ${msg.id} ${sig.name} $valEntries ;")
                    }
                }
            }
            appendLine()
        }
    }

    private fun formatDouble(d: Double): String {
        val longVal = d.toLong()
        return if (d == longVal.toDouble()) longVal.toString() else d.toString()
    }
}
