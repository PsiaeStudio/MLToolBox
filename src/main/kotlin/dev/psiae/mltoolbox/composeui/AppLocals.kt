package dev.psiae.mltoolbox.composeui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ApplicationScope
import dev.psiae.mltoolbox.app.MLToolBoxApp

val LocalApplication = staticCompositionLocalOf<MLToolBoxApp> {
    compositionLocalNotProvidedError("LocalApplication")
}

val LocalComposeApplicationScope = staticCompositionLocalOf<ApplicationScope> {
    compositionLocalNotProvidedError("LocalComposeApplicationScope")
}