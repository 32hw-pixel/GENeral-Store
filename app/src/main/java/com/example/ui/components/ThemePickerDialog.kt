package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AuthViewModel
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonGreen
import com.example.util.SoundPoolManager

@Composable
fun ThemePickerDialog(
    authViewModel: AuthViewModel? = null,
    onSelectTheme: ((AppThemeMode) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentTheme = authViewModel?.appThemeMode?.collectAsState()?.value ?: AppThemeMode.CYBER_DARK

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = CyberYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Ekran & Renk Frekansı",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Göz konforuna göre anında uyarla",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppThemeMode.entries.forEach { mode ->
                    val isSelected = mode == currentTheme
                    val previewAccent = when (mode) {
                        AppThemeMode.CYBER_DARK -> CyberYellow
                        AppThemeMode.LIGHT_MODERN -> Color(0xFF3B82F6)
                        AppThemeMode.WARM_SEPIA -> Color(0xFFF59E0B)
                        AppThemeMode.COOL_OLED -> Color(0xFF00E5FF)
                        AppThemeMode.ULTRA_PAPER -> Color(0xFF1E293B)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                SoundPoolManager.playMechanicalClick()
                                if (authViewModel != null) {
                                    authViewModel.setAppTheme(mode, context)
                                } else {
                                    onSelectTheme?.invoke(mode)
                                }
                                onDismiss()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1E293B) else CyberBlack
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) previewAccent else CyberCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Emoji / Mode icon in pill
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(previewAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = mode.icon, fontSize = 18.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.title,
                                    color = if (isSelected) previewAccent else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mode.description,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }

                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(previewAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seçildi",
                                        tint = CyberBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = Color(0xFF94A3B8))
            }
        },
        containerColor = CyberCardBg,
        shape = RoundedCornerShape(18.dp)
    )
}
