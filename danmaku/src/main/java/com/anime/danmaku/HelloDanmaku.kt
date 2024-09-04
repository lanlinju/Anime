package com.anime.danmaku

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HelloDanmaku(modifier: Modifier = Modifier) {
    Text(text = "Hello danmaku!")
}

@Preview(showBackground = true)
@Composable
private fun PreviewTest() {
    HelloDanmaku()
}