package dev.psiae.mltoolbox.composeui.modmanager

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.java.jFile
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
        listOf<InstalledModData>(),
        neverEqualPolicy()
    )

    fun stateEnter() {
        _coroutineScope = CoroutineScope(uiContext.dispatchContext.mainDispatcher)

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
        var result: List<InstalledModData> = emptyList()
        withContext(Dispatchers.IO) {
            val modsFolder = jFile("${gameFile.parent}\\ue4ss\\Mods\\")
            if (!modsFolder.exists()) {
                TODO()
            } else if (!modsFolder.isDirectory) {
                TODO()
            }
            val mods = modsFolder.listFiles()
                ?.filter {
                    it.isDirectory &&
                            jFile("${it.absolutePath}\\dlls\\main.dll").exists() ||
                            jFile("${it.absolutePath}\\Scripts\\main.lua").exists()
                } ?: run {
                result = emptyList()
                return@withContext
            }
            result = processToInstalledModData(gameFile, modsFolder, mods)
        }
        this.installedModList = result
    }

    private fun processToInstalledModData(
        gameBinaryFile: jFile,
        modsDir: jFile,
        mods: List<jFile>
    ): List<InstalledModData> {
        val modsTxt = jFile("${modsDir.absolutePath}\\mods.txt")
        var modsTxtCh: RandomAccessFile? = null
        return try {
            if (modsTxt.exists()) {
                modsTxtCh = RandomAccessFile(modsTxt, "r")
            }
            mods.map { mod ->
                val modName = mod.name
                val enabled = jFile("${mod.absolutePath}\\enabled.txt").exists() ||
                    run {
                        // https://github.com/UE4SS-RE/RE-UE4SS/blob/8d3857273f12ce8c3800575dee537c5de9d690ef/UE4SS/src/UE4SSProgram.cpp#L1130
                        modsTxt.useLines { lineSequence ->
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
                InstalledModData(
                    gameBinaryFilePath = gameBinaryFile.absolutePath,
                    relativePath = mod.absolutePath
                        .drop(mod.absolutePath.indexOf(gameBinaryFile.absolutePath)+gameBinaryFile.absolutePath.length),
                    name = modName,
                    enabled = enabled
                )
            }
        } finally {
            modsTxtCh?.close()
        }
    }
}

class InstalledModData(
    val gameBinaryFilePath: String,
    val relativePath: String,
    val name: String,
    val enabled: Boolean,
    // e.g. dev.psiae.manorlordsmods.mlconsolecommands
    val qualifiedName: String = name
)