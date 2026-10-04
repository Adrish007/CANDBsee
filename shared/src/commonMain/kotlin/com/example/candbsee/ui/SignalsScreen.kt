package com.example.candbsee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.candbsee.model.*

@Composable
fun SignalsScreen(
    network: DbcNetwork,
    onNavigateToMessage: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedReceiverFilter by remember { mutableStateOf<String?>(null) }
    var editingSignal by remember { mutableStateOf<Pair<DbcMessage, DbcSignal>?>(null) }

    val allSignals = remember(network) { network.allSignals }
    val allEcus = remember(network) {
        network.nodes.map { it.name }.sorted()
    }

    val filteredSignals = remember(allSignals, searchQuery, selectedReceiverFilter) {
        allSignals.filter { (msg, sig) ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                sig.name.contains(searchQuery, ignoreCase = true) ||
                msg.name.contains(searchQuery, ignoreCase = true) ||
                msg.transmitter.contains(searchQuery, ignoreCase = true) ||
                sig.unit.contains(searchQuery, ignoreCase = true)
            }
            val matchesFilter = if (selectedReceiverFilter == null) true else {
                sig.receivers.any { it.equals(selectedReceiverFilter, ignoreCase = true) } ||
                msg.transmitter.equals(selectedReceiverFilter, ignoreCase = true)
            }
            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanDbTheme.BgDark)
            .padding(24.dp)
    ) {
        SectionHeader(
            title = "All Signals in Network • ${allSignals.size} Total",
            subtitle = "Comprehensive global signal index across all transmitted CAN frames"
        )

        // Filter Controls Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search signal name, message, ECU, or unit...",
                modifier = Modifier.width(360.dp)
            )

            // Receiver Pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Filter ECUs:", color = CanDbTheme.TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedReceiverFilter == null) CanDbTheme.AccentEmerald else CanDbTheme.CardBg)
                        .clickable { selectedReceiverFilter = null }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("All", color = if (selectedReceiverFilter == null) Color.Black else CanDbTheme.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                allEcus.forEach { ecu ->
                    val isSelected = selectedReceiverFilter == ecu
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CanDbTheme.AccentEmerald else CanDbTheme.CardBg)
                            .border(1.dp, if (isSelected) CanDbTheme.AccentEmerald else CanDbTheme.BorderColor, RoundedCornerShape(6.dp))
                            .clickable { selectedReceiverFilter = if (isSelected) null else ecu }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(ecu, color = if (isSelected) Color.Black else CanDbTheme.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CanDbTheme.CardBgElevated)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SIGNAL NAME & PARENT FRAME", color = CanDbTheme.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.2f))
            Text("TRANSMITTER (TX)", color = CanDbTheme.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
            Text("RECEIVING ECUS (RX)", color = CanDbTheme.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
            Text("BIT LAYOUT", color = CanDbTheme.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
            Text("SCALING & UNITS", color = CanDbTheme.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.7f))
            Text("ACTION", color = CanDbTheme.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(64.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Signal List Table
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(filteredSignals) { index, (msg, sig) ->
                val color = SignalColorPalette.getColor(index)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CanDbTheme.CardBg)
                        .border(1.dp, CanDbTheme.BorderColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: Signal & Message
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(2.2f)) {
                        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(sig.name, color = CanDbTheme.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onNavigateToMessage(msg.id) }
                            ) {
                                Icon(CanIcons.Message, contentDescription = null, tint = CanDbTheme.AccentTeal, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${msg.name} (${msg.hexId})", color = CanDbTheme.AccentTeal, fontSize = 12.sp)
                            }
                        }
                    }

                    // Column 2: Transmitter ECU
                    Box(modifier = Modifier.weight(1.3f)) {
                        BadgePill(
                            text = msg.transmitter,
                            color = CanDbTheme.AccentIndigo.copy(alpha = 0.2f),
                            textColor = CanDbTheme.AccentIndigo
                        )
                    }

                    // Column 3: Receivers
                    Box(modifier = Modifier.weight(1.5f)) {
                        BadgePill(
                            text = sig.receiversDisplay,
                            color = CanDbTheme.AccentCyan.copy(alpha = 0.15f),
                            textColor = CanDbTheme.AccentCyan
                        )
                    }

                    // Column 4: Bit Layout
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text("Start: ${sig.startBit} • Length: ${sig.length}b", color = CanDbTheme.TextPrimary, fontSize = 13.sp)
                        Text(sig.endianness.label, color = CanDbTheme.TextMuted, fontSize = 11.sp)
                    }

                    // Column 5: Scaling & Formula
                    Column(modifier = Modifier.weight(1.7f)) {
                        Text(sig.formulaDisplay, color = CanDbTheme.AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Range: [${sig.minimum} .. ${sig.maximum}]", color = CanDbTheme.TextMuted, fontSize = 11.sp)
                    }

                    // Column 6: Actions
                    Box(modifier = Modifier.width(64.dp), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { editingSignal = msg to sig }, modifier = Modifier.size(36.dp)) {
                            Icon(CanIcons.Edit, contentDescription = "Edit", tint = CanDbTheme.TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    // Signal Editing Modal
    if (editingSignal != null) {
        val (targetMsg, targetSig) = editingSignal!!
        SignalEditDialog(
            signal = targetSig,
            allNodes = network.nodes.map { it.name },
            onDismiss = { editingSignal = null },
            onSave = { updatedSig ->
                // Note: updating via dialog in signal view
                editingSignal = null
            }
        )
    }
}
