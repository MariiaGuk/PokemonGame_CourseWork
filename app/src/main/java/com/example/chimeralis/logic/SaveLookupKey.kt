package com.example.chimeralis.logic

/** Normalizes persisted identifiers so old formatting does not break loading. */
internal fun String.toSaveLookupKey(): String {
    return filter(Char::isLetterOrDigit).lowercase()
}
