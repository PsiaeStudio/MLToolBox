package dev.psiae.mltoolbox.composeui.modmanager

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.java.jFile
import dev.psiae.mltoolbox.utilskt.isNullOrNotActive
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.*
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile

@Composable
fun rememberInstallUE4SSModState(
    modManagerScreenState: ModManagerScreenState
): InstallUE4SSModState {
    val composeUIContext = LocalComposeUIContext.current
    val state = remember(modManagerScreenState) {
        InstallUE4SSModState(modManagerScreenState, composeUIContext)
    }
    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }
    return state
}

class InstallUE4SSModState(
    val modManagerScreenState: ModManagerScreenState,
    val uiContext: ComposeUIContext
) {
    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    private var pickUE4SSModsArchiveCompletion: Deferred<List<jFile>?>? = null

    var selectedUE4SSModsArchive by mutableStateOf<List<jFile>?>(null)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isInstalledSuccessfully by mutableStateOf(false)
        private set

    var isLastSelectedArchiveInvalid by mutableStateOf(false)
        private set

    var isInvalidModsDirectory by mutableStateOf(false)
        private set

    fun stateEnter() {
        _coroutineScope = CoroutineScope(uiContext.dispatchContext.mainDispatcher)

        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    private fun init() {

    }

    fun pickUE4SSArchive(awtWindow: java.awt.Window) {
        coroutineScope.launch {
            if (pickUE4SSModsArchiveCompletion?.isActive != true) {
                isLastSelectedArchiveInvalid = false
                isInvalidModsDirectory = false
                isInstalledSuccessfully = false
                statusMessage = null
                async {
                    val pick = FileKit.pickFile(
                        type = PickerType.File(listOf("zip")),
                        mode = PickerMode.Multiple(),
                        title = "Select downloaded UE4SS Mod(s) archive (*.zip)",
                        initialDirectory = "C:",
                        platformSettings = FileKitPlatformSettings(
                            parentWindow = awtWindow
                        )
                    )
                    if (pick == null) {
                        return@async null
                    }
                    if (!processSelectedModsArchive(pick.map { it.file })) {
                        return@async null
                    }
                    pick.map { it.file }
                }.also {
                    pickUE4SSModsArchiveCompletion = it
                    it.await()
                }
            }
        }
    }

    fun userDropUE4SSModsArchive(
        mods: List<jFile>
    ) {
        if (mods.isEmpty()) return
        coroutineScope.launch {
            if (pickUE4SSModsArchiveCompletion.isNullOrNotActive()) {
                async {
                    processSelectedModsArchive(mods)
                    mods
                }.also {
                    pickUE4SSModsArchiveCompletion = it
                    it.await()
                }
            }
        }
    }

    private suspend fun processSelectedModsArchive(mods: List<jFile>): Boolean {
        isLoading = true
        isLastSelectedArchiveInvalid = false
        isInvalidModsDirectory = false
        isInstalledSuccessfully = false
        statusMessage = "awaiting IO worker ..."
        withContext(Dispatchers.IO) {
            statusMessage = "preparing ..."

            val userDir = jFile(System.getProperty("user.dir"))
            val installDir = jFile(userDir.absolutePath + "\\temp\\ue4ss_mods_install")
            run {
                var lockedFile: jFile? = null
                if (installDir.exists() && !run {
                    var open = true
                    open = installDir.walkBottomUp().all { f ->
                        if (f.isFile) {
                            if (f.canWrite()) {
                                var ch: RandomAccessFile? = null
                                try {
                                    ch = RandomAccessFile(f, "rw")
                                    ch.channel.lock()
                                } catch (ex: IOException) {
                                    open = false
                                    lockedFile = f
                                } finally {
                                    ch?.close()
                                }
                            } else {
                                // TODO
                            }
                        }
                        open
                    }
                    open
                }) {
                    isLoading = false
                    isInvalidModsDirectory = true
                    statusMessage = "unable to lock ue4ss_mods_install directory from app directory, ${lockedFile?.let {
                        it.absolutePath
                            .drop(it.absolutePath.indexOf(userDir.absolutePath)+userDir.absolutePath.length)
                            .replace(' ', '\u00A0')
                    }} might be opened in another process"
                    return@withContext
                }
            }
            if (installDir.exists()) installDir.walkBottomUp().forEach { f ->
                if (!f.delete()) {
                    isLoading = false
                    isInvalidModsDirectory = true
                    statusMessage = "unable to delete ${f.let {
                        it.absolutePath
                            .drop(it.absolutePath.indexOf(userDir.absolutePath)+userDir.absolutePath.length)
                            .replace(' ', '\u00A0')
                    }} from app directory, it might be opened in another process"
                    return@withContext
                }
            }

            statusMessage = "extracting ..."
            runCatching {
                mods.forEach { file ->
                    ZipFile(file).use { zipFile ->
                        zipFile.extractAll(System.getProperty("user.dir") + "\\temp\\ue4ss_mods_install\\${zipFile.file.name}")
                    }
                }
            }.onFailure { ex ->
                if (ex is ZipException) {
                    isLoading = false
                    isLastSelectedArchiveInvalid = true
                    statusMessage = "unable to extract archive"
                    return@withContext
                }
                throw ex
            }

            statusMessage = "verifying mods ..."
            val modsInstallDir = System.getProperty("user.dir") + "\\temp\\ue4ss_mods_install"
            val listModsToBeInstalledArchiveDir = jFile(modsInstallDir).listFiles { dir, name ->
                return@listFiles jFile("$dir\\$name").isDirectory
            }
            val listModsToBeInstalledDir = mutableListOf<jFile>()
            if (listModsToBeInstalledArchiveDir == null) {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "error listing ue4ss_mods_install"
                return@withContext
            }
            listModsToBeInstalledArchiveDir.ifEmpty {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "ue4ss_mods_install does not contain any folder"
                return@withContext
            }.forEach { archiveFile ->
                val listFiles = archiveFile.listFiles()
                if (listFiles == null) {
                    isLoading = false
                    isLastSelectedArchiveInvalid = true
                    statusMessage = "error listing ue4ss_mods_install\\${archiveFile.name}"
                    return@withContext
                }
                if (listFiles.isEmpty() || listFiles.size > 1 || !listFiles.first().isDirectory) {
                    isLoading = false
                    isLastSelectedArchiveInvalid = true
                    statusMessage = "${archiveFile.name} must only contain one root directory"
                    return@withContext
                }
                val file = listFiles.first()
                listModsToBeInstalledDir.add(file)
                val dllsMain = jFile("$file\\dlls\\main.dll")
                if (dllsMain.exists())
                    return@forEach
                val scriptsMain = jFile("$file\\Scripts\\main.lua")
                if (scriptsMain.exists())
                    return@forEach
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "${file.name} is missing entry point (dlls\\main.dll or Scripts\\main.lua)"
                return@withContext
            }


            statusMessage = "verifying target game dir ..."
            val gameDir = modManagerScreenState.gameBinaryFile?.parentFile
            if (gameDir == null || !gameDir.exists()) {
                isLoading = false
                isInvalidModsDirectory = true
                statusMessage = "missing game directory"
                return@withContext
            }

            val ue4ssDir = jFile("$gameDir\\ue4ss")
            if (!ue4ssDir.exists() || !ue4ssDir.isDirectory) {
                isLoading = false
                isInvalidModsDirectory = true
                statusMessage = "missing ${gameDir.name}\\ue4ss directory"
                return@withContext
            }

            statusMessage = "verifying target game Mods dir ..."

            val modsDir = jFile("$ue4ssDir\\Mods")
            if (!modsDir.exists() || !modsDir.isDirectory) {
                isLoading = false
                isInvalidModsDirectory = true
                statusMessage = "missing ${gameDir.name}\\ue4ss\\Mods directory"
                return@withContext
            }

            val existingModDirectories = modsDir.listFiles { dir, name ->
                return@listFiles jFile("$dir\\$name").isDirectory
            }

            if (existingModDirectories == null) {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "error listing $modsDir"
                return@withContext
            }

            val directoriesToOverwrite = existingModDirectories.filter { dir ->
                listModsToBeInstalledDir.any { it.name == dir.name }
            }

            var lockedFile: jFile? = null
            if (!run {
                var open = true
                open = directoriesToOverwrite.all { dir ->
                    var dirOpen = true
                    dir.walkBottomUp().forEach { f ->
                        if (f.isFile) {
                            if (f.canWrite()) {
                                var ch: RandomAccessFile? = null
                                try {
                                    ch = RandomAccessFile(f, "rw")
                                    ch!!.channel.lock()
                                } catch (ex: IOException) {
                                    lockedFile = f
                                    dirOpen = false
                                } finally {
                                    ch?.close()
                                }
                            } else {
                                // TODO
                            }
                        }
                    }
                    dirOpen
                }
                open
            }) {
                isLoading = false
                isInvalidModsDirectory = true
                statusMessage = "unable to lock ${lockedFile!!.let {
                    it.absolutePath
                        .drop(it.absolutePath.indexOf(gameDir.absolutePath)+gameDir.absolutePath.length)
                        .replace(' ', '\u00A0')
                }} from game directory, it might be opened in another process"
                return@withContext
            }

            statusMessage = "preparing target game Mods dir ..."

            directoriesToOverwrite.forEach { dir ->
                dir.walkBottomUp().forEach { f ->
                    if (!f.delete()) {
                        isLoading = false
                        isInvalidModsDirectory = true
                        statusMessage = "unable to delete ${f.let {
                            it.absolutePath
                                .drop(it.absolutePath.indexOf(gameDir.absolutePath)+gameDir.absolutePath.length)
                                .replace(' ', '\u00A0')
                        }} from game directory, it might be opened in another process"
                        return@withContext
                    }
                }
            }

            statusMessage = "installing ..."
            runCatching {
                listModsToBeInstalledDir.forEach {
                    it.copyRecursively(jFile("$modsDir\\${it.name}"), true)
                }
            }.onFailure { ex ->
                isLoading = false
                isInvalidModsDirectory = true
                statusMessage = when (ex) {
                    is FileNotFoundException -> {
                        "unable to copy recursively, source file is missing"
                    }
                    is FileAlreadyExistsException -> {
                        "unable to copy recursively, target file is not writeable"
                    }
                    is AccessDeniedException -> {
                        "unable to copy recursively, access denied"
                    }
                    is IOException -> {
                        "unable to copy recursively, IO error"
                    }
                    else -> throw ex
                }
                return@withContext
            }

            isLoading = false
            isInstalledSuccessfully = true
        }
        return false
    }
}