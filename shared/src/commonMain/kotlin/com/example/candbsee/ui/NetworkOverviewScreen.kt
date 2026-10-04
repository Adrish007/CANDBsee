package com.example.candbsee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.candbsee.model.DbcNetwork
import com.example.candbsee.model.DbcNode

@Composable
fun NetworkOverviewScreen(
    network: DbcNetwork,
    onNavigateToNodes: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToSignals: () -> Unit,
    onSelectNode: (String) -> Unit
) {
    val totalNodes = network.nodes.size
    val totalMessages = network.messages.size
    val totalSignals = network.allSignals.size
    val totalPayloadBytes = network.messages.sumOf { it.dlc }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanDbTheme.BgDark)
            .padding(24.dp)
    ) {
        SectionHeader(
            title = "Network Overview: ${network.fileName}",
            subtitle = "DBC Version: ${network.version} • Real-time database telemetry and structure visualization"
        )

        // Top Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                title = "TOTAL ECUS",
                value = "$totalNodes",
                subtitle = "Active ECUs on CAN bus",
                icon = CanIcons.Node,
                accentColor = CanDbTheme.AccentTeal,
                modifier = Modifier.weight(1f).clickable { onNavigateToNodes() }
            )
            MetricCard(
                title = "CAN FRAMES",
                value = "$totalMessages",
                subtitle = "$totalPayloadBytes Bytes Total Payload",
                icon = CanIcons.Message,
                accentColor = CanDbTheme.AccentCyan,
                modifier = Modifier.weight(1f).clickable { onNavigateToMessages() }
            )
            MetricCard(
                title = "BUS SIGNALS",
                value = "$totalSignals",
                subtitle = "Mapped engineering data points",
                icon = CanIcons.Signal,
                accentColor = CanDbTheme.AccentEmerald,
                modifier = Modifier.weight(1f).clickable { onNavigateToSignals() }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "ECU NETWORK TOPOLOGY & TRAFFIC",
            color = CanDbTheme.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Grid of Nodes with transmitted / received stats
        LazyVerticalGrid(
            columns = GridCells.Adaptive(320.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(network.nodes) { node ->
                NodeOverviewCard(
                    node = node,
                    network = network,
                    onClick = { onSelectNode(node.name) }
                )
            }
        }
    }
}

@Composable
fun NodeOverviewCard(node: DbcNode, network: DbcNetwork, onClick: () -> Unit) {
    val txMessages = remember(network, node.name) { network.getTransmittedMessages(node.name) }
    val rxSignals = remember(network, node.name) { network.getReceivedSignals(node.name) }
    val rxMessagesCount = remember(network, node.name) { network.getReceivedMessages(node.name).size }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CanDbTheme.CardBg)
            .border(1.dp, CanDbTheme.BorderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CanDbTheme.AccentIndigo.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(CanIcons.Node, contentDescription = null, tint = CanDbTheme.AccentIndigo, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = node.name,
                        color = CanDbTheme.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = node.comment ?: "No description provided",
                        color = CanDbTheme.TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
            Icon(CanIcons.ArrowRight, contentDescription = "Select", tint = CanDbTheme.AccentTeal, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = CanDbTheme.BorderColor)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("TRANSMITS (TX)", color = CanDbTheme.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgePill(
                        text = "${txMessages.size} Frames",
                        color = CanDbTheme.AccentEmerald.copy(alpha = 0.15f),
                        textColor = CanDbTheme.AccentEmerald
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${txMessages.sumOf { it.signals.size }} Signals", color = CanDbTheme.TextSecondary, fontSize = 12.sp)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("RECEIVES (RX)", color = CanDbTheme.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgePill(
                        text = "$rxMessagesCount Frames",
                        color = CanDbTheme.AccentCyan.copy(alpha = 0.15f),
                        textColor = CanDbTheme.AccentCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${rxSignals.size} Signals", color = CanDbTheme.TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}
