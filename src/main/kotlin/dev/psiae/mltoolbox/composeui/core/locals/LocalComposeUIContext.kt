package dev.psiae.mltoolbox.composeui.core.locals

import androidx.compose.runtime.staticCompositionLocalOf
import dev.psiae.mltoolbox.composeui.core.ComposeUIContext

val LocalComposeUIContext = staticCompositionLocalOf<ComposeUIContext> {
    staticCompositionLocalNotProvidedFor("LocalComposeUIContext")
}

