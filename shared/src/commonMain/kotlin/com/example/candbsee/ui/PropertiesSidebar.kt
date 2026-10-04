package com.example.candbsee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.candbsee.model.*

@Composable
fun PropertiesSidebar(
    network: DbcNetwork,
    currentTab: NavigationTab,
    selectedNodeName: String?,
    selectedMessageId: Long?,
    onUpdateNodeAttribute: (String, String, String) -> Unit,
    onUpdateMessageAttribute: (Long, String, String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val sidebarWidth by androidx.compose.animation.core.animateDpAsState(targetValue = if (isExpanded) 300.dp else 0.dp)
    
    val contextInfo = when (currentTab) {
        NavigationTab.NODES -> {
            val node = network.nodes.find { it.name == selectedNodeName }
            if (node != null) {
                Triple(node.name + " Properties", node.attributes, AttributeObjectType.NODE) to { k: String, v: String -> onUpdateNodeAttribute(node.name, k, v) }
            } else null
        }
        NavigationTab.MESSAGES -> {
            val msg = network.messages.find { it.id == selectedMessageId }
            if (msg != null) {
                Triple(msg.name + " Properties", msg.attributes, AttributeObjectType.MESSAGE) to { k: String, v: String -> onUpdateMessageAttribute(msg.id, k, v) }
            } else null
        }
        else -> null
    }

    val title = contextInfo?.first?.first
    val attributesMap = contextInfo?.first?.second
    val defType = contextInfo?.first?.third
    val onUpdate = contextInfo?.second

    LaunchedEffect(title) {
        if (title != null && !isExpanded) {
            isExpanded = true
        } else if (title == null) {
            isExpanded = false
        }
    }

    if (sidebarWidth > 0.dp) {
        Column(
            modifier = Modifier
                .width(sidebarWidth)
                .fillMaxHeight()
                .background(CanDbTheme.BgSidebar)
                .border(width = 1.dp, color = CanDbTheme.BorderColor)
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title ?: "Properties",
                    color = CanDbTheme.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = { isExpanded = false }) {
                    Icon(imageVector = CanIcons.Close, contentDescription = "Close", tint = CanDbTheme.TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
            
            HorizontalDivider(color = CanDbTheme.BorderColor, modifier = Modifier.padding(vertical = 12.dp))
            
            if (title != null && attributesMap != null && defType != null && onUpdate != null) {
                val relevantDefs = network.attributeDefinitions.values.filter { it.objectType == defType }
                
                if (relevantDefs.isEmpty()) {
                    Text("No definitions available.", color = CanDbTheme.TextSecondary, fontSize = 14.sp)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(relevantDefs) { def ->
                            val currentValue = attributesMap[def.name] ?: def.defaultValue
                            AttributeEditor(
                                def = def,
                                value = currentValue,
                                onValueChange = { newValue -> onUpdate(def.name, newValue) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttributeEditor(def: AttributeDefinition, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(def.name, color = CanDbTheme.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        
        when (def.valueType) {
            AttributeValueType.ENUM -> {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(value.ifBlank { "Select..." }, color = CanDbTheme.TextPrimary, fontSize = 13.sp)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(CanDbTheme.CardBgHover)
                    ) {
                        def.enumValues.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = CanDbTheme.TextPrimary, fontSize = 13.sp) },
                                onClick = {
                                    onValueChange(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            else -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CanDbTheme.TextPrimary,
                        unfocusedTextColor = CanDbTheme.TextPrimary
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
            }
        }
    }
}
