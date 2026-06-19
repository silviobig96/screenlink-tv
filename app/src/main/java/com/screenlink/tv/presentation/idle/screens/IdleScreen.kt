package com.screenlink.tv.presentation.idle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text

@Composable
fun IdleScreen(showLabel: Boolean = false) {
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        if (showLabel) Text("ScreenLink ready", color = Color.DarkGray, fontSize = 24.sp)
    }
}
