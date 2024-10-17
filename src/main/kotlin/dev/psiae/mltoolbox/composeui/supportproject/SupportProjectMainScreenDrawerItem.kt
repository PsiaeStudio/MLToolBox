package dev.psiae.mltoolbox.composeui.supportproject

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import dev.psiae.mltoolbox.composeui.MainDrawerDestination
import dev.psiae.mltoolbox.composeui.NoOpPainter

@Composable
fun supportProjectMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { DonateMainScreen() }
    val painter = NoOpPainter
    return remember(painter) {
        MainDrawerDestination(
            id = "support_project",
            icon = painter,
            iconTint = Color(168, 140, 196),
            name = "Support Project",
            content = content
        )
    }
}