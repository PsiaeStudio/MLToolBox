package dev.psiae.mltoolbox.core

import dev.psiae.mltoolbox.core.MainCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher

class CoreCoroutineDispatchers(
    val main: MainCoroutineDispatcher,
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val unconfined: CoroutineDispatcher
)