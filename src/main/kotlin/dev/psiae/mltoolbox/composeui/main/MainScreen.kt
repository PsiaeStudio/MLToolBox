package dev.psiae.mltoolbox.composeui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastForEach
import dev.psiae.mltoolbox.composeui.*
import dev.psiae.mltoolbox.composeui.CustomWin32TitleBarBehavior
import dev.psiae.mltoolbox.composeui.supportproject.supportProjectMainScreenDrawerItem
import dev.psiae.mltoolbox.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.composeui.text.nonScaledFontSize
import dev.psiae.mltoolbox.platform.win32.CustomDecorationParameters
import dev.psiae.mltoolbox.composeui.modmanager.modManagerMainScreenDrawerItem
import dev.psiae.mltoolbox.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.uifoundation.themes.md3.*
import kotlin.math.max

@Composable
fun MainScreen() {
    val state = rememberMainScreenState()
    MainScreen(state)
}

@Composable
fun MainScreen(
    state: MainScreenState
) {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        MainScreenLayoutSurface(
            modifier = Modifier,
            color = remember { Color(0xFF202018) }
        )
        MainScreenLayoutContent(
            contentPadding = run {
                val margin = MD3Spec.margin.spacingOfWindowWidthDp(maxWidth.value).dp
                PaddingValues(margin, 0.dp, margin, 0.dp)
            }
        )
    }
}

@Composable
fun MainScreenLayoutSurface(
    modifier: Modifier,
    color: Color,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)
            .defaultSurfaceGestureModifiers()
    )
}

@Composable
fun MainScreenLayoutContent(
    contentPadding: PaddingValues
) {
    val leftPadding = contentPadding.calculateLeftPadding(LayoutDirection.Ltr)
    val rightPadding = contentPadding.calculateRightPadding(LayoutDirection.Ltr)
    Column(
        modifier = Modifier
            .layout { measurable, constraints ->
                val rtlAware = false
                val start = leftPadding
                val end = rightPadding
                val top = 0.dp
                val bottom = 0.dp
                val horizontal = start.roundToPx() + end.roundToPx()
                val vertical = top.roundToPx() + bottom.roundToPx()

                val placeable = measurable.measure(constraints.offset(-horizontal, -vertical))

                val width = constraints.constrainWidth(placeable.width + horizontal)
                val height = constraints.constrainHeight(placeable.height + vertical)
                layout(width, height) {
                    if (rtlAware) {
                        placeable.placeRelative(start.roundToPx(), top.roundToPx())
                    } else {
                        placeable.place(start.roundToPx(), top.roundToPx())
                    }
                    CustomDecorationParameters.controlBoxRightOffset = end.roundToPx()
                }
            }
    ) {
        MainScreenLayoutTopBar(contentPadding = PaddingValues(top = contentPadding.calculateTopPadding()))
        MainScreenLayoutBody()
    }
}

@Composable
fun MainScreenLayoutTopBar(
    contentPadding: PaddingValues = PaddingValues()
) {
    val titleBarBehavior = LocalTitleBarBehavior.current as CustomWin32TitleBarBehavior
    Box(
        modifier = Modifier
    ) {
        Layout(
            modifier = Modifier.height(60.dp),
            content = {
                MainScreenLayoutIconTitle(
                    modifier = Modifier.layoutId("ic")
                )
                MainScreenLayoutCaptionControls(
                    modifier = Modifier.layoutId("cpc")
                )
            }
        ) { measurable, inConstraint ->
            val constraint = inConstraint.noMinConstraints()
            val icon = measurable.fastFirst { it.layoutId == "ic" }
            val iconMeasure = icon.measure(constraint)
            val cpc = measurable.fastFirst { it.layoutId == "cpc" }
            val cpcMeasure = cpc.measure(constraint)

            var maxH = max(iconMeasure.height, cpcMeasure.height)
            val alignment = BiasAlignment.Vertical(0f)

            layout(
                height = constraint.maxHeight,
                width = constraint.maxWidth
            ) {
                iconMeasure.place(
                    x = 0,
                    y = alignment.align(iconMeasure.height, constraint.maxHeight)
                )
                cpcMeasure.place(
                    x = constraint.maxWidth.minus(cpcMeasure.width),
                    y = alignment.align(cpcMeasure.height, constraint.maxHeight)
                )

                CustomDecorationParameters
                    .apply {
                        titleBarHeight = constraint.maxHeight
                        controlBoxTopOffset = alignment.align(cpcMeasure.height, constraint.maxHeight)
                    }

                titleBarBehavior
                    .apply {
                        titleBarHeightPx = constraint.maxHeight
                    }
            }
        }
    }
}

