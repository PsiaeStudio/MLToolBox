package dev.psiae.mltoolbox.composeui.main

import dev.psiae.mltoolbox.utilskt.LazyConstructor

object NativeLibsInitializer {
    private val INIT = LazyConstructor<Unit>()

    fun init() = INIT.constructOrThrow(
        lazyValue = ::doInit,
        lazyThrow = ::alreadyInitErr
    )

    private fun doInit() {
        initSevenZip()
    }

    private fun alreadyInitErr(): Nothing = error("NativeLibInitializer already init")

    private fun initSevenZip() {
        SevenZipNativeLibInitializer.init()
    }
}