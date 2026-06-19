package com.screenlink.tv.presentation.pairing.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Text

@Composable
fun PairingScreen(
    code: String,
    expiresAt: String,
    message: String?,
    onRefresh: () -> Unit,
    onClearDebug: (() -> Unit)?,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Pair this TV", fontSize = 38.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(20.dp))
        Text("Enter this code in the ScreenLink dashboard", fontSize = 24.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        Text(code.chunked(3).joinToString("  "), fontSize = 76.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp)
        Spacer(Modifier.height(18.dp))
        Text("Expires: $expiresAt", fontSize = 18.sp)
        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, fontSize = 18.sp, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(28.dp))
        Button(onClick = onRefresh) { Text("Refresh pairing code", fontSize = 20.sp) }
        onClearDebug?.let { clear ->
            Spacer(Modifier.height(12.dp))
            Button(onClick = clear) { Text("Clear credentials (debug)", fontSize = 16.sp) }
        }
    }
}
