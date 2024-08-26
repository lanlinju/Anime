package com.sakura.anime.presentation.screen.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.ColorScheme
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.color.DynamicColors
import com.sakura.anime.R
import com.sakura.anime.presentation.theme.AnimeTheme
import com.sakura.anime.util.KEY_DYNAMIC_IMAGE_COLOR
import com.sakura.anime.util.SettingsPreferences
import com.sakura.anime.util.catpucchinLatte
import com.sakura.anime.util.getSchemeFromSeed
import com.sakura.anime.util.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    onBackClick: () -> Unit = {}
) {
    val topBarBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
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
            var isDynamicImageColor by rememberPreference(KEY_DYNAMIC_IMAGE_COLOR, true)

            ColorBall(
                selectedColor = selectedColor,
                isCheckVisible = !dynamicColor,
                onSelect = {
                    isDynamicImageColor = false
                    SettingsPreferences.changeDynamicColor(false)
                    SettingsPreferences.changeCustomColor(it)
                }
            )

            /*
            ColorButtonRow(
                selectedColor = selectedColor,
                isCheckVisible = !dynamicColor,
                onSelect = {
                    isDynamicImageColor = false
                    SettingsPreferences.changeDynamicColor(false)
                    SettingsPreferences.changeCustomColor(it)
                }
            )*/

            if (DynamicColors.isDynamicColorAvailable()) {
                SwitchPref(
                    title = stringResource(id = R.string.dynamic_color),
                    summary = stringResource(id = R.string.dynamic_color_description),
                    painter = rememberVectorPainter(image = Icons.Outlined.Colorize),
                    checked = dynamicColor,
                    onCheckedChange = {
                        isDynamicImageColor = false
                        SettingsPreferences.changeDynamicColor(it)
                    }
                )
            }

            SwitchPref(
                title = stringResource(id = R.string.dynamic_image_color),
                summary = stringResource(id = R.string.dynamic_image_color_description),
                painter = rememberVectorPainter(image = Icons.Outlined.Image),
                checked = isDynamicImageColor,
                onCheckedChange = {
                    isDynamicImageColor = it
                    SettingsPreferences.changeDynamicColor(!isDynamicImageColor)
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

/**
 * [isCheckVisible] 如果是动态主题就不显示CheckIcon
 */
@Composable
fun ColorBall(
    modifier: Modifier = Modifier,
    selectedColor: Int,
    isCheckVisible: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
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
            val isSelected = color == selectedColor && isCheckVisible
            val containerSize by animateDpAsState(targetValue = if (isSelected) 28.dp else 0.dp)
            val iconSize by animateDpAsState(targetValue = if (isSelected) 16.dp else 0.dp)

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .clickable { onSelect(color) },
                contentAlignment = Alignment.Center
            ) {
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

@Composable
fun ColorButtonRow(
    modifier: Modifier = Modifier,
    selectedColor: Int,
    isCheckVisible: Boolean = true,
    onSelect: (Int) -> Unit,
) {
    Row {
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
        ) {
            items(catpucchinLatte) { color ->
                ColorButton(
                    modifier = modifier,
                    originColor = Color(color),
                    colorScheme = getSchemeFromSeed(color, false),
                    isSelected = { color == selectedColor && isCheckVisible }) {
                    onSelect(color)
                }

            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun ColorButtonImpl() {
    AnimeTheme {
        Row(modifier = Modifier.size(200.dp)) {
            ColorButton(
                colorScheme = getSchemeFromSeed(
                    catpucchinLatte[2],
                    dark = false
                ),
                isSelected = { true },
                originColor = Color(catpucchinLatte[2])
            )
        }
    }
}

@Composable
fun RowScope.ColorButton(
    modifier: Modifier = Modifier,
    isSelected: () -> Boolean = { false },
    originColor: Color,
    colorScheme: ColorScheme,
    cardColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: () -> Unit = {}
) {

    val containerSize by animateDpAsState(targetValue = if (isSelected()) 28.dp else 0.dp)
    val iconSize by animateDpAsState(targetValue = if (isSelected()) 16.dp else 0.dp)

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

        val primary = originColor
        val secondary = colorScheme.tertiary
        val tertiary = colorScheme.primary
        Box(Modifier.fillMaxSize()) {
            Box(modifier = modifier
                .size(48.dp)
                .clip(CircleShape)
                .drawBehind { drawCircle(primary) }
                .align(Alignment.Center)
            ) {
                Surface(
                    color = secondary,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(24.dp)
                ) {}
                Surface(
                    color = tertiary,
                    modifier = Modifier
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
                        tint = colorScheme.onPrimaryContainer
                    )
                }

            }
        }
    }
}