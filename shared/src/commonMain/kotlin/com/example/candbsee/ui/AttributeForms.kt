package com.example.candbsee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import com.example.candbsee.model.*

@Composable
fun AttributesEditorForm(
    definitions: List<AttributeDefinition>,
    attributesMap: Map<String, String>,
    onUpdateAttribute: (String, String) -> Unit
) {
    if (definitions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Attributes", color = CanDbTheme.TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        definitions.forEach { def ->
            val value = attributesMap[def.name] ?: def.defaultValue
            
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text(def.name, color = CanDbTheme.TextSecondary, fontSize = 12.sp)
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
                                            onUpdateAttribute(def.name, option)
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
                            onValueChange = { onUpdateAttribute(def.name, it) },
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
    }
}
