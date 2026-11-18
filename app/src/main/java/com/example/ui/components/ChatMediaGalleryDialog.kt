package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.DirectMessage
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.MediaGallerySaver
import com.example.util.SoundPoolManager
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ChatMediaGalleryDialog(
    messages: List<DirectMessage>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("📷 Görseller", "📁 Belgeler & APK", "🎙️ Sesler", "🔗 Linkler")

    val images = remember(messages) {
        messages.filter { it.attachmentType == "IMAGE" || it.attachmentName?.endsWith(".jpg") == true || it.attachmentName?.endsWith(".png") == true }
    }
    val files = remember(messages) {
        messages.filter { it.attachmentType in listOf("DOCUMENT", "APK", "FILE") || (it.attachmentName != null && it.attachmentType != "IMAGE" && it.attachmentType != "LINK" && it.attachmentType != "LOCATION" && it.attachmentType != "LIVE_LOCATION" && it.attachmentType != "EVENT") }
    }
    val voiceNotes = remember(messages) {
        messages.filter { it.isVoiceNote }
    }
    val links = remember(messages) {
        messages.filter { it.attachmentType == "LINK" || it.text.contains("http://") || it.text.contains("https://") }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Şifreli Medya & Veri Galerisi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CyberBlack,
                    contentColor = CyberYellow,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyberYellow
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                SoundPoolManager.playMechanicalClick()
                                selectedTab = index
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) CyberYellow else TextSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val currentList = when (selectedTab) {
                    0 -> images
                    1 -> files
                    2 -> voiceNotes
                    else -> links
                }

                if (currentList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bu kategoride henüz şifreli öğe bulunamadı.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentList) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberBlack),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val icon = when (selectedTab) {
                                        0 -> Icons.Default.Image
                                        1 -> Icons.Default.Description
                                        2 -> Icons.Default.GraphicEq
                                        else -> Icons.Default.Link
                                    }
                                    val tint = when (selectedTab) {
                                        0 -> NeonCyan
                                        1 -> NeonGreen
                                        2 -> CyberYellow
                                        else -> Color(0xFF60A5FA)
                                    }

                                    val imageRawUrl = item.attachmentUrl ?: item.attachmentUri
                                    val localImgFile = remember(imageRawUrl) {
                                        imageRawUrl?.takeIf { !it.startsWith("http") && !it.startsWith("content://") }?.let { File(it) }?.takeIf { it.exists() }
                                    }

                                    if (selectedTab == 0 && (localImgFile != null || !imageRawUrl.isNullOrBlank())) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(localImgFile ?: imageRawUrl)
                                                .size(coil.size.Size.ORIGINAL)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = item.attachmentName ?: "Görsel",
                                            contentScale = ContentScale.Crop,
                                            filterQuality = FilterQuality.High,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(tint.copy(alpha = 0.15f))
                                                .border(1.dp, tint, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.attachmentName ?: if (item.isVoiceNote) "Sesli Mesaj (${item.voiceDurationSeconds}s)" else item.text,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${item.senderName} · ${item.time}",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }

                                    if (selectedTab == 0) {
                                        IconButton(
                                            onClick = {
                                                SoundPoolManager.playMechanicalClick()
                                                scope.launch {
                                                    val res = MediaGallerySaver.saveMessageImageToGallery(
                                                        context = context,
                                                        message = item,
                                                        senderUid = item.senderId
                                                    )
                                                    res.onSuccess {
                                                        Toast.makeText(context, "Fotoğraf galeriye kaydedildi!", Toast.LENGTH_SHORT).show()
                                                    }.onFailure { err ->
                                                        Toast.makeText(context, "Kaydedilemedi: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = "Galeriye Kaydet",
                                                tint = NeonGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = CyberYellow, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CyberCardBg,
        shape = RoundedCornerShape(16.dp)
    )
}
