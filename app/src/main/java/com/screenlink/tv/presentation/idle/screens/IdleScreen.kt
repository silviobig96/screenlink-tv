package com.screenlink.tv.presentation.idle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text

@Composable
fun IdleScreen(appVersion: String = "", connectionLabel: String = "online", showBranding: Boolean = true) {
    if (!showBranding) {
        androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().background(Color.Black))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF123B4A), Color(0xFF05070A), Color.Black),
                    radius = 1_200f,
                ),
            )
            .padding(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF22D3EE)),
                contentAlignment = Alignment.Center,
            ) {
                Text("SL", color = Color(0xFF031018), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text("ScreenLink", color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Display connected",
            color = Color(0xFFE2E8F0),
            fontSize = 30.sp,
            modifier = Modifier.padding(top = 34.dp),
        )
        Text(
            text = "Waiting for content",
            color = Color(0xFF94A3B8),
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp),
        )
        Row(
            modifier = Modifier.padding(top = 38.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusColor(connectionLabel)),
            )
            Text(
                text = connectionLabel.replaceFirstChar { it.uppercase() },
                color = Color(0xFFCBD5E1),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        if (appVersion.isNotBlank()) {
            Text(
                text = "App version $appVersion",
                color = Color(0xFF64748B),
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 18.dp),
            )
        }
    }
}

private fun statusColor(connectionLabel: String): Color = when (connectionLabel.lowercase()) {
    "online" -> Color(0xFF34D399)
    "connecting", "reconnecting" -> Color(0xFFFBBF24)
    else -> Color(0xFF94A3B8)
}
