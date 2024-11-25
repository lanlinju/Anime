package com.lanlinju.animius.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.lanlinju.animius.R

@Composable
fun ScrollableText(
    text: String,
    modifier: Modifier = Modifier,
    gradientSize: Dp = dimensionResource(R.dimen.edge_gradient_size),
    gradientColor: Color = MaterialTheme.colorScheme.background
) {
    Box(modifier) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground.copy(
                alpha = 0.75f
            ),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = gradientSize)
        )

        Box(
            modifier = Modifier
                .height(gradientSize)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            gradientColor,
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .height(gradientSize)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            gradientColor
                        )
                    )
                )
        )
    }
}