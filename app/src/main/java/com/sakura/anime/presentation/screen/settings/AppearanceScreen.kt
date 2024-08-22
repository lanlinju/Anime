package com.sakura.anime.presentation.screen.settings

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sakura.anime.R
import com.sakura.anime.presentation.theme.AnimeTheme
import com.sakura.anime.util.SettingsPreferences
import com.sakura.anime.util.catpucchinLatte

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    onBackClick: () -> Unit = {}
) {
    val topBarBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(id = R.string.appearance_settings)) },
                scrollBehavior = topBarBehavior,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(topBarBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeModeSettings(modifier = Modifier.padding(start = 24.dp))

            val selectedColor by SettingsPreferences.customColor.collectAsState()
            val dynamicColor by SettingsPreferences.dynamicColor.collectAsState()

            ColorBall(
                selectedColor = selectedColor,
                isCheckVisible = !dynamicColor,
                onSelect = {
                    SettingsPreferences.changeDynamicColor(false)
                    SettingsPreferences.changeCustomColor(it)
                }
            )

            SwitchPref(
                title = stringResource(id = R.string.dynamic_color),
                summary = stringResource(id = R.string.dynamic_color_description),
                painter = rememberVectorPainter(image = Icons.Outlined.Colorize),
                checked = dynamicColor,
                onCheckedChange = {
                    SettingsPreferences.changeDynamicColor(it)
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ThemeModeSettings(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val options =
            SettingsPreferences.ThemeMode.values().map { stringResource(id = it.resId) }
        var selectedIndex by remember {
            mutableIntStateOf(
                SettingsPreferences.ThemeMode.values().indexOf(SettingsPreferences.themeMode.value)
            )
        }

        Text(stringResource(id = R.string.theme))

        Spacer(modifier = Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    onClick = {
                        selectedIndex = index
                        val themeMode = SettingsPreferences.ThemeMode.values()[index]
                        SettingsPreferences.changeThemeMode(themeMode)
                    },
                    selected = index == selectedIndex
                ) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
fun ColorBall(
    modifier: Modifier = Modifier,
    selectedColor: Int,
    isCheckVisible: Boolean = true,
    onSelect: (Int) -> Unit,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
        items(catpucchinLatte) { color ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .clickable { onSelect(color) },
                contentAlignment = Alignment.Center
            ) {
                if (color == selectedColor && isCheckVisible) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun SwitchPref(
    title: String,
    summary: String? = null,
    checked: Boolean,
    painter: Painter? = null,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) {
                onCheckedChange(!checked)
            },
        headlineContent = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        leadingContent = {
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                )
            }
        },
        supportingContent = {
            if (summary != null) {
                Text(text = summary)
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                }
            )
        }
    )
}

@Preview
@Composable
private fun AppearanceScreenPreview() {
    AnimeTheme {
        AppearanceScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SwitchPrefPreview() {
    AnimeTheme {
        Column {
            SwitchPref(
                title = "Dynamic Color",
                summary = "Apply dynamic color from wallpaper",
                checked = true
            )
        }

    }
}

/*@Composable
fun RowScope.ColorButton(
    modifier: Modifier = Modifier,
    color: Color = Color.Green,
    index: Int = 0,
    tonalStyle: PaletteStyle = PaletteStyle.TonalSpot,
) {
    val tonalPalettes by remember {
        mutableStateOf(color.toTonalPalettes(tonalStyle))
    }
    val isSelect =
        !LocalDynamicColorSwitch.current && LocalSeedColor.current == color.toArgb() && LocalPaletteStyleIndex.current == index
    ColorButtonImpl(modifier = modifier, tonalPalettes = tonalPalettes, isSelected = { isSelect }) {
        PreferenceUtil.switchDynamicColor(enabled = false)
        PreferenceUtil.modifyThemeSeedColor(color.toArgb(), index)
    }

}

@Composable
fun RowScope.ColorButtonImpl(
    modifier: Modifier = Modifier,
    isSelected: () -> Boolean = { false },
    tonalPalettes: TonalPalettes,
    cardColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: () -> Unit = {}
) {

    val containerSize by animateDpAsState(targetValue = if (isSelected.invoke()) 28.dp else 0.dp)
    val iconSize by animateDpAsState(targetValue = if (isSelected.invoke()) 16.dp else 0.dp)

    Surface(
        modifier = modifier
            .padding(4.dp)
            .sizeIn(maxHeight = 80.dp, maxWidth = 80.dp, minHeight = 64.dp, minWidth = 64.dp)
            .weight(1f, false)
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        onClick = onClick
    ) {
        CompositionLocalProvider(LocalTonalPalettes provides tonalPalettes) {
            val color1 = 80.a1
            val color2 = 90.a2
            val color3 = 60.a3
            Box(Modifier.fillMaxSize()) {
                Box(modifier = modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .drawBehind { drawCircle(color1) }
                    .align(Alignment.Center)) {
                    Surface(
                        color = color2, modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(24.dp)
                    ) {}
                    Surface(
                        color = color3, modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                    ) {}
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .size(containerSize)
                            .drawBehind { drawCircle(containerColor) },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier
                                .size(iconSize)
                                .align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                }
            }
        }
    }
}*/