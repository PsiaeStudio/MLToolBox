package dev.psiae.mltoolbox.composeui.modmanager

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.java.jFile
import dev.psiae.mltoolbox.utilskt.removeSuffix
import kotlinx.coroutines.*
import java.io.RandomAccessFile
import kotlin.coroutines.coroutineContext

@Composable
fun rememberInstalledMostListState(
    modManagerScreenState: ModManagerScreenState
): InstalledModListState {
    val composeUIContext = LocalComposeUIContext.current
    val state = remember(modManagerScreenState) {
        InstalledModListState(modManagerScreenState, composeUIContext)
    }
    DisposableEffect(state) {
        modManagerScreenState.installedModListState = state
        state.stateEnter()
        onDispose { state.stateExit() }
    }
    return state
}

class InstalledModListState(
    val modManagerScreenState: ModManagerScreenState,
    val uiContext: ComposeUIContext
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
        val gameFile = modManagerScreenState.requireGameBinaryFile()
        var result: List<DirectInstalledModData> = emptyList()
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
    }

    private fun processToInstalledUE4SSModData(
        modsDir: jFile,
        mods: List<jFile>
    ): List<DirectInstalledModData> {
        val modsTxt = jFile("${modsDir.absolutePath}\\mods.txt")
        return try {
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
}

class DirectInstalledModData(
    val isUE4SS: Boolean,
    val isUnrealPak: Boolean,
    val name: String,
    val enabled: Boolean,
    val qualifiedNameType: String,
    // e.g. dev.psiae.manorlordsmods.mlconsolecommands
    val qualifiedName: String,
    val resolvedTypeTags: List<String>,
) {
    val uniqueQualifiedName = qualifiedNameType + "_" + qualifiedName
}

class DirectInstalledModFilterParams(
    val isU4SS: Boolean? = null,
    val isPak: Boolean? = null,
    val isBuiltIn: Boolean? = null,
    val hasUE4SSLua: Boolean? = null,
    val hasUE4SSCPP: Boolean? = null,
) {

    fun anyNonNullPropertyValue(): Boolean {
        return isU4SS != null || isPak != null || isBuiltIn != null || hasUE4SSLua != null || hasUE4SSCPP != null
    }
}