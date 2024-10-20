package dev.psiae.mltoolbox.utilskt

fun String.removePrefix(
    prefix: String,
    ignoreCase: Boolean = false
) = if (startsWith(prefix, ignoreCase = ignoreCase)) drop(prefix.length) else this

fun String.removeSuffix(
    suffix: String,
    ignoreCase: Boolean = false
) = if (endsWith(suffix, ignoreCase = ignoreCase)) dropLast(suffix.length) else this

