package com.sakura.anime.presentation.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.sakura.anime.R

@Composable
fun TranslucentStatusBarLayout(
    scrollState: ScrollState,
    distanceUntilAnimated: Dp = dimensionResource(R.dimen.banner_height),
    modifier: Modifier = Modifier,
    targetAlpha: Float = 0.75f,
    targetColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable () -> Unit
) {
    // TODO: Can this be a modifier?
    val distanceUntilAnimatedPx = with(LocalDensity.current) { distanceUntilAnimated.toPx() }
    val statusBarInsets = WindowInsets.statusBars
    Box(
        Modifier
            .drawWithContent {
                drawContent()
                drawRect(
                    color = targetColor.copy(
                        alpha = targetAlpha * (scrollState.value.toFloat() / distanceUntilAnimatedPx)
                            .coerceIn(0f..1f)
                    ),
                    size = Size(
                        width = size.width,
                        height = statusBarInsets
                            .getTop(this)
                            .toFloat()
                    )
                )
            }
            .then(modifier)
    ) {
        content()
    }
}