package com.sakura.videoplayer.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

internal val Icons.Rounded.ArrowBackIos: ImageVector
    get() {
        if (_arrowBackIos != null) {
            return _arrowBackIos!!
        }
        _arrowBackIos = materialIcon(name = "Rounded.ArrowBackIos") {
            materialPath {
                moveTo(16.62f, 2.99f)
                curveToRelative(-0.49f, -0.49f, -1.28f, -0.49f, -1.77f, 0.0f)
                lineTo(6.54f, 11.3f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(8.31f, 8.31f)
                curveToRelative(0.49f, 0.49f, 1.28f, 0.49f, 1.77f, 0.0f)
                reflectiveCurveToRelative(0.49f, -1.28f, 0.0f, -1.77f)
                lineTo(9.38f, 12.0f)
                lineToRelative(7.25f, -7.25f)
                curveToRelative(0.48f, -0.48f, 0.48f, -1.28f, -0.01f, -1.76f)
                close()
            }
        }
        return _arrowBackIos!!
    }

private var _arrowBackIos: ImageVector? = null
