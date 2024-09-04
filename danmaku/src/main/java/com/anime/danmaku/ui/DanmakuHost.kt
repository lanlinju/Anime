package com.anime.danmaku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.anime.danmaku.api.Danmaku
import com.anime.danmaku.api.DanmakuLocation
import com.anime.danmaku.api.DanmakuPresentation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.lang.System.currentTimeMillis
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DanmakuHost(
    state: DanmakuHostState,
    modifier: Modifier = Modifier,
) {

    BoxWithConstraints(modifier) {

        state.hostWidth = constraints.maxWidth
        state.hostHeight = constraints.maxHeight

        DanmakuCanvas {
            for (danmaku in state.presentFloatingDanmaku) {
                drawDanmakuText(
                    state = danmaku.danmaku,
                    screenPosX = { danmaku.screenPosX },
                    screenPosY = { danmaku.screenPosY },
                )
            }
        }
    }

    LaunchedEffect(state.hostWidth, state.hostHeight) { state.setTrackCount() }
    // calculate current play time on every frame
    LaunchedEffect(state.paused) { if (!state.paused) state.interpolateFrameLoop() }
    // logical tick for removal of danmaku
    LaunchedEffect(state.paused) {
        if (!state.paused) {
            while (true) {
                state.tick()
                delay(1000)
            }
        }
    }

}

@Composable
fun DanmakuCanvas(modifier: Modifier = Modifier, onDraw: DrawScope.() -> Unit) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        onDraw()
    }
}


@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, device = Devices.TABLET)
internal fun DanmakuHostPreview() {
    var emitted by remember { mutableIntStateOf(0) }
    val config = remember { mutableStateOf(DanmakuConfig(displayArea = 1.0f, isDebug = true)) }

    val data = remember {
        flow {
            var counter = 0
            val startTime = currentTimeMillis()

            fun danmaku() =
                Danmaku(
                    counter++.toString(),
                    "dummy",
                    currentTimeMillis() - startTime,
                    "1",
                    DanmakuLocation.entries.random(),
                    text = LoremIpsum(Random.nextInt(1..5)).values.first(),
                    0xffffff,
                )

            emit(danmaku())
            emit(danmaku())
            emit(danmaku())
            while (true) {
                emit(danmaku())
                emitted++
                delay(Random.nextLong(5, 10).milliseconds)
            }
        }
    }

    val state = rememberDanmakuHostState(config.value)

    LaunchedEffect(true) {
        data.collect {
            state.trySend(
                DanmakuPresentation(
                    it,
                    isSelf = Random.nextBoolean(),
                ),
            )
        }
    }

    if (isInLandscapeMode()) {
        Row {
            DanmakuHost(
                state,
                Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 40.dp),
            )
        }
    } else {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            DanmakuHost(
                state,
                Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            )
            HorizontalDivider()
        }
    }
}