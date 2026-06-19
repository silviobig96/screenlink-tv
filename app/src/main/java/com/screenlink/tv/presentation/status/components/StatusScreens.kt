package com.screenlink.tv.presentation.status.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Text

@Composable
fun ConnectingScreen(message: String) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, fontSize = 28.sp)
    }
}

@Composable
fun ErrorScreen(title: String, message: String, onRetry: (() -> Unit)?) {
    Column(
        Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, fontSize = 36.sp)
        Spacer(Modifier.height(16.dp))
        Text(message, fontSize = 22.sp, textAlign = TextAlign.Center)
        onRetry?.let {
            Spacer(Modifier.height(28.dp))
            Button(onClick = it) { Text("Try again", fontSize = 20.sp) }
        }
    }
}
