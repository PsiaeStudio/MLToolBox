package dev.psiae.mltoolbox.composeui.modmanager.managemods.direct

import androidx.compose.runtime.*
import com.github.junrar.Junrar
import com.github.junrar.exception.RarException
import com.github.junrar.exception.UnsupportedRarV5Exception
import com.sun.nio.file.ExtendedOpenOption
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
import net.sf.sevenzipjbinding.ArchiveFormat
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.LinkOption
import java.nio.file.StandardOpenOption
import java.util.Comparator
import kotlin.io.path.*

@Composable
fun rememberDirectInstallUE4SSModScreenState(
    directInstallModScreenState: DirectInstallModScreenState
): DirectInstallUE4SSModScreenState {
    val composeUIContext = LocalComposeUIContext.current
    val state = remember(directInstallModScreenState) {
        DirectInstallUE4SSModScreenState(directInstallModScreenState, composeUIContext)
    }
    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }
    return state
}

class DirectInstallUE4SSModScreenState(
    val directInstallModScreenState: DirectInstallModScreenState,
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
        _coroutineScope = CoroutineScope(uiContext.dispatchContext.mainDispatcher.immediate)

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
                        type = PickerType.File(listOf("zip", "rar", "7z")),
                        mode = PickerMode.Multiple(),
                        title = "Select downloaded UE4SS Mod(s) archive (*.zip, *.rar, *.7z)",
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

    @OptIn(ExperimentalPathApi::class)
    private suspend fun processSelectedModsArchive(mods: List<jFile>): Boolean {
        isLoading = true
        isLastSelectedArchiveInvalid = false
        isInvalidModsDirectory = false
        isInstalledSuccessfully = false
        statusMessage = "awaiting IO worker ..."
        val gameBinaryFile = directInstallModScreenState.manageDirectModsScreenState.manageModsScreenState.modManagerScreenState.requireGameBinaryFile()
        withContext(Dispatchers.IO) {
            statusMessage = "preparing ..."

            val userDir = jFile(System.getProperty("user.dir"))
            val installDir = jFile(userDir.absolutePath + "\\temp\\ue4ss_mods_install")
            run {
                var lockedFile: jFile? = null
                if (installDir.exists() && !run {
                    var open = true
                    open = installDir.toPath().walk(PathWalkOption.INCLUDE_DIRECTORIES).all { f ->
                        if (f.isRegularFile(LinkOption.NOFOLLOW_LINKS)) {
                            var ch: FileChannel? = null
                            try {
                                ch = FileChannel.open(
                                    f,
                                    if (f.isWritable()) StandardOpenOption.WRITE else StandardOpenOption.READ,
                                    StandardOpenOption.READ,
                                    ExtendedOpenOption.NOSHARE_READ,
                                    ExtendedOpenOption.NOSHARE_WRITE,
                                    ExtendedOpenOption.NOSHARE_DELETE
                                )
                            } catch (ex: IOException) {
                                open = false
                                lockedFile = f.toFile()
                            } finally {
                                ch?.close()
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

            if (installDir.exists())
                installDir.toPath()
                    .walk(PathWalkOption.INCLUDE_DIRECTORIES)
                    .sortedWith(Comparator.reverseOrder())
                    .forEach { f ->
                        runCatching { f.deleteExisting() }
                            .onFailure { e ->
                                when (e) {
                                    is NoSuchFileException, is DirectoryNotEmptyException, is IOException -> {
                                        isLoading = false
                                        isInvalidModsDirectory = true
                                        statusMessage = "unable to delete ${f.toFile().let {
                                            it.absolutePath
                                                .drop(it.absolutePath.indexOf(userDir.absolutePath)+userDir.absolutePath.length)
                                                .replace(' ', '\u00A0')
                                        }} from app directory, it might be opened in another process"
                                        return@withContext
                                    }
                                }
                            }
                    }

            statusMessage = "extracting ..."
            mods.forEach { file ->
                val dest = jFile(System.getProperty("user.dir") + "\\temp\\ue4ss_mods_install\\${file.name}")
                if (file.extension.equals("rar", ignoreCase = true)) {
                    runCatching {
                        dest.mkdirs()
                        Junrar.extract(file, dest)
                    }.onFailure { junrarExtractException ->
                        when (junrarExtractException) {
                            is UnsupportedRarV5Exception -> {
                                runCatching {
                                    RandomAccessFile(file, "r").use { r ->
                                        RandomAccessFileInStream(r).use { sevenZipR ->
                                            SevenZip
                                                .openInArchive(ArchiveFormat.RAR5, sevenZipR)
                                                .use { inArchive ->
                                                    inArchive.simpleInterface.archiveItems.forEach { archiveItem ->
                                                        RandomAccessFile(jFile("${dest.absolutePath}\\${archiveItem.path}"), "rw").use {
                                                            archiveItem.extractSlow(RandomAccessFileOutStream(it))
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                }.onFailure { sevenZipExtractException ->
                                    if (sevenZipExtractException is SevenZipException) {
                                        isLoading = false
                                        isLastSelectedArchiveInvalid = true
                                        statusMessage = "unable to extract rar5 archive: ${file.name}"
                                        return@withContext
                                    } else if (sevenZipExtractException is IOException) {
                                        isLoading = false
                                        isLastSelectedArchiveInvalid = true
                                        statusMessage = "unable to extract rar5 archive: ${file.name} (IO ERROR)"
                                        return@withContext
                                    }
                                    throw sevenZipExtractException
                                }
                            }
                            is RarException -> {
                                isLoading = false
                                isLastSelectedArchiveInvalid = true
                                statusMessage = "unable to extract rar archive: ${file.name}"
                                return@withContext
                            }
                            is IOException -> {
                                isLoading = false
                                isLastSelectedArchiveInvalid = true
                                statusMessage = "unable to extract rar archive: ${file.name} (IO ERROR)"
                                return@withContext
                            }
                            else -> {
                                throw junrarExtractException
                            }
                        }
                    }
                } else if (file.extension.equals("7z", ignoreCase = true)) {
                    runCatching {
                        dest.mkdirs()
                        RandomAccessFile(file, "r").use { r ->
                            RandomAccessFileInStream(r).use { sevenZipR ->
                                SevenZip
                                    .openInArchive(ArchiveFormat.SEVEN_ZIP, sevenZipR)
                                    .use { inArchive ->
                                        inArchive.simpleInterface.archiveItems.forEach { archiveItem ->
                                            RandomAccessFile(jFile("${dest.absolutePath}\\${archiveItem.path}"), "rw").use {
                                                archiveItem.extractSlow(RandomAccessFileOutStream(it))
                                            }
                                        }
                                    }
                            }
                        }
                    }.onFailure { sevenZipExtractException ->
                        sevenZipExtractException.printStackTrace()
                        if (sevenZipExtractException is SevenZipException) {
                            isLoading = false
                            isLastSelectedArchiveInvalid = true
                            statusMessage = "unable to extract 7z archive: ${file.name}"
                            return@withContext
                        } else if (sevenZipExtractException is IOException) {
                            isLoading = false
                            isLastSelectedArchiveInvalid = true
                            statusMessage = "unable to extract 7z archive: ${file.name} (IO ERROR)"
                            return@withContext
                        }
                        throw sevenZipExtractException
                    }
                } else {
                    runCatching {
                        ZipFile(file).use { zipFile ->
                            zipFile.extractAll(dest.absolutePath)
                        }
                    }.onFailure { ex ->
                        when (ex) {
                            is RarException -> {
                                isLoading = false
                                isLastSelectedArchiveInvalid = true
                                statusMessage = "unable to extract zip archive: ${file.name}"
                                return@withContext
                            }
                            is IOException -> {
                                isLoading = false
                                isLastSelectedArchiveInvalid = true
                                statusMessage = "unable to extract zip archive: ${file.name} (IO ERROR)"
                                return@withContext
                            }
                            else -> {
                                throw ex
                            }
                        }
                    }
                }
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
            val gameDir = gameBinaryFile?.parentFile
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
                    dir.toPath().walk(PathWalkOption.INCLUDE_DIRECTORIES).forEach { f ->
                        if (f.isRegularFile(LinkOption.NOFOLLOW_LINKS)) {
                            var ch: FileChannel? = null
                            try {
                                ch = FileChannel.open(
                                    f,
                                    if (f.isWritable()) StandardOpenOption.WRITE else StandardOpenOption.READ,
                                    StandardOpenOption.READ,
                                    ExtendedOpenOption.NOSHARE_READ,
                                    ExtendedOpenOption.NOSHARE_WRITE,
                                    ExtendedOpenOption.NOSHARE_DELETE
                                )
                            } catch (ex: IOException) {
                                lockedFile = f.toFile()
                                dirOpen = false
                            } finally {
                                ch?.close()
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
                dir.toPath()
                    .walk(PathWalkOption.INCLUDE_DIRECTORIES)
                    .sortedWith(Comparator.reverseOrder())
                    .forEach { f ->
                        runCatching { f.deleteExisting() }
                            .onFailure { e ->
                                when (e) {
                                    is NoSuchFileException, is DirectoryNotEmptyException, is IOException -> {
                                        isLoading = false
                                        isInvalidModsDirectory = true
                                        statusMessage = "unable to delete ${f.toFile().let {
                                            it.absolutePath
                                                .drop(it.absolutePath.indexOf(gameDir.absolutePath)+gameDir.absolutePath.length)
                                                .replace(' ', '\u00A0')
                                        }} from game directory, it might be opened in another process"
                                    }
                                }
                            }
                    }
            }

            statusMessage = "installing ..."
            runCatching {
                listModsToBeInstalledDir.forEach {
                    it.toPath().copyToRecursively(
                        target = jFile("$modsDir\\${it.name}").toPath(),
                        followLinks = false,
                        overwrite = true
                    )
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