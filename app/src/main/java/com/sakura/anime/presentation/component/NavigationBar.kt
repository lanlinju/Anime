package com.sakura.anime.presentation.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sakura.anime.presentation.navigation.Screen
import java.util.Locale
import com.sakura.anime.R as Res

@Deprecated("Use NavigationBar(destinations) instead")
@Composable
fun BottomNavigationBar(modifier: Modifier, navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val isNavBarVisible = remember(currentBackStackEntry) {
        val currentDestination = currentBackStackEntry?.destination
        NavigationBarPath.values().any { it.route == currentDestination?.route }
    }

    AnimatedVisibility(
        visible = isNavBarVisible,
        modifier = modifier,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        NavigationBar(navController = navController)
    }
}

@Deprecated("Use NavigationBar(destinations) instead")
@Composable
fun NavigationBar(
    navController: NavController
) {
    // TODO: Can we use `navigationBarsPadding()` instead?
    NavigationBar(
        Modifier.height(
            dimensionResource(Res.dimen.navigation_bar_height) + WindowInsets
                .navigationBars
                .asPaddingValues()
                .calculateBottomPadding()
        ),
        // TODO: Use a `NavigationRail` instead.
        windowInsets = if (LocalConfiguration.current.orientation
            == Configuration.ORIENTATION_LANDSCAPE
        ) {
            WindowInsets.displayCutout
        } else {
            WindowInsets(0.dp)
        }
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        NavigationBarPath.values().forEach { destination ->
            NavigationBarItem(
                modifier = Modifier.navigationBarsPadding(),
                selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = destination.icon
            )
        }
    }
}

@Composable
fun NavigationBar(
    destinations: List<NavigationBarPath>,
    currentDestination: String,
    onNavigateToDestination: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .height(
                dimensionResource(Res.dimen.navigation_bar_height) + WindowInsets
                    .navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            ),
    ) {
        destinations.forEachIndexed { index, destination ->
            val selected = destination.route == currentDestination
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(index) },
                icon = destination.icon,
            )
        }
    }
}

@Composable
fun AdaptiveNavigationBar(
    destinations: List<NavigationBarPath>,
    currentDestination: String,
    onNavigateToDestination: (Int) -> Unit,
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    if (isWideScreen) {
        // 使用 NavigationRail 适配宽屏
        NavigationRail(
            modifier = modifier
                .fillMaxHeight()
        ) {
            destinations.forEachIndexed { index, destination ->
                val selected = destination.route == currentDestination
                NavigationRailItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(index) },
                    icon = destination.icon,
                    label = { Text(destination.route) }, // 可选：添加标签
                )
            }
        }
    } else {
        // 使用 NavigationBar 适配普通屏幕
        NavigationBar(
            modifier = modifier
                .height(
                    dimensionResource(Res.dimen.navigation_bar_height) + WindowInsets
                        .navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                ),
        ) {
            destinations.forEachIndexed { index, destination ->
                val selected = destination.route == currentDestination
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(index) },
                    icon = destination.icon,
                )
            }
        }
    }
}

enum class NavigationBarPath(
    val route: String,
    val icon: @Composable () -> Unit
) {
    RSlash(
        Screen.WeekScreen.route.capitalize(),
        {
            Icon(
                imageVector = ImageVector.vectorResource(
                    id = Res.drawable.rslash
                ),
                contentDescription = stringResource(
                    id = Res.string.rslash
                )
            )
        }
    ),
    Home(
        Screen.HomeScreen.route.capitalize(),
        {
            Icon(
                imageVector = ImageVector.vectorResource(
                    id = Res.drawable.home
                ),
                contentDescription = stringResource(
                    id = Res.string.home
                )
            )
        }
    ),
    Favourite(
        Screen.FavouriteScreen.route.capitalize(),
        {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = stringResource(id = Res.string.favourite)
            )
        }
    )
}

private fun String.capitalize() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }