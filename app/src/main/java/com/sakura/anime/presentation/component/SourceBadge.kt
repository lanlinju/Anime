package com.sakura.anime.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceBadge(
    modifier: Modifier = Modifier,
    text: String,
    isAlignmentStart: Boolean = true,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    content: @Composable () -> Unit
) {
    Box(modifier) {
        content()

        Badge(
            modifier = Modifier
                .padding(4.dp)
                .align(if (isAlignmentStart) Alignment.TopStart else Alignment.TopEnd),
            containerColor = containerColor,
            contentColor = contentColor
        ) {
            Text(
                modifier = Modifier.padding(2.dp),
                text = text,
                style = style,
            )
        }

    }
}