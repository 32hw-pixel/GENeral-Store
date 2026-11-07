package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonRed
import com.example.ui.theme.PaperBg
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperSurface
import com.example.ui.theme.PaperText
import com.example.ui.theme.PaperTextMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniGamesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGameIndex by remember { mutableIntStateOf(0) }
    val gameTitles = listOf("Metro Koşucusu (Subway)", "Uçan Kuş (Flappy)", "XOX (Tic Tac Toe)")
    val isPaper = com.example.ui.theme.LocalPaperMode.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Games,
                            contentDescription = null,
                            tint = if (isPaper) PaperText else CyberYellow,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Retro & Klasik Mini Oyunlar",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isPaper) PaperText else Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            SoundPoolManager.playMenuNav()
                            onBack()
                        },
                        modifier = Modifier.testTag("mini_games_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = if (isPaper) PaperText else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isPaper) PaperSurface else CyberBlack,
                    titleContentColor = if (isPaper) PaperText else Color.White
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isPaper) PaperBg else CyberBlack)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedGameIndex,
                containerColor = if (isPaper) PaperSurface else CyberCardBg,
                contentColor = if (isPaper) PaperText else CyberYellow,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedGameIndex]),
                        color = if (isPaper) PaperText else CyberYellow,
                        height = 3.dp
                    )
                }
            ) {
                gameTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedGameIndex == index,
                        onClick = {
                            if (selectedGameIndex != index) {
                                SoundPoolManager.playMenuNav()
                                selectedGameIndex = index
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedGameIndex == index) FontWeight.Black else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (selectedGameIndex == index) {
                                    if (isPaper) PaperText else CyberYellow
                                } else {
                                    if (isPaper) PaperTextMuted else TextSecondary
                                }
                            )
                        }
                    )
                }
            }

            AnimatedContent(
                targetState = selectedGameIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "MiniGameTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetIndex ->
                when (targetIndex) {
                    0 -> SubwaySurferReplicaGame(isPaper = isPaper)
                    1 -> CyberBirdClassicGame(isPaper = isPaper)
                    2 -> CyberXoxGame(isPaper = isPaper)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 1. SUBWAY SURFERS REPLICA (Raylar, Trenler, Zıplama, Eğilme, Altın Paralar)
// -------------------------------------------------------------------------------------

enum class SubwayObstacleType {
    TRAIN,        // Büyük tren vagonu (kaçmak için şerit değiştirilmeli)
    LOW_BARRIER,  // Alçak bariyer (ZIPLAYARAK geçilebilir)
    HIGH_BARRIER, // Yüksek bariyer (EĞİLEREK/KAYARAK geçilebilir)
    COIN          // Toplanabilir altın paralar (+10 puan)
}

private data class SubwayItem(
    val id: Long,
    val lane: Int, // 0: Sol, 1: Orta, 2: Sağ
    var y: Float,  // 0.0f (ekranın tepesi) -> 1.0f (ekranın altı)
    val type: SubwayObstacleType
)

@Composable
fun SubwaySurferReplicaGame(isPaper: Boolean) {
    var playerLane by remember { mutableIntStateOf(1) } // 0: Sol ray, 1: Orta ray, 2: Sağ ray
    var isJumping by remember { mutableStateOf(false) }
    var isSliding by remember { mutableStateOf(false) }

    var score by remember { mutableIntStateOf(0) }
    var coinsCollected by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var highScore by remember { mutableIntStateOf(0) }
    var speed by remember { mutableFloatStateOf(0.018f) }
    var trackScrollOffset by remember { mutableFloatStateOf(0f) }

    val items = remember { mutableStateListOf<SubwayItem>() }
    var lastSpawnTime by remember { mutableStateOf(0L) }

    fun jump() {
        if (!isRunning || isGameOver || isJumping) return
        isJumping = true
        isSliding = false
        SoundPoolManager.playWhoosh()
    }

    fun slide() {
        if (!isRunning || isGameOver || isSliding) return
        isSliding = true
        isJumping = false
        SoundPoolManager.playCardFlip()
    }

    fun switchLeft() {
        if (!isRunning || isGameOver) return
        if (playerLane > 0) {
            playerLane -= 1
            SoundPoolManager.playMechanicalClick()
        }
    }

    fun switchRight() {
        if (!isRunning || isGameOver) return
        if (playerLane < 2) {
            playerLane += 1
            SoundPoolManager.playMechanicalClick()
        }
    }

    fun resetGame() {
        playerLane = 1
        isJumping = false
        isSliding = false
        score = 0
        coinsCollected = 0
        speed = 0.018f
        items.clear()
        isGameOver = false
        isRunning = true
        SoundPoolManager.playConfirm()
    }

    // Jump & Slide timer coroutine
    LaunchedEffect(isJumping) {
        if (isJumping) {
            delay(550)
            isJumping = false
        }
    }
    LaunchedEffect(isSliding) {
        if (isSliding) {
            delay(550)
            isSliding = false
        }
    }

    // Main Subway Game Loop
    LaunchedEffect(isRunning, isGameOver) {
        if (!isRunning || isGameOver) return@LaunchedEffect

        while (isRunning && !isGameOver) {
            delay(28) // ~35 FPS
            score += 1
            trackScrollOffset = (trackScrollOffset + speed * 120f) % 60f

            // Dynamic difficulty
            if (score % 300 == 0) {
                speed = (speed + 0.002f).coerceAtMost(0.040f)
            }

            // Spawn railway items
            val now = System.currentTimeMillis()
            val spawnInterval = (1100 - (speed * 10000)).toLong().coerceAtLeast(550L)
            if (now - lastSpawnTime > spawnInterval) {
                lastSpawnTime = now
                val lane = Random.nextInt(3)
                val rand = Random.nextFloat()
                val type = when {
                    rand < 0.35f -> SubwayObstacleType.COIN
                    rand < 0.60f -> SubwayObstacleType.LOW_BARRIER
                    rand < 0.82f -> SubwayObstacleType.HIGH_BARRIER
                    else -> SubwayObstacleType.TRAIN
                }
                items.add(SubwayItem(now, lane, -0.15f, type))

                // Sometimes spawn coins in other lanes alongside
                if (type != SubwayObstacleType.COIN && Random.nextFloat() < 0.4f) {
                    val otherLane = (lane + 1) % 3
                    items.add(SubwayItem(now + 1, otherLane, -0.15f, SubwayObstacleType.COIN))
                }
            }

            // Move items and check collisions
            val toRemove = mutableListOf<SubwayItem>()
            val playerY = 0.80f

            for (item in items) {
                item.y += speed

                // Collision detection zone
                val itemHeight = when (item.type) {
                    SubwayObstacleType.TRAIN -> 0.18f
                    SubwayObstacleType.LOW_BARRIER -> 0.06f
                    SubwayObstacleType.HIGH_BARRIER -> 0.06f
                    SubwayObstacleType.COIN -> 0.05f
                }

                if (item.y in (playerY - itemHeight)..(playerY + 0.04f) && item.lane == playerLane) {
                    when (item.type) {
                        SubwayObstacleType.COIN -> {
                            coinsCollected += 1
                            score += 10
                            SoundPoolManager.playConfirm()
                            toRemove.add(item)
                        }
                        SubwayObstacleType.LOW_BARRIER -> {
                            // Can jump over it!
                            if (!isJumping) {
                                isGameOver = true
                                isRunning = false
                                SoundPoolManager.playWhoosh()
                                if (score > highScore) highScore = score
                                break
                            }
                        }
                        SubwayObstacleType.HIGH_BARRIER -> {
                            // Can slide under it!
                            if (!isSliding) {
                                isGameOver = true
                                isRunning = false
                                SoundPoolManager.playWhoosh()
                                if (score > highScore) highScore = score
                                break
                            }
                        }
                        SubwayObstacleType.TRAIN -> {
                            // Cannot jump or slide under a train!
                            isGameOver = true
                            isRunning = false
                            SoundPoolManager.playWhoosh()
                            if (score > highScore) highScore = score
                            break
                        }
                    }
                } else if (item.y > 1.15f) {
                    toRemove.add(item)
                }
            }
            items.removeAll(toRemove)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Subway Dashboard
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MESAFE: $score m",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = if (isPaper) PaperText else CyberYellow,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "REKOR: $highScore m",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isPaper) PaperTextMuted else TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isPaper) PaperText else CyberYellow)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ALTIN: $coinsCollected",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = if (isPaper) PaperText else Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Railway Canvas (Subway Tracks)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isPaper) PaperSurface else Color(0xFF262834))
                .border(2.dp, if (isGameOver) NeonRed else if (isPaper) PaperBorder else Color(0xFF4A4D5E), RoundedCornerShape(16.dp))
                .pointerInput(isRunning) {
                    detectDragGestures { _, dragAmount ->
                        if (isRunning && !isGameOver) {
                            val dx = dragAmount.x
                            val dy = dragAmount.y
                            if (abs(dx) > abs(dy)) {
                                if (dx > 30) switchRight()
                                else if (dx < -30) switchLeft()
                            } else {
                                if (dy < -30) jump()
                                else if (dy > 30) slide()
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val trackWidth = w / 3f

                // Draw Railway Ties (ahşap traversler scrolling down)
                val tieStep = 45f
                var tieY = (trackScrollOffset % tieStep)
                while (tieY < h) {
                    drawRect(
                        color = if (isPaper) PaperBorder else Color(0xFF1E2838).copy(alpha = 0.5f),
                        topLeft = Offset(0f, tieY),
                        size = Size(w, 8f)
                    )
                    tieY += tieStep
                }

                // Draw Rails (Paralel Çelik Raylar - Her şeritte 2 ray)
                for (lane in 0 until 3) {
                    val leftRail = lane * trackWidth + trackWidth * 0.15f
                    val rightRail = lane * trackWidth + trackWidth * 0.85f

                    drawLine(
                        color = if (isPaper) PaperBorder else Color(0xFF556677),
                        start = Offset(leftRail, 0f),
                        end = Offset(leftRail, h),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = if (isPaper) PaperBorder else Color(0xFF556677),
                        start = Offset(rightRail, 0f),
                        end = Offset(rightRail, h),
                        strokeWidth = 4f
                    )
                }

                // Draw Subway Items (Trenler, Bariyerler, Altınlar)
                for (item in items) {
                    val cx = (item.lane + 0.5f) * trackWidth
                    val cy = item.y * h

                    when (item.type) {
                        SubwayObstacleType.COIN -> {
                            // Spinning Gold Coin
                            drawCircle(
                                color = if (isPaper) PaperText else CyberYellow,
                                radius = trackWidth * 0.16f,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = if (isPaper) PaperBg else Color(0xFF141414),
                                radius = trackWidth * 0.08f,
                                center = Offset(cx, cy)
                            )
                        }

                        SubwayObstacleType.TRAIN -> {
                            // Büyük Metro Tren Vagonu
                            val tw = trackWidth * 0.76f
                            val th = h * 0.17f
                            // Vagon gövdesi
                            drawRoundRect(
                                color = if (isPaper) PaperTextMuted else Color(0xFFB71C1C),
                                topLeft = Offset(cx - tw / 2, cy - th / 2),
                                size = Size(tw, th),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                            // Tren ön camı
                            drawRoundRect(
                                color = if (isPaper) PaperSurface else Color(0xFF0D47A1),
                                topLeft = Offset(cx - tw * 0.4f, cy + th * 0.15f),
                                size = Size(tw * 0.8f, th * 0.25f),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            // Tren farları
                            drawCircle(
                                color = CyberYellow,
                                radius = tw * 0.10f,
                                center = Offset(cx - tw * 0.28f, cy + th * 0.42f)
                            )
                            drawCircle(
                                color = CyberYellow,
                                radius = tw * 0.10f,
                                center = Offset(cx + tw * 0.28f, cy + th * 0.42f)
                            )
                        }

                        SubwayObstacleType.LOW_BARRIER -> {
                            // Alçak Zıplama Bariyeri (Kırmızı - Beyaz Çizgili)
                            val bw = trackWidth * 0.72f
                            val bh = h * 0.045f
                            drawRoundRect(
                                color = if (isPaper) PaperText else NeonRed,
                                topLeft = Offset(cx - bw / 2, cy - bh / 2),
                                size = Size(bw, bh),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            // Zıpla uyarısı simgesi
                            drawCircle(
                                color = Color.White,
                                radius = bh * 0.35f,
                                center = Offset(cx, cy)
                            )
                        }

                        SubwayObstacleType.HIGH_BARRIER -> {
                            // Yüksek Eğilme Bariyeri (Sarı - Siyah Çizgili Üst Geçit)
                            val bw = trackWidth * 0.82f
                            val bh = h * 0.04f
                            drawRoundRect(
                                color = if (isPaper) PaperText else CyberYellow,
                                topLeft = Offset(cx - bw / 2, cy - bh / 2),
                                size = Size(bw, bh),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            // İki yan direk
                            drawRect(
                                color = if (isPaper) PaperText else Color.Gray,
                                topLeft = Offset(cx - bw / 2, cy - bh / 2),
                                size = Size(6f, h * 0.07f)
                            )
                            drawRect(
                                color = if (isPaper) PaperText else Color.Gray,
                                topLeft = Offset(cx + bw / 2 - 6f, cy - bh / 2),
                                size = Size(6f, h * 0.07f)
                            )
                        }
                    }
                }

                // Draw Subway Surfer Player Character (Ray Üzerinde Koşucu)
                val px = (playerLane + 0.5f) * trackWidth
                val baseY = 0.80f * h
                val jumpOffsetY = if (isJumping) -h * 0.14f else 0f
                val py = baseY + jumpOffsetY

                val pw = trackWidth * 0.50f
                val ph = if (isSliding) h * 0.038f else h * 0.075f

                // Player Shadow on Ground
                drawOval(
                    color = Color.Black.copy(alpha = if (isJumping) 0.25f else 0.55f),
                    topLeft = Offset(px - pw * 0.35f, baseY + h * 0.03f),
                    size = Size(pw * 0.7f, h * 0.02f)
                )

                // Hoverboard / Skateboard on ground if running
                drawRoundRect(
                    color = if (isPaper) PaperText else CyberYellow,
                    topLeft = Offset(px - pw * 0.38f, py + ph / 2 - 4f),
                    size = Size(pw * 0.76f, 8f),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Surfer Body (Kapüşonlu Koşucu)
                drawRoundRect(
                    color = if (isJumping) NeonCyan else if (isSliding) NeonGreen else if (isPaper) PaperText else CyberYellow,
                    topLeft = Offset(px - pw / 2, py - ph / 2),
                    size = Size(pw, ph),
                    cornerRadius = CornerRadius(14f, 14f)
                )

                // Surfer Hat / Head
                drawCircle(
                    color = if (isPaper) PaperSurface else Color(0xFF141414),
                    radius = pw * 0.22f,
                    center = Offset(px, py - ph * 0.25f)
                )
                // Cap visor
                drawOval(
                    color = NeonRed,
                    topLeft = Offset(px - pw * 0.25f, py - ph * 0.42f),
                    size = Size(pw * 0.5f, 6f)
                )
            }

            // Start / Crash Overlay
            if (!isRunning) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isPaper) PaperBg.copy(alpha = 0.9f) else CyberBlack.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = if (isGameOver) "ÇARPTIN!" else "METRO KOŞUCUSU",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = if (isGameOver) NeonRed else if (isPaper) PaperText else CyberYellow,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isGameOver) "Mesafe: $score m | Toplanan Altın: $coinsCollected" else "Subway Surfers tarzı 3 rayda sonsuz koşu!\nTrenlerden kaç, alçak engellerden ZIPLA, tabelaların altından KAY!",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = if (isPaper) PaperTextMuted else TextSecondary
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperText else CyberYellow),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isGameOver) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isPaper) PaperBg else CyberBlack
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isGameOver) "YENİDEN KOŞ" else "BAŞLA",
                                fontWeight = FontWeight.Black,
                                color = if (isPaper) PaperBg else CyberBlack
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tactical 4-Button Controls for Subway Surfers Gameplay
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { switchLeft() },
                enabled = isRunning && !isGameOver && playerLane > 0,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder)
            ) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Sol", tint = if (isPaper) PaperText else CyberYellow)
            }

            Button(
                onClick = { jump() },
                enabled = isRunning && !isGameOver && !isJumping,
                modifier = Modifier.weight(1.2f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperBorder else Color(0xFF1E3A5F))
            ) {
                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Zıpla", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("ZIPLA", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
            }

            Button(
                onClick = { slide() },
                enabled = isRunning && !isGameOver && !isSliding,
                modifier = Modifier.weight(1.2f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperBorder else Color(0xFF332000))
            ) {
                Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Kay", tint = CyberYellow)
                Spacer(modifier = Modifier.width(4.dp))
                Text("EĞİL", fontWeight = FontWeight.Black, fontSize = 11.sp, color = CyberYellow)
            }

            Button(
                onClick = { switchRight() },
                enabled = isRunning && !isGameOver && playerLane < 2,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder)
            ) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Sağ", tint = if (isPaper) PaperText else CyberYellow)
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 2. FLAPPY BIRD CLASSIC REPLICA (Özünden kopmayan borular, kanat çırpış, yerçekimi)
// -------------------------------------------------------------------------------------

private data class ClassicPipe(
    val id: Long,
    var x: Float, // 1.15f -> -0.2f
    val gapY: Float,
    var passed: Boolean = false
)

@Composable
fun CyberBirdClassicGame(isPaper: Boolean) {
    var birdY by remember { mutableFloatStateOf(0.5f) }
    var velocity by remember { mutableFloatStateOf(0f) }
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var wingUp by remember { mutableStateOf(false) }

    val pipes = remember { mutableStateListOf<ClassicPipe>() }
    var lastPipeSpawnTime by remember { mutableStateOf(0L) }

    fun flap() {
        if (!isRunning) {
            isRunning = true
            isGameOver = false
            score = 0
            birdY = 0.5f
            velocity = -0.016f
            pipes.clear()
            SoundPoolManager.playConfirm()
            return
        }
        if (isGameOver) {
            isGameOver = false
            isRunning = true
            score = 0
            birdY = 0.5f
            velocity = -0.016f
            pipes.clear()
            SoundPoolManager.playConfirm()
            return
        }
        velocity = -0.017f
        wingUp = !wingUp
        SoundPoolManager.playMechanicalClick()
    }

    LaunchedEffect(isRunning, isGameOver) {
        if (!isRunning || isGameOver) return@LaunchedEffect

        val gravity = 0.00115f
        while (isRunning && !isGameOver) {
            delay(24)
            velocity += gravity
            birdY += velocity

            if (birdY <= 0.02f || birdY >= 0.98f) {
                isGameOver = true
                isRunning = false
                SoundPoolManager.playWhoosh()
                if (score > highScore) highScore = score
                break
            }

            val now = System.currentTimeMillis()
            if (now - lastPipeSpawnTime > 1600L) {
                lastPipeSpawnTime = now
                val gapCenter = Random.nextFloat() * 0.44f + 0.28f
                pipes.add(ClassicPipe(now, 1.15f, gapCenter))
            }

            val birdX = 0.25f
            val birdRadius = 0.038f
            val pipeWidth = 0.15f
            val gapHeight = 0.32f

            val toRemove = mutableListOf<ClassicPipe>()
            for (pipe in pipes) {
                pipe.x -= 0.009f

                if (!pipe.passed && pipe.x < birdX) {
                    pipe.passed = true
                    score += 1
                    SoundPoolManager.playConfirm()
                }

                if (abs(pipe.x - birdX) < (pipeWidth / 2 + birdRadius)) {
                    val topEdge = pipe.gapY - gapHeight / 2
                    val bottomEdge = pipe.gapY + gapHeight / 2
                    if (birdY - birdRadius < topEdge || birdY + birdRadius > bottomEdge) {
                        isGameOver = true
                        isRunning = false
                        SoundPoolManager.playWhoosh()
                        if (score > highScore) highScore = score
                        break
                    }
                }

                if (pipe.x < -0.25f) toRemove.add(pipe)
            }
            pipes.removeAll(toRemove)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SKOR: $score",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = if (isPaper) PaperText else NeonCyan,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "EN YÜKSEK: $highScore",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isPaper) PaperTextMuted else TextSecondary,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isPaper) PaperSurface else Color(0xFF70C5CE))
                .border(2.dp, if (isGameOver) NeonRed else if (isPaper) PaperBorder else Color(0xFF5A9CA4), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures { flap() }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val pipeWidthPx = w * 0.15f
                val gapHeightPx = h * 0.32f

                // Draw Classic Flappy Pipes (Yeşil retro borular)
                for (pipe in pipes) {
                    val gx = pipe.x * w
                    val gapCenterPx = pipe.gapY * h
                    val topPipeH = gapCenterPx - gapHeightPx / 2
                    val bottomPipeY = gapCenterPx + gapHeightPx / 2
                    val bottomPipeH = h - bottomPipeY

                    // Top pipe body
                    drawRoundRect(
                        color = if (isPaper) PaperTextMuted else Color(0xFF2E7D32),
                        topLeft = Offset(gx - pipeWidthPx / 2, 0f),
                        size = Size(pipeWidthPx, topPipeH),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // Top pipe rim / lip
                    drawRoundRect(
                        color = if (isPaper) PaperText else Color(0xFF4CAF50),
                        topLeft = Offset(gx - pipeWidthPx * 0.58f, topPipeH - 18f),
                        size = Size(pipeWidthPx * 1.16f, 18f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )

                    // Bottom pipe body
                    drawRoundRect(
                        color = if (isPaper) PaperTextMuted else Color(0xFF2E7D32),
                        topLeft = Offset(gx - pipeWidthPx / 2, bottomPipeY),
                        size = Size(pipeWidthPx, bottomPipeH),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // Bottom pipe rim / lip
                    drawRoundRect(
                        color = if (isPaper) PaperText else Color(0xFF4CAF50),
                        topLeft = Offset(gx - pipeWidthPx * 0.58f, bottomPipeY),
                        size = Size(pipeWidthPx * 1.16f, 18f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }

                // Draw Classic Bird
                val bx = w * 0.25f
                val by = birdY * h
                val br = w * 0.046f

                // Bird body
                drawCircle(
                    color = if (isPaper) PaperText else CyberYellow,
                    radius = br,
                    center = Offset(bx, by)
                )
                // Eye
                drawCircle(
                    color = Color.White,
                    radius = br * 0.35f,
                    center = Offset(bx + br * 0.35f, by - br * 0.2f)
                )
                drawCircle(
                    color = Color.Black,
                    radius = br * 0.18f,
                    center = Offset(bx + br * 0.45f, by - br * 0.2f)
                )
                // Beak
                drawOval(
                    color = Color(0xFFFF9800),
                    topLeft = Offset(bx + br * 0.7f, by - br * 0.1f),
                    size = Size(br * 0.6f, br * 0.4f)
                )
                // Flapping wing
                val wingYOffset = if (wingUp) -br * 0.3f else br * 0.2f
                drawOval(
                    color = Color.White.copy(alpha = 0.9f),
                    topLeft = Offset(bx - br * 0.8f, by + wingYOffset),
                    size = Size(br * 0.8f, br * 0.5f)
                )
            }

            if (!isRunning) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isPaper) PaperBg.copy(alpha = 0.9f) else CyberBlack.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = if (isGameOver) "BORUYA ÇARPTIN!" else "UÇAN KUŞ",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = if (isGameOver) NeonRed else if (isPaper) PaperText else NeonCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isGameOver) "Skor: $score | En Yüksek: $highScore" else "Ekrana her dokunduğunda kuş kanat çırpar.\nBoruların arasından süzül!",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = if (isPaper) PaperTextMuted else TextSecondary
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { flap() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperText else NeonCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isGameOver) "TEKRAR UÇ" else "UÇUŞA BAŞLA",
                                fontWeight = FontWeight.Black,
                                color = if (isPaper) PaperBg else CyberBlack
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { flap() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperText else CyberYellow),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("⚡ KANAT ÇIRP / ZIPLA ⚡", fontWeight = FontWeight.Black, color = if (isPaper) PaperBg else CyberBlack, fontSize = 14.sp)
        }
    }
}

// -------------------------------------------------------------------------------------
// 3. CYBER XOX (Klasik 3x3 Tic Tac Toe - Smart AI & 2-Player)
// -------------------------------------------------------------------------------------

@Composable
fun CyberXoxGame(isPaper: Boolean) {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var isXTurn by remember { mutableStateOf(true) }
    var vsAi by remember { mutableStateOf(true) }
    var winner by remember { mutableStateOf<String?>(null) }
    var xWins by remember { mutableIntStateOf(0) }
    var oWins by remember { mutableIntStateOf(0) }
    var draws by remember { mutableIntStateOf(0) }

    fun checkWinner(b: List<String>): String? {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (pattern in winPatterns) {
            val (i1, i2, i3) = pattern
            if (b[i1].isNotEmpty() && b[i1] == b[i2] && b[i2] == b[i3]) {
                return b[i1]
            }
        }
        if (b.all { it.isNotEmpty() }) return "DRAW"
        return null
    }

    fun makeAiMove(currentBoard: List<String>) {
        if (winner != null) return
        val available = currentBoard.indices.filter { currentBoard[it].isEmpty() }
        if (available.isEmpty()) return

        // 1. Can AI win immediately?
        for (i in available) {
            val test = currentBoard.toMutableList()
            test[i] = "O"
            if (checkWinner(test) == "O") {
                val newBoard = currentBoard.toMutableList()
                newBoard[i] = "O"
                board = newBoard
                winner = "O"
                oWins += 1
                SoundPoolManager.playMechanicalClick()
                return
            }
        }

        // 2. Block player if player about to win
        for (i in available) {
            val test = currentBoard.toMutableList()
            test[i] = "X"
            if (checkWinner(test) == "X") {
                val newBoard = currentBoard.toMutableList()
                newBoard[i] = "O"
                board = newBoard
                winner = checkWinner(newBoard)
                SoundPoolManager.playMechanicalClick()
                return
            }
        }

        // 3. Take center
        if (currentBoard[4].isEmpty()) {
            val newBoard = currentBoard.toMutableList()
            newBoard[4] = "O"
            board = newBoard
            winner = checkWinner(newBoard)
            SoundPoolManager.playMechanicalClick()
            return
        }

        // 4. Random move
        val pick = available.random()
        val newBoard = currentBoard.toMutableList()
        newBoard[pick] = "O"
        board = newBoard
        val win = checkWinner(newBoard)
        if (win != null) {
            winner = win
            if (win == "O") oWins += 1
            else if (win == "DRAW") draws += 1
        }
        SoundPoolManager.playMechanicalClick()
    }

    fun resetBoard() {
        board = List(9) { "" }
        winner = null
        isXTurn = true
        SoundPoolManager.playConfirm()
    }

    fun onCellClick(index: Int) {
        if (board[index].isNotEmpty() || winner != null) return
        val newBoard = board.toMutableList()
        newBoard[index] = if (isXTurn) "X" else "O"
        board = newBoard
        SoundPoolManager.playMechanicalClick()

        val check = checkWinner(newBoard)
        if (check != null) {
            winner = check
            if (check == "X") xWins += 1
            else if (check == "O") oWins += 1
            else if (check == "DRAW") draws += 1
            SoundPoolManager.playConfirm()
            return
        }

        if (vsAi) {
            makeAiMove(newBoard)
        } else {
            isXTurn = !isXTurn
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (!vsAi) {
                        vsAi = true
                        resetBoard()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (vsAi) {
                        if (isPaper) PaperText else CyberYellow
                    } else {
                        if (isPaper) PaperSurface else CyberCardBg
                    }
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = if (vsAi) (if (isPaper) PaperBg else CyberBlack) else Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Yapay Zeka (AI)", fontWeight = FontWeight.Bold, color = if (vsAi) (if (isPaper) PaperBg else CyberBlack) else Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (vsAi) {
                        vsAi = false
                        resetBoard()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!vsAi) {
                        if (isPaper) PaperText else CyberYellow
                    } else {
                        if (isPaper) PaperSurface else CyberCardBg
                    }
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("2 Kişilik", fontWeight = FontWeight.Bold, color = if (!vsAi) (if (isPaper) PaperBg else CyberBlack) else Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Score Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isPaper) PaperSurface else CyberCardBg)
                .border(1.dp, if (isPaper) PaperBorder else CyberCardBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("X (SEN)", fontWeight = FontWeight.Black, color = if (isPaper) PaperText else CyberYellow, fontSize = 12.sp)
                Text("$xWins", fontWeight = FontWeight.Black, color = if (isPaper) PaperText else Color.White, fontSize = 18.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("BERABERE", fontWeight = FontWeight.Bold, color = if (isPaper) PaperTextMuted else TextSecondary, fontSize = 11.sp)
                Text("$draws", fontWeight = FontWeight.Bold, color = if (isPaper) PaperText else Color.White, fontSize = 16.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (vsAi) "O (AI)" else "O (2. OYUNCU)", fontWeight = FontWeight.Black, color = if (isPaper) PaperText else NeonCyan, fontSize = 12.sp)
                Text("$oWins", fontWeight = FontWeight.Black, color = if (isPaper) PaperText else Color.White, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when {
                winner == "X" -> "🏆 X KAZANDI!"
                winner == "O" -> if (vsAi) "🤖 GEN AI KAZANDI!" else "🏆 O KAZANDI!"
                winner == "DRAW" -> "🤝 BERABERE BİTTİ!"
                else -> if (isXTurn) "Sıra: X" else if (vsAi) "AI Hamle Yapıyor..." else "Sıra: O"
            },
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            color = if (isPaper) PaperText else Color.White,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3x3 Grid
        Column(
            modifier = Modifier
                .size(290.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isPaper) PaperSurface else Color(0xFF070D18))
                .border(2.dp, if (isPaper) PaperBorder else CyberCardBorder, RoundedCornerShape(18.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (r in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (c in 0 until 3) {
                        val index = r * 3 + c
                        val cell = board[index]

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isPaper) PaperBg else CyberCardBg)
                                .border(1.dp, if (isPaper) PaperBorder else CyberCardBorder, RoundedCornerShape(12.dp))
                                .clickable { onCellClick(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (cell.isNotEmpty()) {
                                Text(
                                    text = cell,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (cell == "X") {
                                        if (isPaper) PaperText else CyberYellow
                                    } else {
                                        if (isPaper) PaperTextMuted else NeonCyan
                                    },
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = { resetBoard() },
            colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberYellow),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = if (isPaper) PaperText else CyberYellow)
            Spacer(modifier = Modifier.width(8.dp))
            Text("TAHTAYI SIFIRLA", fontWeight = FontWeight.Bold, color = if (isPaper) PaperText else CyberYellow)
        }
    }
}
