package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager
import kotlin.math.roundToInt

/**
 * Roadmap Section 9: Kayan Pencere / Çoklu Görev (Multitasking Floating Overlay)
 * Provides quick scratchpad notes, cyber calculator, or quick assistant without leaving current screen.
 */
@Composable
fun FloatingCyberWidget(
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Not, 1: Hesap Makinesi, 2: Hızlı AI
    var noteText by remember { mutableStateOf("") }

    // Mini calculator state
    var calcInput by remember { mutableStateOf("") }
    var calcResult by remember { mutableStateOf("0") }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Column(horizontalAlignment = Alignment.End) {
            // Expandable HUD
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .padding(bottom = 8.dp)
                        .testTag("floating_cyber_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberBlack.copy(alpha = 0.95f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberYellow)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "KAYAN ÇOKLU GÖREV",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CyberYellow,
                                    letterSpacing = 1.sp
                                )
                            }
                            IconButton(
                                onClick = {
                                    SoundPoolManager.playMechanicalClick()
                                    isExpanded = false
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Kapat",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tab selectors
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberCardBg)
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("Hızlı Not", "Hesapla", "Durum").forEachIndexed { index, title ->
                                val isSelected = selectedTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) CyberYellow else Color.Transparent)
                                        .clickable {
                                            SoundPoolManager.playScrollTick()
                                            selectedTab = index
                                        }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CyberBlack else Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        when (selectedTab) {
                            0 -> {
                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it },
                                    placeholder = { Text("Geçici not / şifreli taslak yaz...", color = TextMuted, fontSize = 11.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyberYellow,
                                        unfocusedBorderColor = CyberCardBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = CyberCardBg,
                                        unfocusedContainerColor = CyberCardBg
                                    ),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                                )
                            }
                            1 -> {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CyberCardBg)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = if (calcInput.isEmpty()) calcResult else calcInput,
                                            color = CyberYellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val calcButtons = listOf(
                                        listOf("7", "8", "9", "+"),
                                        listOf("4", "5", "6", "-"),
                                        listOf("1", "2", "3", "*"),
                                        listOf("C", "0", "=", "/")
                                    )
                                    calcButtons.forEach { row ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            row.forEach { btn ->
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(28.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(if (btn == "=" || btn == "C") CyberYellow else CyberCardBg)
                                                        .clickable {
                                                            SoundPoolManager.playMechanicalClick()
                                                            when (btn) {
                                                                "C" -> {
                                                                    calcInput = ""
                                                                    calcResult = "0"
                                                                }
                                                                "=" -> {
                                                                    try {
                                                                        val eval = simpleEval(calcInput)
                                                                        calcResult = eval
                                                                        calcInput = eval
                                                                    } catch (_: Exception) {
                                                                        calcResult = "Hata"
                                                                    }
                                                                }
                                                                else -> calcInput += btn
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = btn,
                                                        color = if (btn == "=" || btn == "C") CyberBlack else Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                            2 -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text("⚡ E2EE Tünel Durumu: Aktif (256-bit)", color = NeonGreen, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("🛰️ Küresel Frekans: Senkronize", color = NeonCyan, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("🔒 Maskeleme: Hazır", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Floating Trigger Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CyberYellow)
                    .border(2.dp, CyberBlack, CircleShape)
                    .clickable {
                        SoundPoolManager.playMechanicalClick()
                        isExpanded = !isExpanded
                    }
                    .testTag("floating_widget_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Layers,
                    contentDescription = "Kayan Pencere Modu",
                    tint = CyberBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun simpleEval(expr: String): String {
    if (expr.isBlank()) return "0"
    val op = when {
        expr.contains("+") -> '+'
        expr.contains("-") -> '-'
        expr.contains("*") -> '*'
        expr.contains("/") -> '/'
        else -> return expr
    }
    val parts = expr.split(op)
    if (parts.size != 2) return expr
    val left = parts[0].trim().toDoubleOrNull() ?: return expr
    val right = parts[1].trim().toDoubleOrNull() ?: return expr
    val res = when (op) {
        '+' -> left + right
        '-' -> left - right
        '*' -> left * right
        '/' -> if (right != 0.0) left / right else Double.NaN
        else -> left
    }
    return if (res.isNaN()) "Hata" else if (res % 1.0 == 0.0) res.toLong().toString() else String.format(java.util.Locale.US, "%.2f", res)
}
