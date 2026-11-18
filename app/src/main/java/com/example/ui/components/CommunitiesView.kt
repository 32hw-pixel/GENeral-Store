package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CommunityItem
import com.example.data.ConversationItem
import com.example.data.DirectMessage
import com.example.data.defaultCommunities
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager

@Composable
fun CommunitiesView(
    onOpenCommunityChat: (ConversationItem) -> Unit,
    modifier: Modifier = Modifier,
    isPaper: Boolean
) {
    val context = LocalContext.current
    val communities = com.example.data.CommunitiesRepository.communities
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tümü") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val categories = listOf("Tümü", "Siber Güvenlik", "Yazılım", "Yapay Zekâ", "Haber", "Spor", "Yemek", "Şirket", "Oyun", "Gizlilik")

    val filteredCommunities = communities.filter { item ->
        val matchesCategory = selectedCategory == "Tümü" || item.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val cardBg = if (isPaper) Color(0xFFF7F4EE) else CyberCardBg
    val textColor = if (isPaper) Color(0xFF1E1C1A) else Color.White
    val textMutedColor = if (isPaper) Color(0xFF8A8275) else TextSecondary
    val borderColor = if (isPaper) Color(0xFFDCD5C9) else CyberYellow.copy(alpha = 0.7f)
    val accentColor = if (isPaper) Color(0xFF1E1C1A) else CyberYellow

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isPaper) Color(0xFFE2DDD5) else Color(0xFF141414)
                ),
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPaper) Color.White else Color(0xFF262000))
                                .border(1.dp, accentColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GEN TOPLULUKLARI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                modifier = Modifier.semantics { heading() }
                            )
                            Text(
                                text = "Açık ve şifreli frekanslar · ${communities.size} aktif topluluk",
                                fontSize = 11.sp,
                                color = textMutedColor
                            )
                        }
                    }

                    Button(
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            showCreateDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .semantics {
                                role = Role.Button
                                contentDescription = "Yeni Topluluk Oluştur"
                            }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = if (isPaper) Color.White else CyberBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Oluştur",
                            color = if (isPaper) Color.White else CyberBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Topluluklarda ve kanallarda ara...", color = textMutedColor, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Temizle", tint = textMutedColor, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = borderColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedContainerColor = if (isPaper) Color(0xFFECE6DA) else CyberBlack,
                    unfocusedContainerColor = if (isPaper) Color(0xFFECE6DA) else CyberBlack
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Categories Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            selectedCategory = cat
                        },
                        label = { Text(cat, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isPaper) Color(0xFFD6CEBF) else Color(0xFF262000),
                            selectedLabelColor = accentColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Community List (Adaptive 1-column on phone, 2-column grid on tablets/foldables)
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useTwoColumns = maxWidth >= 600.dp
                if (filteredCommunities.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, borderColor),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (communities.isEmpty()) "Henüz Topluluk Yok" else "Aramanızla Eşleşen Topluluk Yok",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (communities.isEmpty()) {
                                        "Hazır sahte topluluklar temizlendi. Kendi şifreli topluluğunuzu hemen oluşturabilirsiniz."
                                    } else {
                                        "Farklı bir kategori seçin veya arama kutusunu temizleyin."
                                    },
                                    fontSize = 12.sp,
                                    color = textMutedColor
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        SoundPoolManager.playMechanicalClick()
                                        if (communities.isEmpty()) {
                                            showCreateDialog = true
                                        } else {
                                            searchQuery = ""
                                            selectedCategory = "Tümü"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Text(
                                        text = if (communities.isEmpty()) "+ İlk Topluluğunu Kur" else "Filtreleri Temizle",
                                        color = if (isPaper) Color.White else CyberBlack,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else if (useTwoColumns) {
                    val rows = filteredCommunities.chunked(2)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(rows, key = { row -> row.joinToString("_") { it.id } }) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { item ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isPaper) Color.White else CyberBlack
                                            ),
                                            border = BorderStroke(1.dp, if (item.isJoined) NeonCyan.copy(alpha = 0.8f) else borderColor),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .semantics {
                                                    role = Role.Button
                                                    contentDescription = "${item.name}, ${item.memberCount} üye, ${item.category}"
                                                }
                                                .clickable {
                                                    SoundPoolManager.playMechanicalClick()
                                                    val conv = ConversationItem(
                                                        id = item.id,
                                                        name = item.name,
                                                        initials = item.emoji,
                                                        avatarEmoji = item.emoji,
                                                        logoUri = item.logoUri,
                                                        username = "@${item.category.lowercase().replace(" ", "")}",
                                                        isGroup = true,
                                                        isOnline = true,
                                                        lastMessage = item.description,
                                                        lastTime = "Aktif",
                                                        isVerified = item.isVerified,
                                                        messages = item.messages
                                                    )
                                                    onOpenCommunityChat(conv)
                                                }
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (!item.logoUri.isNullOrBlank()) {
                                                        AsyncImage(
                                                            model = item.logoUri,
                                                            contentDescription = "${item.name} logosu",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier
                                                                .size(34.dp)
                                                                .clip(CircleShape)
                                                                .border(1.dp, accentColor, CircleShape)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                    }
                                                    Text(
                                                        text = if (!item.logoUri.isNullOrBlank()) item.name else "${item.emoji} ${item.name}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = textColor
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("${item.memberCount} üye · ${item.category}", fontSize = 11.sp, color = textMutedColor)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(item.description, fontSize = 12.sp, color = textColor.copy(alpha = 0.85f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredCommunities, key = { it.id }) { item ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPaper) Color.White else CyberBlack
                                ),
                                border = BorderStroke(1.dp, if (item.isJoined) NeonCyan.copy(alpha = 0.8f) else borderColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "${item.name}, ${item.memberCount} üye, ${item.category}"
                                    }
                                    .clickable {
                                        SoundPoolManager.playMechanicalClick()
                                        val conv = ConversationItem(
                                            id = item.id,
                                            name = item.name,
                                            initials = item.emoji,
                                            avatarEmoji = item.emoji,
                                            logoUri = item.logoUri,
                                            username = "@${item.category.lowercase().replace(" ", "")}",
                                            isGroup = true,
                                            isOnline = true,
                                            lastMessage = item.description,
                                            lastTime = "Aktif",
                                            isVerified = item.isVerified,
                                            messages = item.messages
                                        )
                                        onOpenCommunityChat(conv)
                                    }
                            ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isPaper) Color(0xFFECE6DA) else Color(0xFF141414))
                                            .border(1.dp, accentColor.copy(alpha = 0.45f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!item.logoUri.isNullOrBlank()) {
                                            AsyncImage(
                                                model = item.logoUri,
                                                contentDescription = "${item.name} logosu",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            Text(item.emoji, fontSize = 22.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = textColor
                                            )
                                            if (item.isVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.Verified, contentDescription = "Doğrulanmış", tint = NeonGreen, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Text(
                                            text = "${item.memberCount} üye · ${item.category}",
                                            fontSize = 11.sp,
                                            color = textMutedColor
                                        )
                                    }
                                }

                                // Join / Joined Button
                                Button(
                                    onClick = {
                                        SoundPoolManager.playMechanicalClick()
                                        com.example.data.CommunitiesRepository.updateMembership(item.id, !item.isJoined)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (item.isJoined) NeonGreen.copy(alpha = 0.2f) else accentColor
                                    ),
                                    border = if (item.isJoined) BorderStroke(1.dp, NeonGreen) else null,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .semantics {
                                            role = Role.Button
                                            contentDescription = if (item.isJoined) "${item.name} topluluğundan ayrıl" else "${item.name} topluluğuna katıl"
                                        }
                                ) {
                                    Text(
                                        text = if (item.isJoined) "✓ Katıldın" else "+ Katıl",
                                        color = if (item.isJoined) NeonGreen else (if (isPaper) Color.White else CyberBlack),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.description,
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.9f),
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Channel tags
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                item.channelTags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isPaper) Color(0xFFECE6DA) else Color(0xFF161616))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("#$tag", fontSize = 10.sp, color = NeonCyan)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }

    // Create Community Modal
    if (showCreateDialog) {
        CreateCommunityDialog(
            onCreate = { newCommunity ->
                com.example.data.CommunitiesRepository.addCommunity(newCommunity, context)
                selectedCategory = "Tümü"
                searchQuery = ""
                SoundPoolManager.playNotification()
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
            isPaper = isPaper
        )
    }
    }
}

@Composable
fun CreateCommunityDialog(
    onCreate: (CommunityItem) -> Unit,
    onDismiss: () -> Unit,
    isPaper: Boolean
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("⚡") }
    var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategory by remember { mutableStateOf("Siber Güvenlik") }
    var tagsInput by remember { mutableStateOf("genel, sohbet") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedLogoUri = uri
            SoundPoolManager.playConfirm()
        }
    }

    val fileLogoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedLogoUri = uri
            SoundPoolManager.playConfirm()
        }
    }

    val emojis = listOf("⚡", "🛡️", "🤖", "📰", "⚽", "🍔", "🏢", "🎮", "🌐", "💬")
    val categories = listOf("Siber Güvenlik", "Yazılım", "Yapay Zekâ", "Haber", "Spor", "Yemek", "Şirket", "Oyun", "Gizlilik", "Genel")

    val cardBg = if (isPaper) Color(0xFFF7F4EE) else CyberCardBg
    val textColor = if (isPaper) Color(0xFF1E1C1A) else Color.White
    val textMutedColor = if (isPaper) Color(0xFF8A8275) else TextSecondary
    val borderColor = if (isPaper) Color(0xFFDCD5C9) else CyberYellow.copy(alpha = 0.7f)
    val accentColor = if (isPaper) Color(0xFF1E1C1A) else CyberYellow

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedLogoUri != null) {
                    AsyncImage(
                        model = selectedLogoUri,
                        contentDescription = "Seçilen topluluk logosu",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, accentColor, CircleShape)
                    )
                } else {
                    Text(selectedEmoji, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Yeni Topluluk Oluştur", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                    Text("Kendi siber frekansını ve logosunu ayarla", fontSize = 11.sp, color = textMutedColor)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Custom Community Logo Upload (Gallery / Files)
                Text("Topluluk Logosu (Galeri veya Dosyalar):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, "Galeri açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        border = BorderStroke(1.dp, accentColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                    ) {
                        Text("Galeriden Seç", fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            try {
                                com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                                fileLogoLauncher.launch(arrayOf("image/*"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Dosya seçici açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        border = BorderStroke(1.dp, NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                    ) {
                        Text("Dosyalardan Seç", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                }
                if (selectedLogoUri != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPaper) Color(0xFFECE6DA) else Color(0xFF141414))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = selectedLogoUri,
                                contentDescription = "Önizleme",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(44.dp).clip(CircleShape).border(1.dp, accentColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Özel Logo Hazır ✓", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { selectedLogoUri = null }) {
                            Text("Kaldır", color = NeonRed, fontSize = 11.sp)
                        }
                    }
                }

                // Emoji Picker (minimum 48dp accessible touch target)
                Text("Simge Seç (Logo yoksa kullanılır):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(emojis) { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .semantics {
                                    role = Role.RadioButton
                                    selected = isSelected
                                    contentDescription = "Topluluk simgesi: $emoji"
                                }
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) (if (isPaper) Color(0xFFD6CEBF) else Color(0xFF262000)) else Color.Transparent)
                                    .border(1.dp, if (isSelected) accentColor else borderColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }
                }

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Topluluk Adı", fontSize = 12.sp) },
                    placeholder = { Text("Örn: Kripto Araştırma Grubu", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    )
                )

                // Category Selector
                Text("Kategori:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isPaper) Color(0xFFD6CEBF) else Color(0xFF262000),
                                selectedLabelColor = accentColor
                            )
                        )
                    }
                }

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama", fontSize = 12.sp) },
                    placeholder = { Text("Topluluğun amacını ve kurallarını belirtin...", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    )
                )

                // Tags Input
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Kanal Etiketleri (virgülle ayırın)", fontSize = 12.sp) },
                    placeholder = { Text("Örn: genel, duyurular, kod", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isNotBlank()) {
                        val tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val newComm = CommunityItem(
                            id = "comm_${System.currentTimeMillis()}",
                            name = cleanName,
                            description = description.ifBlank { "Güvenli GENeral Chat topluluğu." },
                            emoji = selectedEmoji,
                            logoUri = selectedLogoUri?.toString(),
                            category = selectedCategory,
                            memberCount = 1,
                            isVerified = false,
                            isJoined = true,
                            createdByMe = true,
                            channelTags = if (tags.isNotEmpty()) tags else listOf("genel", "sohbet")
                        )
                        onCreate(newComm)
                    }
                },
                enabled = name.trim().isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Topluluğu Başlat", color = if (isPaper) Color.White else CyberBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = textMutedColor)
            }
        },
        containerColor = cardBg,
        shape = RoundedCornerShape(16.dp)
    )
}
