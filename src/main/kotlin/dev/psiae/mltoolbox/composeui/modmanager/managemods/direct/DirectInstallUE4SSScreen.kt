package dev.psiae.mltoolbox.composeui.modmanager.managemods.direct

import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.composeui.HeightSpacer
import dev.psiae.mltoolbox.composeui.LocalAwtWindow
import dev.psiae.mltoolbox.composeui.WidthSpacer
import dev.psiae.mltoolbox.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.composeui.modmanager.SimpleTooltip
import dev.psiae.mltoolbox.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.composeui.theme.md3.currentLocalAbsoluteOnSurfaceColor
import dev.psiae.mltoolbox.composeui.theme.md3.surfaceColorAtElevation
import dev.psiae.mltoolbox.java.jFile
import dev.psiae.mltoolbox.uifoundation.themes.md3.MD3Theme
import kotlinx.coroutines.launch
import java.net.URI

@Composable
fun DirectInstallUE4SSScreen(
    directInstallModScreenState: DirectInstallModScreenState
) {
    val ue4ssState = rememberDirectInstallUE4SSScreenState(directInstallModScreenState)
    val window = LocalAwtWindow.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surfaceDim)
            .defaultSurfaceGestureModifiers()
    ) {
        val snackbar = remember { SnackbarHostState() }
        val scrollState = rememberScrollState()
        val topBarScrolling by remember { derivedStateOf { scrollState.value > 0 } }
        CompositionLocalProvider(
            LocalIndication provides ripple(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .height(64.dp)
                        .fillMaxWidth()
                        .background(
                            color = MD3Theme.surfaceColorAtElevation(
                                surface = Material3Theme.colorScheme.surfaceDim,
                                elevation = if (topBarScrolling) 2.dp else 0.dp,
                                tint = Material3Theme.colorScheme.primary
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SimpleTooltip(
                            "go back"
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable(onClick = { directInstallModScreenState.userInputNavigateOutInstallUE4SS() })
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            ) {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource("drawable/arrow_left_simple_32px.png"),
                                    tint = Material3Theme.colorScheme.onSurface,
                                    contentDescription = null
                                )
                            }
                        }

                        WidthSpacer(16.dp)

                        Row {
                            Text(
                                "Install RE-UE4SS mod loader",
                                style = Material3Theme.typography.headlineSmall.copy(
                                    baselineShift = BaselineShift(-0.1f)
                                ),
                                color = Material3Theme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Row {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        HeightSpacer(12.dp)
                        Text(
                            text = "RE-UE4SS Installation",
                            style = Material3Theme.typography.titleLarge,
                            color = Material3Theme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        HeightSpacer(8.dp)
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            val str = buildAnnotatedString {
                                append("1. Download RE-UE4SS: ")
                                pushStringAnnotation(tag = "ue4ss", annotation = "https://www.nexusmods.com/manorlords/mods/229?tab=files")
                                withStyle(SpanStyle(Material3Theme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                                    append("https://www.nexusmods.com/manorlords/mods/229?tab=files")
                                }
                                pop()

                                append("\n")
                                append("2. Import the downloaded (.zip) file below")

                                append("\n\n")
                                withStyle(Material3Theme.typography.bodySmall.toSpanStyle()) {
                                    append("*note: previous installation will be deleted")
                                }
                            }
                            val uriHandler = LocalUriHandler.current
                            ClickableText(
                                text = str,
                                style = Material3Theme.typography.bodyLarge.copy(color = Material3Theme.colorScheme.onSurface),
                                onClick = { offset ->
                                    str.getStringAnnotations(tag = "ue4ss", start = offset, end = offset).let { link ->
                                        if (link.isNotEmpty())
                                            uriHandler.openUri(link.first().item)
                                    }
                                }
                            )

                            HeightSpacer(32.dp)
                            SelectUE4SSArchiveUICard(Modifier, ue4ssState, snackbar)
                            Box(
                                modifier = Modifier.height(16.dp)
                            )
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier
                            .height(
                                with(LocalDensity.current) {
                                    scrollState.viewportSize.toDp()
                                }
                            )
                            .padding(start = 0.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                            .then(
                                if (scrollState.maxValue > 0)
                                    Modifier.background(Color.White.copy(alpha = 0.06f))
                                else Modifier
                            ),
                        adapter = rememberScrollbarAdapter(scrollState),
                        style = run {
                            val absOnSurface = MD3Theme.currentLocalAbsoluteOnSurfaceColor()
                            defaultScrollbarStyle().copy(
                                unhoverColor = absOnSurface.copy(alpha = 0.25f),
                                hoverColor = absOnSurface.copy(alpha = 0.50f),
                                thickness = 4.dp
                            )
                        }
                    )
                }
            }
            SnackbarHost(
                modifier = Modifier.align(Alignment.BottomCenter),
                hostState = snackbar
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SelectUE4SSArchiveUICard(
    modifier: Modifier,
    ue4ssState: DirectInstallUE4SSScreenState,
    snackbar: SnackbarHostState
) {
    val window = LocalAwtWindow.current
    Column(
        modifier = modifier
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val draggingInBoundState = remember {
                mutableStateOf(false)
            }
            val showInBoundEffect = true
            val leastConstraintsMax = minOf(maxWidth, maxHeight)
            println("leastConstraintsMax=$leastConstraintsMax")
            ElevatedCard(
                modifier = Modifier
                    .align(Alignment.Center)
                    .then(
                        if (LocalIsDarkTheme.current)
                            Modifier.shadow(elevation = 2.dp, RoundedCornerShape(12.dp))
                        else
                            Modifier
                    )
                /*.verticalScroll(rememberScrollState())*/,
                colors = CardDefaults.cardColors(containerColor = Material3Theme.colorScheme.surfaceContainerHigh, contentColor = Material3Theme.colorScheme.onSurface)
            ) {
                val contentMinSize = when {
                    leastConstraintsMax < 1000.dp -> 300.dp
                    else -> 450.dp
                }
                if (ue4ssState.isLoading) {
                    Column(
                        Modifier
                            .defaultMinSize(contentMinSize, contentMinSize)
                            .padding(36.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(160.dp),
                            color = Material3Theme.colorScheme.secondary
                        )
                        HeightSpacer(30.dp)
                        Text(
                            modifier = Modifier,
                            text = ue4ssState.statusMessage ?: "",
                            style = Material3Theme.typography.titleMedium,
                            color = Material3Theme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                } else if (ue4ssState.selectedUE4SSArchive == null) {
                    val dragAndDropTarget = remember {
                        object : DragAndDropTarget {
                            override fun onEntered(event: DragAndDropEvent) {
                                draggingInBoundState.value = true
                            }

                            override fun onExited(event: DragAndDropEvent) {
                                draggingInBoundState.value = false
                            }

                            override fun onDrop(event: DragAndDropEvent): Boolean {
                                draggingInBoundState.value = false
                                ue4ssState.userDropUE4SSArchive(
                                    jFile(URI((event.dragData() as DragData.FilesList).readFiles().first()))
                                )
                                return true
                            }
                        }
                    }
                    @OptIn(ExperimentalFoundationApi::class)
                    Column(
                        Modifier
                            .defaultMinSize(contentMinSize, contentMinSize)
                            .dragAndDropTarget(
                                shouldStartDragAndDrop = { event ->
                                    val dragData = event.dragData()
                                    val accept = dragData is DragData.FilesList &&
                                        dragData.readFiles().let { files ->
                                            files.size == 1 && files.first().let {
                                                jFile(URI(it)).isFile && (it.endsWith(".zip", ignoreCase = true))
                                            }
                                        }
                                    return@dragAndDropTarget accept
                                },
                                target = dragAndDropTarget
                            )
                            .then(
                                if (draggingInBoundState.value && showInBoundEffect) {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = Color.Green,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                } else Modifier
                            )
                            .clickable { ue4ssState.pickUE4SSArchive(window) }
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.padding(12.dp).size(128.dp),
                            painter = painterResource("drawable/icon_import_mm_128px.png"),
                            contentDescription = null,
                            tint = Material3Theme.colorScheme.secondary
                        )
                        HeightSpacer(12.dp)
                        Text(
                            text = "Select or Drop the archive (*.zip) here",
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.bodyMedium
                        )

                        if (ue4ssState.isLastSelectedArchiveInvalid) {
                            HeightSpacer(16.dp)
                            val ins = remember { MutableInteractionSource() }
                            val clipBoardManager = LocalClipboardManager.current
                            val text = buildAnnotatedString {
                                append("[Error][invalid archive]: ${ue4ssState.statusMessage}")
                            }
                            val coroutineScope = rememberCoroutineScope()
                            SimpleTooltip("click to copy") {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Material3Theme.colorScheme.error)
                                        .clickable(
                                            interactionSource = ins,
                                            indication = LocalIndication.current
                                        ) {
                                            clipBoardManager.setText(
                                                annotatedString = text
                                            )
                                            coroutineScope.launch {
                                                snackbar.currentSnackbarData?.dismiss()
                                                snackbar.showSnackbar(message = "Copied to clipboard", withDismissAction = true)
                                            }
                                        }
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f, false),
                                        text = text,
                                        color = Material3Theme.colorScheme.onError,
                                        style = Material3Theme.typography.bodyMedium,
                                    )
                                    Box(modifier = Modifier.size(32.dp).align(Alignment.Top)) {
                                        if (/*ins.collectIsHoveredAsState().value*/ true) {
                                            Icon(
                                                modifier = Modifier.size(24.dp).align(Alignment.Center),
                                                painter = painterResource("drawable/icon_copy_24px.png"),
                                                contentDescription = null,
                                                tint = Material3Theme.colorScheme.onError
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (ue4ssState.isInvalidGameDirectory) {
                            HeightSpacer(16.dp)
                            val ins = remember { MutableInteractionSource() }
                            val clipBoardManager = LocalClipboardManager.current
                            val text = buildAnnotatedString {
                                append("[Error][invalid game dir]: ${ue4ssState.statusMessage}")
                            }
                            val coroutineScope = rememberCoroutineScope()
                            SimpleTooltip("click to copy") {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Material3Theme.colorScheme.error)
                                        .clickable(
                                            interactionSource = ins,
                                            indication = LocalIndication.current
                                        ) {
                                            clipBoardManager.setText(
                                                annotatedString = text
                                            )
                                            coroutineScope.launch {
                                                snackbar.currentSnackbarData?.dismiss()
                                                snackbar.showSnackbar(message = "Copied to clipboard", withDismissAction = true)
                                            }
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f, false),
                                        text = text,
                                        color = Material3Theme.colorScheme.onError,
                                        style = Material3Theme.typography.bodyMedium,
                                    )
                                    Box(modifier = Modifier.size(32.dp).align(Alignment.Top)) {
                                        if (/*ins.collectIsHoveredAsState().value*/ true) {
                                            Icon(
                                                modifier = Modifier.size(24.dp).align(Alignment.Center),
                                                painter = painterResource("drawable/icon_copy_24px.png"),
                                                contentDescription = null,
                                                tint = Material3Theme.colorScheme.onError
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (ue4ssState.isInstalledSuccessfully) {
                            HeightSpacer(24.dp)
                            val text = buildAnnotatedString {
                                append("Installed Successfully !")
                            }
                            Text(
                                modifier = Modifier.weight(1f, false),
                                text = text,
                                color = Material3Theme.colorScheme.primary,
                                style = Material3Theme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

    }
}