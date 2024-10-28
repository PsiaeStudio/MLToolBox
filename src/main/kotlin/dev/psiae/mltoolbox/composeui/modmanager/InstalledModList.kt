package dev.psiae.mltoolbox.composeui.modmanager

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.composeui.HeightSpacer
import dev.psiae.mltoolbox.composeui.WidthSpacer
import dev.psiae.mltoolbox.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.composeui.theme.md3.Material3Theme

@Composable
fun InstalledModListSection(
    modManagerScreenState: ModManagerScreenState
) {
    val state = rememberInstalledMostListState(modManagerScreenState)
    Box(
        modifier = Modifier
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Material3Theme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .background(Material3Theme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp)
            .defaultSurfaceGestureModifiers(),
    ) {
        var width by remember {
            mutableStateOf(0.dp)
        }
        val density = LocalDensity.current
        Column(
            // TODO: measure content ourselves then render the divider based on it
            modifier = Modifier
                .onGloballyPositioned { coord ->
                    with(density) { coord.size.width.toDp() }.let {
                        width = it
                    }
                }
        ) {
            var selectedTab by remember {
                mutableStateOf<String>("ue4ss_mods")
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
            ) {
                SlotTabItem(
                    Modifier,
                    index = 0,
                    lastIndex = 2,
                    isSelected = selectedTab == "ue4ss_mods",
                    displayName = "UE4SS Mods (${state.installedModList.size})",
                    enabled = true,
                    onClick = {
                        selectedTab = "installed_mods"
                    }
                )
                SlotTabItem(
                    Modifier,
                    index = 1,
                    lastIndex = 2,
                    isSelected = selectedTab == "pak_mods",
                    displayName = "Pak Mods",
                    enabled = false,
                    onClick = {
                        selectedTab = "wip1"
                    }
                )
                SlotTabItem(
                    Modifier,
                    index = 2,
                    lastIndex = 2,
                    isSelected = selectedTab == "wip2",
                    displayName = "WIP",
                    enabled = false,
                    onClick = {
                        selectedTab = "wip2"
                    }
                )
            }
            HeightSpacer(12.dp)
            Divider(
                modifier = Modifier.width(width),
                color = Material3Theme.colorScheme.outlineVariant
            )
            if (selectedTab == "ue4ss_mods") {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                ) {
                    HeightSpacer(8.dp)
                    Text(
                        text = "*note: enable/disable mod not yet implemented",
                        color = Material3Theme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = Material3Theme.typography.labelSmall
                    )
                    HeightSpacer(8.dp)
                }
                Box(modifier = Modifier.padding(horizontal = 16.dp)) { InstalledModListLazyColumn(state) }
            }
        }
    }
}

@Composable
private fun InstalledModListLazyColumn(installedModListState: InstalledModListState) {
    val scrollState = rememberLazyListState()
    Row {
        LazyColumn(
            modifier = Modifier
                .weight(1f, false)
                .sizeIn(maxHeight = 600.dp)
                .width(800.dp),
            state = scrollState
        ) {
            val list = installedModListState.installedModList
            itemsIndexed(
                key = { i, modData -> modData.uniqueQualifiedName },
                items = list
            ) { i, modData ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = modData.name,
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Checkbox(
                        checked = modData.enabled,
                        onCheckedChange = { checked ->

                        },
                        colors = CheckboxDefaults.colors(),
                        enabled = false
                    )
                }
                if (i < list.lastIndex)
                    HeightSpacer(16.dp)
            }
        }
        WidthSpacer(4.dp)
        VerticalScrollbar(
            modifier = Modifier
                .height(
                    with(LocalDensity.current) {
                        remember(this) {
                            derivedStateOf { scrollState.layoutInfo.viewportSize.height.toDp() }
                        }.value
                    }
                )
                .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
                .then(
                    if (scrollState.canScrollForward || scrollState.canScrollBackward)
                        Modifier.background(Color.White.copy(alpha = 0.06f))
                    else Modifier
                ),
            adapter = rememberScrollbarAdapter(scrollState),
            style = remember {
                defaultScrollbarStyle().copy(
                    unhoverColor = Color.White.copy(alpha = 0.25f),
                    hoverColor = Color.White.copy(alpha = 0.50f)
                )
            }
        )
    }
}

@Composable
private fun SlotTabItem(
    modifier: Modifier,
    index: Int,
    lastIndex: Int,
    isSelected: Boolean,
    displayName: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = when {
        index == 0 -> {
            RoundedCornerShape(
                topStart = 12.dp,
                bottomStart = 12.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp
            )
        }
        index == lastIndex -> {
            RoundedCornerShape(
                topStart = 0.dp,
                bottomStart = 0.dp,
                topEnd = 12.dp,
                bottomEnd = 12.dp
            )
        }
        else -> RectangleShape
    }
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 100.dp)
            .height(35.dp)
            .clip(shape)
            .border(
                width = 1.dp,
                color = Material3Theme.colorScheme.outline.copy(alpha = if (enabled) 1f else 0.38f),
                shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.background(Material3Theme.colorScheme.secondaryContainer)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = displayName,
            style = Material3Theme.typography.labelLarge,
            maxLines = 1,
            color = if (!enabled) Material3Theme.colorScheme.onSurface.copy(alpha = 0.38f)
                else if (isSelected) Material3Theme.colorScheme.onSecondaryContainer
                else Material3Theme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis
        )
    }
}