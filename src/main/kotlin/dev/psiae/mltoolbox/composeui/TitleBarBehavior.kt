package dev.psiae.mltoolbox.composeui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalTitleBarBehavior = staticCompositionLocalOf<TitleBarBehavior> {
    compositionLocalNotProvidedError(
        "TitleBarBehavior "
    )
}

@Stable
interface TitleBarBehavior {
    val showRestoreWindow: Boolean
    val titleBarHeightPx: Int

    fun minimizeClicked()
    fun restoreClicked()
    fun maximizeClicked()
    fun closeClicked()
}