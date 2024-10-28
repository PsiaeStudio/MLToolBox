package dev.psiae.mltoolbox.composeui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.composeui.MainDrawerDestination
import dev.psiae.mltoolbox.composeui.StableList
import dev.psiae.mltoolbox.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.utilskt.fastForEach

@Composable
fun MainScreenLayoutScreenHost(
    destinations: StableList<MainDrawerDestination>
) {
    if (destinations.isEmpty()) {
        HostNoDestinationSelected()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .background(remember { Color(29, 24, 34) })
                .defaultSurfaceGestureModifiers()
        ) {
            destinations.fastForEach { destination ->
                destination.content()
            }
        }
    }
}