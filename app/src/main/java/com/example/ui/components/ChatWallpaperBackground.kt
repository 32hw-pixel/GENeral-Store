package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.LocalPaperMode
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PaperBg
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperSurface
import com.example.ui.theme.PaperText
import com.example.ui.theme.PaperTextMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.ChatWallpaper
import com.example.util.ChatWallpaperManager
import com.example.util.SoundPoolManager

@Composable
fun ChatWallpaperBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isPaper = LocalPaperMode.current
    val wallpaper by ChatWallpaperManager.selectedWallpaper.collectAsState()
    val customUri by ChatWallpaperManager.customPhotoUri.collectAsState()
    val dimAlpha by ChatWallpaperManager.dimAlpha.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (isPaper) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PaperBg)
            )
        } else {
            when (wallpaper) {
                ChatWallpaper.CYBER_GRID -> {
                    CyberGridWallpaper()
                }
                ChatWallpaper.SERVER_ROOM -> {
                    ServerRoomWallpaper()
                }
                ChatWallpaper.NEON_PURPLE -> {
                    NeonPurpleWallpaper()
                }
                ChatWallpaper.CARBON_TACTICAL -> {
                    CarbonTacticalWallpaper()
                }
                ChatWallpaper.EMERALD_MATRIX -> {
                    EmeraldMatrixWallpaper()
                }
                ChatWallpaper.SUNSET_HORIZON -> {
                    SunsetHorizonWallpaper()
                }
                ChatWallpaper.DEEP_SPACE_NEBULA -> {
                    DeepSpaceNebulaWallpaper()
                }
                ChatWallpaper.AMOLED_PURE_BLACK -> {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000000)))
                }
                ChatWallpaper.PAPER_INK -> {
                    Box(modifier = Modifier.fillMaxSize().background(PaperBg))
                }
                ChatWallpaper.CUSTOM_PHOTO -> {
                    if (customUri != null) {
                        AsyncImage(
                            model = customUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Dimming overlay so messages pop clearly
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = dimAlpha))
                        )
                    } else {
                        CyberGridWallpaper()
                    }
                }
            }
        }

        // Inner chat content
        content()
    }
}

@Composable
private fun CyberGridWallpaper() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        val gridSpacing = 48.dp.toPx()
        val gridColor = Color(0xFF00E5FF).copy(alpha = 0.04f)

        var x = 0f
        while (x < size.width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += gridSpacing
        }

        var y = 0f
        while (y < size.height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += gridSpacing
        }
    }
}

@Composable
private fun ServerRoomWallpaper() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050C18),
                        Color(0xFF0A1830),
                        Color(0xFF040A14)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cyanGlow = Color(0xFF00E5FF).copy(alpha = 0.06f)
            drawCircle(
                color = cyanGlow,
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.5f, size.height * 0.3f)
            )
        }
    }
}

@Composable
private fun NeonPurpleWallpaper() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF240E36),
                        Color(0xFF100518),
                        Color(0xFF07020A)
                    )
                )
            )
    )
}

@Composable
private fun CarbonTacticalWallpaper() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E10))
    ) {
        val patternColor = Color(0xFF1E1E24).copy(alpha = 0.4f)
        val step = 16.dp.toPx()
        var x = 0f
        while (x < size.width + size.height) {
            drawLine(
                color = patternColor,
                start = Offset(x, 0f),
                end = Offset(0f, x),
                strokeWidth = 1.5f
            )
            x += step
        }
    }
}

@Composable
private fun EmeraldMatrixWallpaper() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040E07))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val matrixColor = Color(0xFF00FF66).copy(alpha = 0.05f)
            val step = 32.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = matrixColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.2f
                )
                x += step
            }
        }
    }
}

@Composable
private fun SunsetHorizonWallpaper() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1F0615),
                        Color(0xFF330D14),
                        Color(0xFF1A050A),
                        Color(0xFF080205)
                    )
                )
            )
    )
}

@Composable
private fun DeepSpaceNebulaWallpaper() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A103C),
                        Color(0xFF09061A),
                        Color(0xFF03020A)
                    )
                )
            )
    )
}

@Composable
fun WallpaperSettingsDialog(
    onDismiss: () -> Unit
) {
    val isPaper = LocalPaperMode.current
    val currentWallpaper by ChatWallpaperManager.selectedWallpaper.collectAsState()
    val dimAlpha by ChatWallpaperManager.dimAlpha.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            ChatWallpaperManager.setCustomPhoto(uri)
            SoundPoolManager.playConfirm()
        }
    }

    val cardBg = if (isPaper) PaperSurface else CyberCardBg
    val textColor = if (isPaper) PaperText else Color.White
    val textMutedColor = if (isPaper) PaperTextMuted else TextSecondary
    val borderColor = if (isPaper) PaperBorder else CyberCardBorder
    val accentColor = if (isPaper) PaperText else CyberYellow

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wallpaper,
                    contentDescription = null,
                    tint = if (isPaper) PaperText else NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Sohbet Duvar Kağıdı",
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Sohbet arka planını özelleştir",
                        fontSize = 11.sp,
                        color = textMutedColor
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Siber Arka Plan Teması Seç:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                ChatWallpaper.entries.forEach { wp ->
                    val isSelected = wp == currentWallpaper
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) {
                                    if (isPaper) PaperBorder else Color(0xFF10253B)
                                } else {
                                    if (isPaper) PaperSurface else Color(0xFF161616)
                                }
                            )
                            .border(
                                1.dp,
                                if (isSelected) {
                                    if (isPaper) PaperText else NeonCyan
                                } else borderColor,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                SoundPoolManager.playMenuNav()
                                if (wp == ChatWallpaper.CUSTOM_PHOTO) {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                } else {
                                    ChatWallpaperManager.setWallpaper(wp)
                                }
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = wp.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) (if (isPaper) PaperText else NeonCyan) else textColor
                            )
                            Text(
                                text = wp.description,
                                fontSize = 10.sp,
                                color = textMutedColor
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isPaper) PaperText else NeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        } else if (wp == ChatWallpaper.CUSTOM_PHOTO) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = if (isPaper) PaperText else CyberYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dimming Slider for Custom Photo
                if (currentWallpaper == ChatWallpaper.CUSTOM_PHOTO) {
                    Text(
                        text = "Arka Plan Karartma: %${(dimAlpha * 100).toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Slider(
                        value = dimAlpha,
                        onValueChange = { ChatWallpaperManager.setDimAlpha(it) },
                        valueRange = 0.1f..0.85f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    text = "Kaydet",
                    color = if (isPaper) PaperBg else CyberBlack,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = cardBg
    )
}
