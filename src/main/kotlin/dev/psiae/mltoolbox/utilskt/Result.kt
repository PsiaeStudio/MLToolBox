package dev.psiae.mltoolbox.utilskt

// rethrow non Exception, which are not recoverable
inline fun <T> Result<T>.rethrowNonException() = onFailure { if (it !is Exception) throw it }