@Composable
private fun MainScreenLayoutIconTitle(
    modifier: Modifier
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        /*Icon(
            modifier = Modifier
                .sizeIn(maxWidth = 64.dp, maxHeight = 36.dp),
            painter = painterResource("drawable/icon_manorlords_logo_text.png"),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Spacer(Modifier.width(MD3Spec.padding.incrementsDp(2).dp))*/
        Text(
            modifier = Modifier,
            text = "MANOR LORDS Toolbox",
            style = Material3Theme.typography.titleMedium,
            fontSize = Material3Theme.typography.titleMedium.nonScaledFontSize(),
            color = Color.White,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MainScreenLayoutCaptionControls(
    modifier: Modifier
) {
    val titleBarBehavior = LocalTitleBarBehavior.current
    Row(
        modifier = modifier.layout { measurable, constraints ->

            val measure = measurable.measure(constraints)

            CustomDecorationParameters
                .apply {
                    controlBoxWidth = measure.width
                    controlBoxHeight = measure.height
                }

            layout(measure.width, measure.height) {
                measure.place(0, 0)
            }
        }
    ) {
        Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
            titleBarBehavior.minimizeClicked()
        }) {
            Icon(
                modifier = Modifier.size(20.dp).align(Alignment.Center),
                painter = painterResource("drawable/windowcontrol_minimize_win1.png"),
                contentDescription = null,
                tint = Color.White
            )
        }

        run {
            // keep lambda and painter in sync
            val showRestore = titleBarBehavior.showRestoreWindow
            Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
                if (showRestore) titleBarBehavior.restoreClicked() else titleBarBehavior.maximizeClicked()
            }) {
                Icon(
                    modifier = Modifier.size(20.dp).align(Alignment.Center),
                    painter = if (!showRestore)
                        painterResource("drawable/windowcontrol_maximized_win.png")
                    else
                        painterResource("drawable/windowcontrol_restore_down.png"),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
            titleBarBehavior.closeClicked()
        }) {
            Icon(
                modifier = Modifier.size(20.dp).align(Alignment.Center),
                painter = painterResource("drawable/windowcontrol_close2.png"),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun MainScreenLayoutBody() {
    BoxWithConstraints(
        modifier = Modifier
    ) {
        val maxWidth = maxWidth
        Row {
            val dest = remember { mutableStateOf<StableList<MainDrawerDestination>>(StableList(emptyList()), neverEqualPolicy()) }
            Column(
                modifier = Modifier
                    .widthIn(
                        max = 160.dp,
                    )
            ) {
                MainScreenLayoutDrawerNavigationPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onDestinationClicked = { select ->
                        if (!dest.value.contains(select)) {
                            dest.value = StableList(
                                ArrayList<MainDrawerDestination>()
                                    .apply { addAll(dest.value) ; add(select) }
                            )
                        } else {
                            dest.value = StableList(
                                ArrayList<MainDrawerDestination>()
                                    .apply {
                                        dest.value.forEach {
                                            if (it.id != select.id) add(it)
                                        }
                                        add(select)
                                    }
                            )
                        }
                    },
                    currentDestinationId = dest.value.lastOrNull()?.id
                )
                Box(modifier = Modifier.height(80.dp).width(100.dp))
            }
            Spacer(modifier = Modifier.width(MD3Spec.margin.spacingOfWindowWidthDp(maxWidth.value).dp))
            MainScreenLayoutScreenHost(dest.value)
        }
    }
}

@Composable
fun MainScreenLayoutDrawerNavigationPanel(
    modifier: Modifier,
    onDestinationClicked: (MainDrawerDestination) -> Unit,
    currentDestinationId: String?
) {
    Box(
        modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier) {
            listOf(
                modManagerMainScreenDrawerItem(),
                supportProjectMainScreenDrawerItem()
            ).also {
                LaunchedEffect(Unit) {
                    onDestinationClicked(it.first())
                }
            }.fastForEach { item ->
                val isSelected = currentDestinationId == item.id
                DrawerNavigationPanelItem(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                        .composed {
                            Modifier
                                .then(
                                    if (isSelected)
                                        Modifier.clip(RoundedCornerShape(50)).background(remember { Color(0xFF46492f) })
                                    else
                                        Modifier
                                )
                        }
                        .clickable(
                            enabled = !isSelected,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple()
                        ) { onDestinationClicked(item) },
                    item = item,
                    isSelected = isSelected
                )
            }
        }
    }
}

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
            destinations.fastForEach { dest ->
                key(dest.id) {
                    dest.content.invoke()
                }
            }
        }
    }
}

@Composable
private fun DrawerNavigationPanelItem(
    modifier: Modifier,
    item: MainDrawerDestination,
    isSelected: Boolean
) {
    Row(
        modifier = modifier
            .padding(vertical = 10.dp, horizontal = 15.dp),
    ) {
        if (item.icon != NoOpPainter) {
            Icon(
                modifier = Modifier.size(24.dp).align(Alignment.CenterVertically),
                painter = item.icon,
                tint = item.iconTint ?: Color.Unspecified,
                contentDescription = null
            )
            Spacer(Modifier.width(MD3Spec.padding.incrementsDp(2).dp))
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = item.name,
                style = Material3Theme.typography.labelMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                color = Color(0xFFe5e3d6)
            )
        } else {
            Box(
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Row(
                    modifier = Modifier.alpha(0f)
                ) {
                    Box(Modifier.size(24.dp))
                    Spacer(Modifier.width(MD3Spec.padding.incrementsDp(2).dp))
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = item.name,
                        style = Material3Theme.typography.labelMedium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        color = Color(0xFFe5e3d6)
                    )
                }
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = item.name,
                    style = Material3Theme.typography.labelMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    color = Color(0xFFe5e3d6)
                )
            }

        }

    }
}

@Composable
private fun HostNoDestinationSelected() {

}