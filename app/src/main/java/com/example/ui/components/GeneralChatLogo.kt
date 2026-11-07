package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.CyberYellowLight

/**
 * The iconic yellow squircle with bold black "G", matching the user's reference video.
 */
@Composable
fun GeneralChatLogo(
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    val cornerRadius = size * 0.28f
    val fontSize = (size.value * 0.52f).sp

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = CyberYellow.copy(alpha = 0.4f),
                spotColor = CyberYellow
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(CyberYellow)
            .border(2.dp, CyberYellowLight.copy(alpha = 0.8f), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            color = CyberBlack,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            lineHeight = fontSize
        )
    }
}
