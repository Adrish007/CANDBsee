package com.example.candbsee.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun NodesScreen(
    network: DbcNetwork,
    selectedNodeName: String?,
    onSelectNode: (String) -> Unit,
    onNewNode: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredNodes = remember(network.nodes, searchQuery) {
        if (searchQuery.isBlank()) network.nodes
        else network.nodes.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            (it.comment ?: "").contains(searchQuery, ignoreCase = true)
        }
    }

    val currentNode = remember(network.nodes, selectedNodeName) {
        network.nodes.find { it.name == selectedNodeName } ?: network.nodes.firstOrNull()
    }

    Row(modifier = Modifier.fillMaxSize().background(CanDbTheme.BgDark)) {
        // Left Pane: ECU List & Search
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
                Text("ECUs (${network.nodes.size})", color = CanDbTheme.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onNewNode, modifier = Modifier.size(36.dp)) {
                    Icon(CanIcons.Add, contentDescription = "Add ECU", tint = CanDbTheme.AccentTeal, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            SearchTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search ECUs or desc..."
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(filteredNodes) { node ->
                    val isSelected = node.name == currentNode?.name
                    val txCount = remember(network, node.name) { network.getTransmittedMessages(node.name).size }
                    val rxCount = remember(network, node.name) { network.getReceivedSignals(node.name).size }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CanDbTheme.CardBgElevated else CanDbTheme.CardBg)
                            .border(1.dp, if (isSelected) CanDbTheme.AccentTeal else CanDbTheme.BorderColor, RoundedCornerShape(8.dp))
                            .clickable { onSelectNode(node.name) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(CanIcons.Node, contentDescription = null, tint = if (isSelected) CanDbTheme.AccentTeal else CanDbTheme.TextPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = node.name,
                                    color = if (isSelected) CanDbTheme.AccentTeal else CanDbTheme.TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            if (!node.comment.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(node.comment, color = CanDbTheme.TextSecondary, fontSize = 12.sp, maxLines = 1)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BadgePill(text = "TX $txCount", color = CanDbTheme.AccentEmerald.copy(0.15f), textColor = CanDbTheme.AccentEmerald, fontSize = 10)
                                Spacer(modifier = Modifier.width(6.dp))
                                BadgePill(text = "RX $rxCount sigs", color = CanDbTheme.AccentCyan.copy(0.15f), textColor = CanDbTheme.AccentCyan, fontSize = 10)
                            }
                        }
                        Icon(CanIcons.ArrowRight, contentDescription = null, tint = if (isSelected) CanDbTheme.AccentTeal else CanDbTheme.TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Right Pane: Nodewise Inspector
        Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(24.dp)) {
            if (currentNode != null) {
                NodewiseInspector(node = currentNode, network = network)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select an ECU on the left to inspect its messages and signals.", color = CanDbTheme.TextMuted)
                }
            }
        }
    }
}

@Composable
fun NodewiseInspector(node: DbcNode, network: DbcNetwork) {
    val txMessages = remember(network, node.name) { network.getTransmittedMessages(node.name) }
    val rxSignals = remember(network, node.name) { network.getReceivedSignals(node.name) }
    val rxByMessage = remember(rxSignals) { rxSignals.groupBy { it.first } }

    var selectedSection by remember { mutableStateOf(0) } // 0 = Transmitted (TX), 1 = Received (RX)

    Column(modifier = Modifier.fillMaxSize()) {
        // ECU Header
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CanDbTheme.AccentIndigo.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(CanIcons.Node, contentDescription = null, tint = CanDbTheme.AccentIndigo, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(node.name, color = CanDbTheme.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text(
                        node.comment ?: "No additional engineering description provided for this ECU.",
                        color = CanDbTheme.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Tab Switcher for TX vs RX
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CanDbTheme.BgDark)
                    .border(1.dp, CanDbTheme.BorderColor, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedSection == 0) CanDbTheme.AccentTeal else Color.Transparent)
                        .clickable { selectedSection = 0 }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Transmitted (${txMessages.size} msgs)",
                        color = if (selectedSection == 0) Color.Black else CanDbTheme.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedSection == 1) CanDbTheme.AccentTeal else Color.Transparent)
                        .clickable { selectedSection = 1 }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Received (${rxByMessage.size} msgs)",
                        color = if (selectedSection == 1) Color.Black else CanDbTheme.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content
        if (selectedSection == 0) {
            // TX Messages and Signals
            Text(
                text = "TRANSMITTED MESSAGES & CONTAINED SIGNALS",
                color = CanDbTheme.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (txMessages.isEmpty()) {
                EmptyStateCard("This ECU does not currently transmit any CAN frames.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                    items(txMessages) { msg ->
                        MessageNodeAccordion(message = msg, isTx = true)
                    }
                }
            }
        } else {
            // RX Signals grouped by transmitting frame
            Text(
                text = "RECEIVED BUS SIGNALS (GROUPED BY SOURCE MESSAGE)",
                color = CanDbTheme.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (rxByMessage.isEmpty()) {
                EmptyStateCard("This ECU is not listed as a receiver (RX) for any network signals.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                    items(rxByMessage.toList()) { (msg, signals) ->
                        RxMessageNodeCard(message = msg, signals = signals.map { it.second })
                    }
                }
            }
        }
    }
}

