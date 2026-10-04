package com.example.candbsee.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NavigationTab(val title: String, val icon: ImageVector, val badge: String = "") {
    OVERVIEW("Overview", CanIcons.Overview),
    NODES("ECUs", CanIcons.Node),
    MESSAGES("Messages", CanIcons.Message),
    SIGNALS("Signals", CanIcons.Signal),
    RAW("Raw DBC Editor", CanIcons.Raw)
}

@Composable
fun NavSidebar(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    selectedTab: NavigationTab,
    nodeCount: Int,
    msgCount: Int,
    sigCount: Int,
    onSelectTab: (NavigationTab) -> Unit,
    onOpenFile: () -> Unit,
    onSaveFile: () -> Unit,
    onNewNode: () -> Unit,
    onNewMessage: () -> Unit
) {
    val sidebarWidth by androidx.compose.animation.core.animateDpAsState(targetValue = if (isExpanded) 260.dp else 72.dp)

    Column(
        modifier = Modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .background(CanDbTheme.BgSidebar)
            .border(width = 1.dp, color = CanDbTheme.BorderColor, shape = RoundedCornerShape(0.dp))
            .padding(vertical = 16.dp, horizontal = if (isExpanded) 16.dp else 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Brand & Title + Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = if (isExpanded) Arrangement.SpaceBetween else Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isExpanded) {
                    Column {
                        Text(
                            text = "DBsee",
                            color = CanDbTheme.TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "CAN DBC Editor",
                            color = CanDbTheme.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) CanIcons.ArrowLeft else CanIcons.Menu,
                        contentDescription = "Toggle Sidebar",
                        tint = CanDbTheme.TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (isExpanded) {
                Text(
                    text = "NAVIGATION",
                    color = CanDbTheme.TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            NavigationTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val countBadge = when (tab) {
                    NavigationTab.NODES -> nodeCount.toString()
                    NavigationTab.MESSAGES -> msgCount.toString()
                    NavigationTab.SIGNALS -> sigCount.toString()
                    else -> ""
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CanDbTheme.CardBgElevated else Color.Transparent)
                        .clickable { onSelectTab(tab) }
                        .padding(horizontal = if (isExpanded) 12.dp else 0.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isExpanded) Arrangement.SpaceBetween else Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) CanDbTheme.AccentTeal else CanDbTheme.TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        if (isExpanded) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = tab.title,
                                color = if (isSelected) CanDbTheme.AccentTeal else CanDbTheme.TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                    if (isExpanded && countBadge.isNotEmpty()) {
                        BadgePill(
                            text = countBadge,
                            color = if (isSelected) CanDbTheme.AccentTeal else CanDbTheme.CardBgHover,
                            textColor = if (isSelected) Color.Black else CanDbTheme.TextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isExpanded) {
                Text(
                    text = "ADD",
                    color = CanDbTheme.TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                OutlinedActionButton(icon = CanIcons.Add, title = "New ECU", onClick = onNewNode)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedActionButton(icon = CanIcons.Add, title = "New CAN Frame", onClick = onNewMessage)
            } else {
                IconButton(onClick = onNewNode, modifier = Modifier.fillMaxWidth()) {
                    Icon(CanIcons.Add, contentDescription = "New ECU", tint = CanDbTheme.AccentTeal)
                }
                IconButton(onClick = onNewMessage, modifier = Modifier.fillMaxWidth()) {
                    Icon(CanIcons.Add, contentDescription = "New CAN Frame", tint = CanDbTheme.AccentCyan)
                }
            }
        }

        Column {
            Divider(color = CanDbTheme.BorderColor, modifier = Modifier.padding(vertical = 12.dp))
            
            if (isExpanded) {
                Button(
                    onClick = onOpenFile,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CanDbTheme.AccentTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(CanIcons.Folder, contentDescription = "Open", modifier = Modifier.size(18.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open DBC File", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSaveFile,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CanDbTheme.TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CanDbTheme.BorderColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(CanIcons.Save, contentDescription = "Save", modifier = Modifier.size(18.dp), tint = CanDbTheme.TextPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export / Save DBC", fontWeight = FontWeight.Medium)
                }
            } else {
                IconButton(
                    onClick = onOpenFile,
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CanDbTheme.AccentTeal)
                ) {
                    Icon(CanIcons.Folder, contentDescription = "Open", tint = Color.Black, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = onSaveFile,
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, CanDbTheme.BorderColor, RoundedCornerShape(8.dp))
                ) {
                    Icon(CanIcons.Save, contentDescription = "Save", tint = CanDbTheme.TextPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
@Composable
fun OutlinedActionButton(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, CanDbTheme.BorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = CanDbTheme.AccentTeal, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, color = CanDbTheme.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CanDbTheme.CardBg)
            .border(width = 1.dp, color = CanDbTheme.BorderColor, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = title, color = CanDbTheme.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, color = CanDbTheme.TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = subtitle, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BadgePill(
    text: String,
    color: Color = CanDbTheme.CardBgHover,
    textColor: Color = CanDbTheme.TextPrimary,
    fontSize: Int = 11
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CanDbTheme.CardBg)
            .border(1.dp, CanDbTheme.BorderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(CanIcons.Search, contentDescription = "Search", tint = CanDbTheme.TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = CanDbTheme.TextPrimary, fontSize = 14.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(CanDbTheme.AccentTeal),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(placeholder, color = CanDbTheme.TextMuted, fontSize = 14.sp)
                    }
                    innerTextField()
                }
            )
            if (value.isNotEmpty()) {
                Icon(
                    imageVector = CanIcons.Close,
                    contentDescription = "Clear",
                    tint = CanDbTheme.TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onValueChange("") }
                        .padding(2.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String, action: @Composable (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(text = title, color = CanDbTheme.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = CanDbTheme.TextSecondary, fontSize = 13.sp)
        }
        if (action != null) {
            action()
        }
    }
}
