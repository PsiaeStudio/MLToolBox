package dev.psiae.mltoolbox.composeui.modmanager.manageplayset.direct

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.composeui.HeightSpacer
import dev.psiae.mltoolbox.composeui.WidthSpacer
import dev.psiae.mltoolbox.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.composeui.modmanager.SimpleTooltip
import dev.psiae.mltoolbox.composeui.modmanager.managemods.direct.DirectInstalledModData
import dev.psiae.mltoolbox.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.composeui.theme.md3.currentLocalAbsoluteOnSurfaceColor
import dev.psiae.mltoolbox.java.jFile
import dev.psiae.mltoolbox.uifoundation.themes.md3.MD3Theme
import dev.psiae.mltoolbox.utilskt.endsWithLineSeparator
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

@Composable
fun DirectModList(
    screenState: ManageDirectPlaysetScreenState
) {
    val state = rememberDirectModListState(screenState)
    Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Row(modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 4.dp, vertical = 8.dp)) { DirectModsQuerySearchBarPanel(state) }
        Box(modifier = Modifier.padding(vertical = 4.dp)) { DirectModListLazyColumn(state) }
    }
}

@Composable
private fun DirectModListLazyColumn(modListState: DirectModListState) {
    val scrollState = rememberLazyListState()
    Row {
        LazyColumn(
            modifier = Modifier
                .weight(1f, false)
                .fillMaxSize(),
            state = scrollState
        ) {
            val list = if (modListState.queryParamsEnabled)
                modListState.queriedInstalledModList
            else
                modListState.filteredInstalledModList
            itemsIndexed(
                key = { i, modData -> modData.uniqueQualifiedName },
                items = list
            ) { i, modData ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        run {
                            val enabled = false
                            SimpleTooltip(
                                "Drag to reorder (WIP)",
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .alpha(if (enabled) 1f else 0.38f)
                                        .size(24.dp),
                                    painter = painterResource("drawable/icon_draganddrop_strip_24px.png"),
                                    contentDescription = null,
                                    tint = Material3Theme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    WidthSpacer(8.dp)
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f, fill = false),
                            text = modData.name,
                            color = Material3Theme.colorScheme.onSurface,
                            style = Material3Theme.typography.bodyLarge
                                .copy(baselineShift = BaselineShift(-0.1f)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        WidthSpacer(8.dp)
                        var tagsRenderIndex = 0

                        if (modData.isUE4SS) {
                            if (tagsRenderIndex++ > 0)
                                WidthSpacer(4.dp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Material3Theme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "UE4SS",
                                    color = Material3Theme.colorScheme.onSurface,
                                    style = Material3Theme.typography.labelSmall
                                        .copy(baselineShift = BaselineShift(-0.1f)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        if (modData.isUnrealPak) {
                            if (tagsRenderIndex++ > 0)
                                WidthSpacer(4.dp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Material3Theme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "UE Pak",
                                    color = Material3Theme.colorScheme.onSurface,
                                    style = Material3Theme.typography.labelSmall
                                        .copy(baselineShift = BaselineShift(-0.1f)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (modData.isUnrealPak) {
                            SimpleTooltip(
                                "delete"
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .clickable { modListState.deleteMod(modData) }
                                        .padding(8.dp)
                                        .size(24.dp),
                                    painter = painterResource("drawable/icon_delete_trash_outline_24px.png"),
                                    tint = Material3Theme.colorScheme.onSurface,
                                    contentDescription = null
                                )
                            }
                        }
                        Checkbox(
                            checked = modData.enabled,
                            onCheckedChange = { checked -> modListState.toggleMod(modData) },
                            colors = CheckboxDefaults.colors(),
                            enabled = modData.isUE4SS
                        )
                        Icon(
                            modifier = Modifier
                                .alpha(0.38f)
                                .clickable(enabled = false) { }
                                .padding(8.dp)
                                .size(24.dp),
                            painter = painterResource("drawable/icon_more_24px.png"),
                            tint = Material3Theme.colorScheme.onSurface,
                            contentDescription = null
                        )
                    }
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
                .padding(start = 0.dp, end = 0.dp, top = 8.dp, bottom = 8.dp)
                .then(
                    if (scrollState.canScrollForward or scrollState.canScrollBackward)
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

@Composable
private fun DirectModsQuerySearchBarPanel(
    state: DirectModListState
) {
    val openFilter = remember {
        mutableStateOf(false)
    }
    val openSort = remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f, fill = false)) {
            DirectModsQuerySearchBar(state)
        }
        /*WidthSpacer(8.dp)
        run {
            val enabled = remember {
                mutableStateOf(false)
            }
            SimpleTooltip(
                "Filter (WIP)"
            ) {
                Row(
                    modifier = Modifier
                        .alpha(if (enabled.value) 1f else 0.38f)
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = enabled.value) {
                            openFilter.value = !openFilter.value
                        }
                        *//*.border(
                            width = 1.dp,
                            color = Color(0xFF79747E),
                            shape = RoundedCornerShape(50)
                        )*//*
                        *//*.padding(start = 12.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),*//*
                        .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp).padding(4.dp),
                        painter = painterResource("drawable/icon_filter_funnel_24px.png"),
                        contentDescription = null,
                        tint = if (state.filterParamsEnabled) Material3Theme.colorScheme.primary else Material3Theme.colorScheme.onSurface
                    )
                }
            }
        }
        run {
            val enabled = remember {
                mutableStateOf(false)
            }
            SimpleTooltip(
                "Sort (WIP)"
            ) {
                Row(
                    modifier = Modifier
                        .alpha(if (enabled.value) 1f else 0.38f)
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = enabled.value) {
                            openSort.value = !openSort.value
                        }
                        *//*.border(
                            width = 1.dp,
                            color = Color(0xFF79747E),
                            shape = RoundedCornerShape(50)
                        )*//*
                        *//*.padding(start = 12.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),*//*
                        .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource("drawable/icon_sort_list_centered_24px.png"),
                        contentDescription = null,
                        tint = if (state.sortParamsEnabled) Material3Theme.colorScheme.primary else Material3Theme.colorScheme.onSurface
                    )

                    *//*WidthSpacer(8.dp)

                    Text(
                        text = "Sort",
                        color = Color(252, 252, 252),
                        style = Material3Theme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false
                    )*//*
                }
            }
        }*/
    }
}

@Composable
private fun DirectModsQuerySearchBar(
    state: DirectModListState
) {
    val enabled by remember {
        mutableStateOf(true)
    }
    var active by remember {
        mutableStateOf(false)
    }
    val query by remember {
        derivedStateOf { state.lastQuery }
    }
    var isError by remember {
        mutableStateOf(false)
    }
    val ins = remember { MutableInteractionSource() }

    val surfaceColor = Material3Theme.colorScheme.surfaceContainerHigh
    val colors = TextFieldDefaults.colors(
        focusedContainerColor = surfaceColor,
        unfocusedContainerColor = surfaceColor,
        errorContainerColor = surfaceColor,
        errorIndicatorColor = Material3Theme.colorScheme.error,
    )
    Column {
        BasicTextField(
            modifier = Modifier
                .defaultMinSize(350.dp, 24.dp)
                .sizeIn(maxHeight = 35.dp),
            singleLine = true,
            maxLines = 1,
            value = query,
            onValueChange = { state.userInputChangeModListQuery(it) },
            interactionSource = ins,
            textStyle = Material3Theme.typography.labelMedium.copy(
                color = Material3Theme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            ),
            keyboardOptions = KeyboardOptions.Default,
            cursorBrush = SolidColor(Material3Theme.colorScheme.onSurface),
            visualTransformation = VisualTransformation.None,
            decorationBox = {
                @OptIn(ExperimentalMaterial3Api::class)
                (TextFieldDefaults.DecorationBox(
                    value = query,
                    innerTextField = {
                        Box(
                            modifier = Modifier
                                .sizeIn(350.dp),
                            content = { it() }
                        )
                    },
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = ins,
                    colors = colors,
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 4.dp),
                    container = {
                        val color by run {
                            val focused by ins.collectIsFocusedAsState()

                            val targetValue = with(colors) { when {
                                !enabled -> disabledContainerColor
                                isError -> errorContainerColor
                                focused -> focusedContainerColor
                                else -> unfocusedContainerColor
                            } }
                            key(surfaceColor) {
                                animateColorAsState(targetValue, tween(durationMillis = 150))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .then(
                                    if (LocalIsDarkTheme.current)
                                        Modifier.shadow(elevation = 2.dp, RoundedCornerShape(50))
                                    else
                                        Modifier.border(width = 1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(50))
                                )
                                .background(color)
                        )
                    },
                    label = null,
                    leadingIcon = {
                        Icon(
                            painter = painterResource("drawable/icon_search_16px.png"),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Material3Theme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = null,
                    shape = RoundedCornerShape(50),
                    placeholder = {
                        Box(
                            modifier = Modifier.defaultMinSize(350.dp)
                        ) {
                            if (query.isEmpty() and !ins.collectIsFocusedAsState().value) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterStart),
                                    text = "Search mod",
                                    color = Material3Theme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    style = Material3Theme.typography.labelLarge.copy(
                                        baselineShift = BaselineShift(-0.1f)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                ))
            },
        )
    }
}

@Composable
private fun rememberDirectModListState(
    screenState: ManageDirectPlaysetScreenState
): DirectModListState {
    val uiContext = LocalComposeUIContext.current
    val state = remember(screenState) {
        DirectModListState(screenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

private class DirectModListState(
    private val screenState: ManageDirectPlaysetScreenState,
    private val uiContext: ComposeUIContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var installedModList by mutableStateOf(
        listOf<DirectInstalledModData>(),
        neverEqualPolicy()
    )

    var filteredInstalledModList by mutableStateOf(
        listOf<DirectInstalledModData>(),
        neverEqualPolicy()
    )

    var queriedInstalledModList by mutableStateOf(
        listOf<DirectInstalledModData>(),
        neverEqualPolicy()
    )

    var filterParamsEnabled by mutableStateOf(false)
        private set

    var filterParamsKey by mutableStateOf(Any())
        private set

    var queryParamsEnabled by mutableStateOf(
        false
    )

    var queryParamsKey by mutableStateOf(Any())
        private set

    var sortParams by mutableStateOf<String>(
        "name_asc",
        neverEqualPolicy()
    )

    var sortParamsEnabled by mutableStateOf(false)
        private set

    var sortParamsKey by mutableStateOf(Any())
        private set

    fun stateEnter() {
        _coroutineScope = CoroutineScope(uiContext.dispatchContext.mainDispatcher.immediate)
        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }


    private var updateInstalledModList: Job? = null

    suspend fun refreshSuspend() {
        try { updateInstalledModList?.cancelAndJoin() } catch (_: CancellationException) {}
        coroutineContext.ensureActive()
        updateInstalledModList = coroutineScope.launch { doUpdateInstalledModList() }
        updateInstalledModList?.join()
    }

    private fun init() {
        coroutineScope.launch {
            while (true) {
                ensureActive()
                refreshSuspend()
                delay(1000)
            }
        }
    }

    private suspend fun doUpdateInstalledModList() {
        val gameFile = screenState.managePlaysetScreenState.modManagerScreenState.requireGameBinaryFile()
        var result: List<DirectInstalledModData>
        withContext(Dispatchers.IO) {
            val ue4ssMods: List<DirectInstalledModData> = run {
                val modsFolder = jFile("${gameFile.parent}\\ue4ss\\Mods\\")
                if (!modsFolder.exists()) {
                    return@run emptyList()
                } else if (!modsFolder.isDirectory) {
                    return@run emptyList()
                }
                val mods = modsFolder.listFiles()
                    ?.let { files ->
                        files
                            .filter {
                                it.isDirectory &&
                                        jFile("${it.absolutePath}\\dlls\\main.dll").exists() ||
                                        jFile("${it.absolutePath}\\Scripts\\main.lua").exists()
                            }.takeIf {
                                it.isNotEmpty()
                            }
                    }
                    ?: return@run emptyList()
                processToInstalledUE4SSModData(modsFolder, mods)
            }
            val pakMods: List<DirectInstalledModData> = run {
                val (unrealGameRoot, gameRoot) = resolveGameRoot(gameFile)
                val modsFolder = jFile("$gameRoot\\Content\\Paks\\~mods")
                if (!modsFolder.exists()) {
                    return@run emptyList()
                } else if (!modsFolder.isDirectory) {
                    return@run emptyList()
                }
                val mods = modsFolder.listFiles()
                    ?.let { files ->
                        files.filter {
                            it.isFile &&
                                    it.extension.equals("pak", ignoreCase = true)
                        }.takeIf {
                            it.isNotEmpty()
                        }
                    }
                    ?: return@run emptyList()
                processToInstalledPakModData(modsFolder, mods)
            }
            result = mutableListOf<DirectInstalledModData>()
                .apply {
                    addAll(ue4ssMods)
                    addAll(pakMods)
                }
        }
        this.installedModList = result
        this.filteredInstalledModList = this.installedModList
        if (queryParamsEnabled) {
            queryChange(lastQuery, reQuery = true)
        }
    }

    private fun processToInstalledUE4SSModData(
        modsDir: jFile,
        mods: List<jFile>
    ): List<DirectInstalledModData> {
        val modsTxt = jFile("${modsDir.absolutePath}\\mods.txt")
        return try {
            val occurrence = mapOf<String, Int>()

            mods.map { mod ->
                val modName = mod.name
                val enabled = jFile("${mod.absolutePath}\\enabled.txt").exists() ||
                        run {
                            // https://github.com/UE4SS-RE/RE-UE4SS/blob/8d3857273f12ce8c3800575dee537c5de9d690ef/UE4SS/src/UE4SSProgram.cpp#L1130
                            modsTxt.exists() && modsTxt.useLines { lineSequence ->
                                lineSequence.any { line ->
                                    var mutLine = line
                                    if (line.contains(';'))
                                        return@any false
                                    if (line.length <= 4)
                                        return@any false
                                    mutLine = mutLine.filterNot { it == ' ' }
                                    if (mutLine.isEmpty())
                                        return@any false
                                    val entryModName = mutLine.takeWhile { it != ':' }
                                    if (!entryModName.equals(modName, ignoreCase = true))
                                        return@any false
                                    val entryModEnabled = mutLine.takeLastWhile { it != ':' }
                                    entryModEnabled.isNotEmpty() && entryModEnabled[0] == '1'
                                }
                            }
                        }
                DirectInstalledModData(
                    isUE4SS = true,
                    isUnrealPak = false,
                    name = modName,
                    enabled = enabled,
                    qualifiedNameType = "direct",
                    qualifiedName = "direct.ue4ss.mods.$modName",
                    resolvedTypeTags = buildList {
                        add("ue4ss")
                    }
                )
            }
        } finally {
        }
    }

    private fun processToInstalledPakModData(
        modsDir: jFile,
        mods: List<jFile>
    ): List<DirectInstalledModData> {
        return try {
            mods.map { mod ->
                val modName = mod.nameWithoutExtension
                val enabled = true
                DirectInstalledModData(
                    isUE4SS = false,
                    isUnrealPak = true,
                    name = modName,
                    enabled = enabled,
                    qualifiedNameType = "direct",
                    qualifiedName = "direct.paks.$modName",
                    resolvedTypeTags = buildList {
                        add("pak")
                    }
                )
            }
        } finally {
        }
    }

    private fun resolveGameRoot(targetGameBinary: jFile): Pair<String, String> {
        return targetGameBinary.absolutePath
            .split("\\")
            .let { split ->
                if (split.size < 5) {
                    error("unable to find target game binary root directory, split size to small=${split.size}")
                }
                split.dropLast(4).joinToString("\\") to split.dropLast(3).joinToString("\\")
            }
    }

    fun toggleMod(
        mod: DirectInstalledModData
    ) {
        coroutineScope.launch {
            val gameBinary = screenState.managePlaysetScreenState.modManagerScreenState.requireGameBinaryFile()
            withContext(Dispatchers.IO) {
                if (mod.isUE4SS) {
                    toggleUE4SSModEnableFlag(
                        mod.name,
                        jFile("${gameBinary.parent}\\ue4ss\\Mods\\")
                    )
                }
            }
            refreshSuspend()
        }
    }

    fun deleteMod(
        mod: DirectInstalledModData
    ) {
        coroutineScope.launch {
            val gameFile = screenState.managePlaysetScreenState.modManagerScreenState.requireGameBinaryFile()
            withContext(Dispatchers.IO) {
                if (mod.isUnrealPak) {
                    deleteUnrealPakMod(gameFile, mod)
                }
            }
            refreshSuspend()
        }
    }

    fun userInputChangeModListQuery(query: String) {
        queryChange(query)
    }

    private fun toggleUE4SSModEnableFlag(
        modName: String,
        modsDir: jFile
    ) {
        val hasEnabledTxt = jFile("$modsDir\\${modName}\\enabled.txt").exists()
        jFile("$modsDir\\${modName}\\enabled.txt").delete()
        val modsTxt = jFile("${modsDir.absolutePath}\\mods.txt")
        var entryLine = -1
        var entryEnabled = hasEnabledTxt
        val lines = mutableListOf<String>()
        val hasModEntry = run {
            // https://github.com/UE4SS-RE/RE-UE4SS/blob/8d3857273f12ce8c3800575dee537c5de9d690ef/UE4SS/src/UE4SSProgram.cpp#L1130
            modsTxt.useLines { lineSequence ->
                var i = -1
                lines.addAll(lineSequence)
                lines.any { line ->
                    i++
                    var mutLine = line
                    if (line.contains(';'))
                        return@any false
                    if (line.length <= 4)
                        return@any false
                    mutLine = mutLine.filterNot { it == ' ' }
                    if (mutLine.isEmpty())
                        return@any false
                    val entryModName = mutLine.takeWhile { it != ':' }
                    entryModName.equals(modName, ignoreCase = true).also { isTheModEntry ->
                        if (isTheModEntry) {
                            entryLine = i
                            val entryModEnabledField = mutLine.takeLastWhile { it != ':' }
                            entryEnabled = hasEnabledTxt || entryModEnabledField.isNotEmpty() && entryModEnabledField[0] == '1'
                        }
                    }
                }
            }
        }
        if (hasModEntry) {
            lines[entryLine] = buildString {
                append(modName)
                append(' ')
                append(':')
                append(' ')
                append(if (entryEnabled) "0" else "1")
            }
            if (entryLine < lines.lastIndex) {
                // remove duplicates
                lines
                    .subList(entryLine+1, lines.size)
                    .removeAll { line ->
                        line
                            .dropWhile { it == ' ' }
                            .takeWhile { it != ':' }
                            .dropLastWhile { it == ' ' }
                            .equals(modName, ignoreCase = true)
                    }
            }
        } else {
            lines.add(0, buildString {
                append(modName)
                append(':')
                append(' ')
                append(if (entryEnabled) "0" else "1")
            })
        }
        with(modsTxt.bufferedWriter()) {
            lines.forEachIndexed { i, e ->
                write(e)
                if (i < lines.lastIndex)
                    newLine()
            }
            flush()
        }
    }

    private fun deleteUnrealPakMod(
        gameFile: jFile,
        mod: DirectInstalledModData
    ) {
        val (unrealGameRoot, gameRoot) = resolveGameRoot(gameFile)
        val modsFolder = jFile("$gameRoot\\Content\\Paks\\~mods")
        jFile("$modsFolder\\${mod.name}.pak").delete()
    }


    var lastQuery by mutableStateOf("")
        private set
    private var lastQueryTask: Job? = null
    private var lastQueryStamp = 0L
    fun queryChange(
        query: String,
        reQuery: Boolean = false
    ) {
        if (!reQuery && query.equals(lastQuery, ignoreCase = true)) {
            return
        }
        lastQuery = query
        lastQueryTask?.cancel()
        lastQueryTask = coroutineScope.launch {
            if (query.isNotEmpty()) {
                delay(100)
                val src = filteredInstalledModList
                val queried = withContext(Dispatchers.Default) {
                    src.filterIndexed { i, e ->
                        e.name.indexOf(query, ignoreCase = true) >= 0
                    }
                }
                ensureActive()
                queriedInstalledModList = queried
                queryParamsKey = Any()
                queryParamsEnabled = true
            } else {
                queriedInstalledModList = emptyList()
                queryParamsKey = Any()
                queryParamsEnabled = false
            }
        }
    }
}