package com.example.chimeralis.logic

/**
 * Normalizes persisted identifiers so old formatting does not break loading.
 *
 * @receiver The string receiver used by this operation.
 * @return The text value produced by this operation.
 */
internal fun String.toSaveLookupKey(): String {
    return filter(Char::isLetterOrDigit).lowercase()
}
