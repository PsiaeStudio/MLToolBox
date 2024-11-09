package dev.psiae.mltoolbox.utilskt

fun runtimeError(message: String, cause: Throwable? = null): Nothing = throw RuntimeException(message, cause)
fun unsupportedOperationError(message: String, cause: Throwable? = null): Nothing = throw RuntimeException(message, cause)