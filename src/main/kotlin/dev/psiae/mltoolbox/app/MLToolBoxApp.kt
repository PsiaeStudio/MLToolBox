package dev.psiae.mltoolbox.app

import dev.psiae.mltoolbox.modmanager.ModManager
import dev.psiae.mltoolbox.utilskt.LazyConstructor
import dev.psiae.mltoolbox.utilskt.valueOrNull

class MLToolBoxApp {
    val modManager = ModManager()

    companion object {
        private val INSTANCE = LazyConstructor<MLToolBoxApp>()

        fun construct() = INSTANCE.constructOrThrow(
            lazyValue = { MLToolBoxApp() },
            lazyThrow = { error("MLToolBoxApp already initialized") }
        )

        fun getInstance(): MLToolBoxApp = INSTANCE.valueOrNull()
            ?: error("MLToolBoxApp not initialized")
    }
}