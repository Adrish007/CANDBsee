package com.example.candbsee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.candbsee.model.DbcNetwork
import com.example.candbsee.parser.DbcParser

@Composable
fun RawEditorScreen(
    network: DbcNetwork,
    onApplyNewText: (String) -> Unit
) {
    val initialText = remember(network) { DbcParser.serialize(network) }
    var editableText by remember(initialText) { mutableStateOf(initialText) }
    val isModified = editableText != initialText

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanDbTheme.BgDark)
            .padding(24.dp)
    ) {
        SectionHeader(
            title = "Raw DBC Editor",
            subtitle = "Live DBC specification text. You may edit raw syntax directly below."
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isModified) {
                    OutlinedButton(
                        onClick = { editableText = initialText },
                        border = androidx.compose.foundation.BorderStroke(1.dp, CanDbTheme.BorderLight)
                    ) {
                        Text("Discard Changes", color = CanDbTheme.TextSecondary)
                    }
                }
                Button(
                    onClick = { onApplyNewText(editableText) },
                    enabled = isModified,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CanDbTheme.AccentTeal,
                        disabledContainerColor = CanDbTheme.CardBgHover
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Sync",
                        color = if (isModified) Color.Black else CanDbTheme.TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(CanDbTheme.CardBg)
                .border(1.dp, if (isModified) CanDbTheme.AccentTeal else CanDbTheme.BorderColor, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            BasicTextField(
                value = editableText,
                onValueChange = { editableText = it },
                textStyle = TextStyle(
                    color = CanDbTheme.TextPrimary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(CanDbTheme.AccentTeal),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
