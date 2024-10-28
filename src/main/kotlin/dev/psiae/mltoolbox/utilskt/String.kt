package dev.psiae.mltoolbox.utilskt

fun String.removePrefix(
    prefix: String,
    ignoreCase: Boolean = false
) = if (startsWith(prefix, ignoreCase = ignoreCase)) drop(prefix.length) else this

fun String.removeSuffix(
    suffix: String,
    ignoreCase: Boolean = false
) = if (endsWith(suffix, ignoreCase = ignoreCase)) dropLast(suffix.length) else this

fun String.endsWithLineSeparator(): Boolean {
    // empty
    if (isEmpty())
        return false
    // Unix and Windows
    if (endsWith("\n"))
        return true
    // Mac
    if (endsWith("\r"))
        return true
    return false
}

