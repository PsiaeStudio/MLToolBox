package dev.psiae.mltoolbox.composeui.modmanager

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import dev.psiae.mltoolbox.composeui.MainDrawerDestination

@Composable
fun modManagerMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { ModManagerMainScreen() }
    val painter = painterResource("drawable/icon_ios_glyph_gears_100px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "MODS",
            icon = painter,
            iconTint = null,
            name = "Mod Manager",
            content = content
        )
    }
}