@Composable
fun MessageNodeAccordion(message: DbcMessage, isTx: Boolean) {
    var expanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CanDbTheme.CardBg)
            .border(1.dp, CanDbTheme.BorderColor, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CanDbTheme.CardBgElevated)
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (expanded) CanIcons.ArrowDown else CanIcons.ArrowRight,
                    contentDescription = null,
                    tint = CanDbTheme.AccentTeal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BadgePill(text = message.hexId, color = CanDbTheme.AccentAmber.copy(0.2f), textColor = CanDbTheme.AccentAmber, fontSize = 13)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = message.name, color = CanDbTheme.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "(DLC: ${message.dlc}B)", color = CanDbTheme.TextMuted, fontSize = 13.sp)
            }
            BadgePill(
                text = "${message.signals.size} Signals",
                color = CanDbTheme.CardBg,
                textColor = CanDbTheme.AccentTeal
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (message.signals.isEmpty()) {
                    Text("No signals mapped to this CAN message.", color = CanDbTheme.TextMuted, fontSize = 13.sp)
                } else {
                    message.signals.forEachIndexed { idx, sig ->
                        val color = SignalColorPalette.getColor(idx)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CanDbTheme.BgDark)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(sig.name, color = CanDbTheme.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Start Bit: ${sig.startBit} • Length: ${sig.length} bits • ${sig.endianness.label}", color = CanDbTheme.TextMuted, fontSize = 12.sp)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(sig.formulaDisplay, color = CanDbTheme.AccentEmerald, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text("RX: ${sig.receiversDisplay}", color = CanDbTheme.AccentCyan, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RxMessageNodeCard(message: DbcMessage, signals: List<DbcSignal>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CanDbTheme.CardBg)
            .border(1.dp, CanDbTheme.BorderColor, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CanDbTheme.CardBgElevated)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgePill(text = "TX ECU: ${message.transmitter}", color = CanDbTheme.AccentIndigo.copy(0.2f), textColor = CanDbTheme.AccentIndigo, fontSize = 12)
                Spacer(modifier = Modifier.width(12.dp))
                BadgePill(text = message.hexId, color = CanDbTheme.AccentAmber.copy(0.2f), textColor = CanDbTheme.AccentAmber, fontSize = 13)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = message.name, color = CanDbTheme.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            BadgePill(text = "${signals.size} Received Signals", color = CanDbTheme.CardBg, textColor = CanDbTheme.AccentCyan)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            signals.forEachIndexed { idx, sig ->
                val color = SignalColorPalette.getColor(idx)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CanDbTheme.BgDark)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(sig.name, color = CanDbTheme.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(sig.formulaDisplay, color = CanDbTheme.AccentEmerald, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CanDbTheme.CardBg)
            .border(1.dp, CanDbTheme.BorderColor, RoundedCornerShape(12.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = CanDbTheme.TextMuted, fontSize = 14.sp)
    }
}
