package com.sakura.anime.presentation.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.sakura.anime.R
import com.sakura.anime.util.VIDEO_ASPECT_RATIO

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PopupMenuListItem(
    content: @Composable () -> Unit,
    menuText: String,
    onClick: () -> Unit,
    onMenuItemClick: () -> Unit,
) {

    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier.combinedClickable(
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                expanded = true
            },
            onClick = onClick
        )
    ) {

        content()

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(
                x = dimensionResource(id = R.dimen.image_cover_height) * VIDEO_ASPECT_RATIO
                        + dimensionResource(id = R.dimen.small_padding),
                y = (-40).dp
            ),
        ) {

            DropdownMenuItem(
                text = {
                    Text(
                        text = menuText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                onClick = {
                    expanded = false
                    onMenuItemClick()
                }
            )

        }
    }
}