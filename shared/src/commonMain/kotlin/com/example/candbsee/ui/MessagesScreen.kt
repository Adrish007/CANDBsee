package com.example.candbsee.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.candbsee.model.*

@Composable
fun MessagesScreen(
    network: DbcNetwork,
    selectedMessageId: Long?,
    onSelectMessage: (Long) -> Unit,
    onNewMessage: () -> Unit,
    onUpdateMessage: (DbcMessage) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredMessages = remember(network.messages, searchQuery) {
        if (searchQuery.isBlank()) network.messages
        else network.messages.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.hexId.contains(searchQuery, ignoreCase = true) ||
            it.transmitter.contains(searchQuery, ignoreCase = true)
        }
    }

    val currentMessage = remember(network.messages, selectedMessageId) {
        network.messages.find { it.id == selectedMessageId } ?: network.messages.firstOrNull()
    }

    var editingSignal by remember { mutableStateOf<DbcSignal?>(null) }
    var showNewSignalDialog by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize().background(CanDbTheme.BgDark)) {
        // Left Pane: CAN Frame Master List
        Column(
            modifier = Modifier
                .width(340.dp)
                .fillMaxHeight()
                .border(width = 1.dp, color = CanDbTheme.BorderColor, shape = RoundedCornerShape(0.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Messages (${network.messages.size})", color = CanDbTheme.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onNewMessage, modifier = Modifier.size(36.dp)) {
                    Icon(CanIcons.Add, contentDescription = "New Message", tint = CanDbTheme.AccentCyan, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            SearchTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = "Search ID or Name...")
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(filteredMessages) { msg ->
                    val isSelected = msg.id == currentMessage?.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CanDbTheme.CardBgElevated else CanDbTheme.CardBg)
                            .border(1.dp, if (isSelected) CanDbTheme.AccentCyan else CanDbTheme.BorderColor, RoundedCornerShape(8.dp))
                            .clickable { onSelectMessage(msg.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BadgePill(text = msg.hexId, color = CanDbTheme.AccentAmber.copy(0.2f), textColor = CanDbTheme.AccentAmber)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(msg.name, color = if (isSelected) CanDbTheme.AccentCyan else CanDbTheme.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BadgePill(text = "TX: ${msg.transmitter}", color = CanDbTheme.AccentIndigo.copy(0.15f), textColor = CanDbTheme.AccentIndigo, fontSize = 10)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${msg.signals.size} Signals (${msg.dlc}B)", color = CanDbTheme.TextSecondary, fontSize = 11.sp)
                            }
                        }
                        Icon(CanIcons.ArrowRight, contentDescription = null, tint = if (isSelected) CanDbTheme.AccentCyan else CanDbTheme.TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Right Pane: Message Specification & 64-bit Interactive Matrix
        Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(24.dp)) {
            if (currentMessage != null) {
                MessageInspectorPanel(
                    msg = currentMessage,
                    onAddSignal = { showNewSignalDialog = true },
                    onEditSignal = { editingSignal = it },
                    onDeleteSignal = { sig ->
                        onUpdateMessage(currentMessage.copy(signals = currentMessage.signals - sig))
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a CAN frame to inspect its bit matrix and signals.", color = CanDbTheme.TextMuted)
                }
            }
        }
    }

    // Signal Editing Modal / Dialog
    if (editingSignal != null || showNewSignalDialog) {
        val targetSignal = editingSignal ?: DbcSignal(name = "New_Signal", startBit = 0, length = 8)
        SignalEditDialog(
            signal = targetSignal,
            allNodes = network.nodes.map { it.name },
            attributeDefs = network.attributeDefinitions.values.filter { it.objectType == AttributeObjectType.SIGNAL },
            onDismiss = {
                editingSignal = null
                showNewSignalDialog = false
            },
            onSave = { newSig ->
                if (showNewSignalDialog && currentMessage != null) {
                    onUpdateMessage(currentMessage.copy(signals = currentMessage.signals + newSig))
                } else if (currentMessage != null && editingSignal != null) {
                    val updated = currentMessage.signals.map { if (it.name == editingSignal!!.name) newSig else it }
                    onUpdateMessage(currentMessage.copy(signals = updated))
                }
                editingSignal = null
                showNewSignalDialog = false
            }
        )
    }
}

@Composable
fun MessageInspectorPanel(
    msg: DbcMessage,
    onAddSignal: () -> Unit,
    onEditSignal: (DbcSignal) -> Unit,
    onDeleteSignal: (DbcSignal) -> Unit
) {
    var highlightedSignalIndex by remember { mutableStateOf<Int?>(null) }
    var isMatrixExpanded by remember { mutableStateOf(true) }

    // Precalculate bit occupation map for matrix rendering
    val bitToSignalIndex = remember(msg.signals) {
        val map = mutableMapOf<Int, Int>()
        msg.signals.forEachIndexed { index, sig ->
            val bits = sig.occupiedBits(msg.dlc * 8)
            for (bit in bits) {
                map[bit] = index
            }
        }
        map
    }

    // Single unified LazyColumn so the header, matrix, and signal table scroll fluidly together!
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Item 1: Message Header Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CanDbTheme.CardBg)
                    .border(1.dp, CanDbTheme.BorderColor, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgePill(text = msg.hexId, color = CanDbTheme.AccentAmber.copy(0.2f), textColor = CanDbTheme.AccentAmber, fontSize = 16)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(msg.name, color = CanDbTheme.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ID: ${msg.cleanId} (Dec)", color = CanDbTheme.TextSecondary, fontSize = 13.sp)
                        Text(" • ", color = CanDbTheme.TextMuted)
                        Text(if (msg.isExtendedId) "29-Bit Extended CAN" else "11-Bit Standard CAN", color = CanDbTheme.AccentTeal, fontSize = 13.sp)
                        Text(" • ", color = CanDbTheme.TextMuted)
                        Text("Transmitter ECU: ${msg.transmitter}", color = CanDbTheme.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = onAddSignal,
                    colors = ButtonDefaults.buttonColors(containerColor = CanDbTheme.AccentCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(CanIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Signal", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Item 2: Collapsible & Scrollable CAN Frame Matrix Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CanDbTheme.CardBg)
                    .border(1.dp, CanDbTheme.BorderColor, RoundedCornerShape(12.dp))
            ) {
                // Toggle header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CanDbTheme.CardBgElevated)
                        .clickable { isMatrixExpanded = !isMatrixExpanded }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isMatrixExpanded) CanIcons.ArrowDown else CanIcons.ArrowRight,
                            contentDescription = "Toggle Matrix",
                            tint = CanDbTheme.AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CAN FRAME BIT LAYOUT MATRIX (BYTES 0..${msg.dlc - 1})",
                            color = CanDbTheme.TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    BadgePill(
                        text = if (isMatrixExpanded) "Hide Matrix" else "Show Matrix",
                        color = CanDbTheme.BgDark,
                        textColor = CanDbTheme.AccentCyan,
                        fontSize = 11
                    )
                }

                if (isMatrixExpanded) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header row for bits 7 to 0
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Byte \\ Bit", color = CanDbTheme.TextMuted, fontSize = 12.sp, modifier = Modifier.width(64.dp))
                            for (b in 7 downTo 0) {
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    Text("B$b", color = CanDbTheme.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = CanDbTheme.BorderColor)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Rows for each byte in DLC
                        for (byteIdx in 0 until msg.dlc) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Byte $byteIdx",
                                    color = CanDbTheme.TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(64.dp)
                                )

                                for (bitInByte in 7 downTo 0) {
                                    val globalBitIndex = byteIdx * 8 + bitInByte
                                    val signalIdx = bitToSignalIndex[globalBitIndex]
                                    val isHighlighted = highlightedSignalIndex != null && highlightedSignalIndex == signalIdx

                                    val cellColor = if (signalIdx != null) {
                                        val baseColor = SignalColorPalette.getColor(signalIdx)
                                        if (highlightedSignalIndex == null || isHighlighted) baseColor else baseColor.copy(alpha = 0.25f)
                                    } else {
                                        CanDbTheme.BgDark
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(cellColor)
                                            .border(
                                                width = if (isHighlighted) 2.dp else 1.dp,
                                                color = if (isHighlighted) Color.White else CanDbTheme.BorderColor,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                if (signalIdx != null) {
                                                    highlightedSignalIndex = if (highlightedSignalIndex == signalIdx) null else signalIdx
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$globalBitIndex",
                                            color = if (signalIdx != null) Color.Black else CanDbTheme.TextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = if (signalIdx != null) FontWeight.Black else FontWeight.Normal,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Item 3: Signal List Header & Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONTAINED SIGNALS (${msg.signals.size})",
                    color = CanDbTheme.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (highlightedSignalIndex != null) {
                    Text(
                        text = "Clear Filter ✕",
                        color = CanDbTheme.AccentRose,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { highlightedSignalIndex = null }
                    )
                }
            }
        }

        // Items: All signal rows inside the same scrollable column!
        itemsIndexed(msg.signals) { index, sig ->
            val isHighlighted = highlightedSignalIndex == index
            val color = SignalColorPalette.getColor(index)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isHighlighted) CanDbTheme.CardBgElevated else CanDbTheme.CardBg)
                    .border(
                        width = if (isHighlighted) 2.dp else 1.dp,
                        color = if (isHighlighted) color else CanDbTheme.BorderColor,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        highlightedSignalIndex = if (highlightedSignalIndex == index) null else index
                    }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(sig.name, color = CanDbTheme.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Start: ${sig.startBit} • Len: ${sig.length} bits • ${sig.endianness.label} • Range: [${sig.minimum}..${sig.maximum}]",
                            color = CanDbTheme.TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgePill(text = sig.formulaDisplay, color = CanDbTheme.CardBgHover, textColor = CanDbTheme.AccentTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgePill(text = "RX: ${sig.receiversDisplay}", color = CanDbTheme.AccentCyan.copy(0.15f), textColor = CanDbTheme.AccentCyan)
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = { onEditSignal(sig) }, modifier = Modifier.size(36.dp)) {
                        Icon(CanIcons.Edit, contentDescription = "Edit", tint = CanDbTheme.TextPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { onDeleteSignal(sig) }, modifier = Modifier.size(36.dp)) {
                        Icon(CanIcons.Delete, contentDescription = "Delete", tint = CanDbTheme.AccentRose, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
