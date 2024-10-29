package dev.psiae.mltoolbox.composeui.modmanager

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import dev.psiae.mltoolbox.composeui.HeightSpacer
import dev.psiae.mltoolbox.composeui.LocalAwtWindow
import dev.psiae.mltoolbox.composeui.WidthSpacer
import dev.psiae.mltoolbox.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.composeui.theme.md3.currentLocalAbsoluteOnSurfaceColor
import dev.psiae.mltoolbox.java.jFile
import dev.psiae.mltoolbox.platform.content.filepicker.JnaFileChooserWindowHost
import dev.psiae.mltoolbox.platform.content.filepicker.win32.JnaFileChooser
import dev.psiae.mltoolbox.uifoundation.themes.md3.MD3Spec
import dev.psiae.mltoolbox.uifoundation.themes.md3.MD3Theme
import dev.psiae.mltoolbox.uifoundation.themes.md3.incrementsDp
import dev.psiae.mltoolbox.uifoundation.themes.md3.padding
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import javax.swing.SwingUtilities
import kotlin.jvm.optionals.getOrNull
import kotlin.math.round

typealias jProcessHandle = java.lang.ProcessHandle

@Composable
fun SelectGameWorkingDirectoryScreen(
    modManagerScreenState: ModManagerScreenState
) {
    val state = remember(modManagerScreenState) {
        SelectGameWorkingDirectoryState(modManagerScreenState)
    }
    if (state.openExePicker) {
        GameExecutablePicker(state)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surfaceDim)
            .defaultSurfaceGestureModifiers()
    ) {
        val scrollState = rememberScrollState()
        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .align(Alignment.CenterVertically)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center
            ) {
                HeightSpacer(16.dp)
                ElevatedCard(
                    modifier = Modifier
                        .padding(horizontal = 48.dp)
                        .sizeIn(maxWidth = 1400.dp, maxHeight = 1400.dp)
                        .align(Alignment.CenterHorizontally)
                        .then(
                            if (LocalIsDarkTheme.current)
                                Modifier.shadow(elevation = 2.dp, RoundedCornerShape(12.dp))
                            else
                                Modifier
                        ),
                    colors = CardDefaults
                        .cardColors(Material3Theme.colorScheme.surfaceContainerHigh, contentColor = Material3Theme.colorScheme.onSurface)
                ) {
                    Column(
                        Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Select the game executable file (.exe) to continue",
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.titleMedium
                        )
                        HeightSpacer(4.dp)
                        Text(
                            text = "Their location are usually defined as follows:",
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.labelSmall
                        )
                        HeightSpacer(16.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                painter = painterResource("drawable/icon_steam_logo_convert_32px.png"),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                            WidthSpacer(12.dp)
                            Column {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            Material3Theme.typography.titleSmall.toSpanStyle()
                                                .copy(fontWeight = FontWeight.Bold)
                                        ) {
                                            append("Steam".replace(' ', '\u00A0'))
                                        }
                                    },
                                    color = Material3Theme.colorScheme.onSurface,
                                    style = Material3Theme.typography.bodySmall,
                                )
                                Text(
                                    modifier = Modifier.padding(start = 12.dp),
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                                            append("**\\SteamLibrary\\steamapps\\common\\Manor Lords\\ManorLords\\Binaries\\Win64\\ManorLords-Win64-Shipping.exe".replace(' ', '\u00A0'))
                                        }
                                    },
                                    color = Material3Theme.colorScheme.onSurface,
                                    style = Material3Theme.typography.bodySmall,
                                )
                            }
                        }
                        HeightSpacer(20.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                painter = painterResource("drawable/icon_xbox_logo_convert_32px.png"),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                            WidthSpacer(12.dp)
                            Column {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            Material3Theme.typography.titleSmall.toSpanStyle()
                                                .copy(fontWeight = FontWeight.Bold)
                                        ) {
                                            append("Xbox pc gamepass".replace(' ', '\u00A0'))
                                        }
                                    },
                                    color = Material3Theme.colorScheme.onSurface,
                                    style = Material3Theme.typography.bodySmall,
                                )
                                Text(
                                    modifier = Modifier.padding(start = 12.dp),
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                                            append("**\\XboxGames\\*\\Manor Lords\\Content\\ManorLords\\Binaries\\WinGDK\\ManorLords-WinGDK-Shipping.exe".replace(' ', '\u00A0'))                                    }
                                    },
                                    color = Material3Theme.colorScheme.onSurface,
                                    style = Material3Theme.typography.bodySmall,
                                )
                            }
                        }

                        Text(
                            modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
                            text = "*note: currently this program does not check whether the selected file is actually ManorLords",
                            color = Material3Theme.colorScheme.onSecondaryContainer,
                            style = Material3Theme.typography.labelSmall
                        )

                        BoxWithConstraints(modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp)
                            .defaultMinSize(minWidth = 500.dp)
                            .defaultMinSize(minHeight = 42.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .then(
                                if (LocalIsDarkTheme.current)
                                    Modifier.shadow(elevation = 2.dp, RoundedCornerShape(4.dp))
                                else
                                    Modifier.border(width = 1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                            )
                            .clickable(enabled = state.allowUserFileInput()) { state.userInputOpenDirPicker() }
                            .background(Material3Theme.colorScheme.inverseOnSurface)
                            .padding(MD3Spec.padding.incrementsDp(2).dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(horizontal = 2.dp)
                                    .defaultMinSize(minWidth = with(LocalDensity.current) { constraints.minWidth.toDp() }),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                run {
                                    val f = state.chosenDir
                                    val fp = state.chosenDir?.absolutePath
                                    val (fp1, fp2) = remember(f) {
                                        run {
                                            var dash = false
                                            fp?.dropLastWhile { c -> !dash.also { dash = c == '\\' } }
                                        } to run {
                                            var dash = false
                                            fp?.takeLastWhile { c -> !dash.also { dash = c == '\\' } }
                                        }
                                    }
                                    val color = Material3Theme.colorScheme.onSurface.copy(alpha = 0.78f)
                                    Text(
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .align(Alignment.CenterVertically),
                                        text = fp1?.plus(fp2) ?: "Select game executable",
                                        style = Material3Theme.typography.labelMedium,
                                        color = color,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                WidthSpacer(MD3Spec.padding.incrementsDp(2).dp)
                                Icon(
                                    modifier = Modifier.size(18.dp).align(Alignment.CenterVertically),
                                    painter = painterResource("drawable/icon_folder_96px.png"),
                                    contentDescription = null,
                                    tint = Material3Theme.colorScheme.secondary
                                )
                            }
                        }

                        Row(modifier = Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                            BoxWithConstraints(modifier = Modifier
                                .padding(8.dp)
                                .defaultMinSize(minHeight = 36.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shadow(elevation = 2.dp, RoundedCornerShape(4.dp))
                                .clickable(enabled = state.allowUserFileInput()) { state.userInputFindFromRunningGameInstance() }
                                .background(Material3Theme.colorScheme.secondary)
                                .padding(horizontal = MD3Spec.padding.incrementsDp(2).dp, vertical = MD3Spec.padding.incrementsDp(1).dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(horizontal = 2.dp)
                                        .defaultMinSize(minWidth = with(LocalDensity.current) { constraints.minWidth.toDp() }),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    run {
                                        if (state.searchingManorLordsProcess) {
                                            Row {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = Material3Theme.colorScheme.onSecondary,
                                                    strokeWidth = 1.dp
                                                )
                                                WidthSpacer(8.dp)
                                                Text(
                                                    modifier = Modifier
                                                        .weight(1f, fill = false)
                                                        .align(Alignment.CenterVertically),
                                                    text = "Searching for process ...",
                                                    style = Material3Theme.typography.labelMedium,
                                                    color = Material3Theme.colorScheme.onSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                            }
                                        } else {
                                            Text(
                                                modifier = Modifier
                                                    .weight(1f, fill = false)
                                                    .align(Alignment.CenterVertically),
                                                text = "Detect from running instance",
                                                style = Material3Theme.typography.labelMedium,
                                                color = Material3Theme.colorScheme.onSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                    }
                                    /*WidthSpacer(MD3Spec.padding.incrementsDp(2).dp)*/
                                    /*Icon(
                                        modifier = Modifier.size(18.dp).align(Alignment.CenterVertically),
                                        painter = painterResource("drawable/icon_folder_96px.png"),
                                        contentDescription = null,
                                        tint = remember { Color(168, 140, 196) }
                                    )*/
                                }
                            }
                            SimpleTooltip(
                                "Auto select, start ManorLords then click the detect button"
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.CenterVertically),
                                    painter = painterResource("drawable/icon_info_filled_32px.png"),
                                    contentDescription = null,
                                    tint = Material3Theme.colorScheme.secondary
                                )
                            }
                        }
                        if (state.traverseRunningInstanceNotFound) {
                            HeightSpacer(16.dp)
                            Text(
                                modifier = Modifier.align(Alignment.CenterHorizontally).weight(1f, false),
                                text = "ManorLords process not found",
                                color = Material3Theme.colorScheme.error,
                                style = Material3Theme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                HeightSpacer(16.dp)
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
}

@Composable
fun ChangeGameWorkingDirectory(modManagerComposeState: ModManagerScreenState) {
    val state = remember(modManagerComposeState) {
        SelectGameWorkingDirectoryState(modManagerComposeState)
            .apply { userInputOpenDirPicker() }
    }
    if (state.openExePicker) {
        GameExecutablePicker(state)
    }
}

@Composable
private fun GameExecutablePicker(state: SelectGameWorkingDirectoryState) {
    val window = LocalAwtWindow.current
    LaunchedEffect(Unit) {
        val pick = FileKit.pickFile(
            title = "Select Game Executable (*.exe)",
            type = PickerType.File(listOf("exe")),
            platformSettings = FileKitPlatformSettings(parentWindow = window),
            initialDirectory = state.chosenDir?.absolutePath
        )
        state.filePick(pick?.file)
    }
}

@Composable
private fun NativeFilePickerDialog(
    title: String,
    initialDir: String?,
    initialFileName: String,
    onCloseRequest: (File?) -> Unit,
    filter: () -> List<Pair<String, Array<String>>>? = { null },
    mode: JnaFileChooser.Mode = JnaFileChooser.Mode.Files,
    multiSelect: Boolean = true,
    key: Any = ""
) {
    key(key) {
        val window = LocalAwtWindow.current
        AwtWindow(
            visible = false,
            create = {
                JnaFileChooserWindowHost(window, title, initialDir, initialFileName, filter, mode, multiSelect)
                    .apply {
                        openAndInvokeOnCompletion { result ->
                            SwingUtilities.invokeLater {
                                onCloseRequest(result.getOrThrow())
                            }
                        }
                    }
            },
            dispose = JnaFileChooserWindowHost::dispose
        )
    }
}

private class SelectGameWorkingDirectoryState(
    private val modManagerScreenState: ModManagerScreenState
) {
    var chosenDir: jFile? by mutableStateOf(null)
        private set

    var openExePicker by mutableStateOf(false)
        private set

    var searchingManorLordsProcess by mutableStateOf(false)
        private set

    var traverseRunningInstanceNotFound by mutableStateOf(false)
        private set

    fun filePick(jFile: jFile?) {
        chosenDir = jFile
        openExePicker = false
        modManagerScreenState.runOnUiContext {
            modManagerScreenState.chosenGameBinaryFile(jFile)
        }
    }

    fun userInputOpenDirPicker() {
        traverseRunningInstanceNotFound = false
        openExePicker = chosenDir == null && !searchingManorLordsProcess
    }

    fun userInputFindFromRunningGameInstance() {
        if (openExePicker || searchingManorLordsProcess) return
        traverseRunningInstanceNotFound = false
        searchingManorLordsProcess = true
        modManagerScreenState.coroutineScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    queryProcessByExecName("ManorLords-Win64-Shipping.exe")
                        .ifEmpty { queryProcessByExecName("ManorLords-WinGDK-Shipping.exe") }
                        .ifEmpty { null }
                        ?.let {
                            modManagerScreenState.coroutineUIPublication {
                                chosenDir = jFile(it.first().path)
                                filePick(chosenDir)
                            }
                        }
                        ?: run {
                            traverseRunningInstanceNotFound = true
                        }
                }
            } finally {
                searchingManorLordsProcess = false
            }
        }
    }

    fun allowUserFileInput(): Boolean {
        return !openExePicker && !searchingManorLordsProcess
    }

    private class WindowProcessInfo(
        val name: String,
        val path: String,
        val id: Long,
    )

    private fun queryProcessByExecName(name: String): List<WindowProcessInfo> {
        val result = mutableListOf<WindowProcessInfo>()
        jProcessHandle.allProcesses().forEach { proc ->
            val exec = proc.info()?.command()?.getOrNull()
                ?: return@forEach
            val execName = exec.takeLastWhile { it != '\\' }
            if (execName != name) return@forEach
            val pid = proc.pid() ?: return@forEach
            result.add(
                WindowProcessInfo(
                    name = execName,
                    path = exec,
                    id = pid
                )
            )
        }
        return result
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleTooltip(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    TooltipArea(
        delayMillis = 300,
        modifier = modifier
            .pointerHoverIcon(PointerIcon.Default),
        tooltip = {
            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = 24.dp)
                    .background(Material3Theme.colorScheme.inverseSurface)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = text,
                    color = Material3Theme.colorScheme.inverseOnSurface,
                    style = Material3Theme.typography.labelMedium,
                    maxLines = maxLines
                )
            }
        }
    ) {
        content()
    }
}