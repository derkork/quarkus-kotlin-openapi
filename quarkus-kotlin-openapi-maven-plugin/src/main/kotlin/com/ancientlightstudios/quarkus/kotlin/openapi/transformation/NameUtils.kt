package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import java.util.*

private val camelCasePattern = Regex("([a-z])([A-Z])")
private val numberFollowedByLetterPattern = Regex("([0-9])([a-zA-Z])")
private val unwantedGroupPattern = Regex("[^a-zA-Z0-9]+")
private val snakeCasePattern = Regex("_([a-z0-9])")

// ---------------------------------------------------------------------------
// low level functions to convert a string into various formats
// ---------------------------------------------------------------------------
fun String.toSnakeCase(): String {
    if (isBlank()) {
        return ""
    }

    // add an underscore into every combination of a lowercase letter followed by an uppercase letter
    return this.replace(camelCasePattern, "$1_$2")
        // add an underscore into every combination of a number followed by a letter
        .replace(numberFollowedByLetterPattern, "$1_$2")
        // replace any sequence that is not a letter or a number with a single underscore
        .replace(unwantedGroupPattern, "_")
        // trim off any underscore at the start or end
        .trim('_')
        .lowercase()
}

fun String.toKebabCase() = toSnakeCase()
    .replace('_', '-')

fun String.toLowerCamelCase() = toSnakeCase()
    // replace any letter directly behind an underscore with its uppercase buddy
    // also remove underscores in front of numbers
    .replace(snakeCasePattern) { it.groupValues[1].capitalize() }

fun String.toUpperCamelCase() = toLowerCamelCase()
    .capitalize()

private fun String.capitalize() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }

// ---------------------------------------------------------------------------
// high level functions to create various identifiers
// ---------------------------------------------------------------------------
fun classNameOf(first: String, vararg additional: String): String {
    val result = listOf(first, *additional).joinToString(" ").toUpperCamelCase()
    // if it does not start with a letter, prepend an underscore
    return if (result[0].isLetter()) result else "_$result"
}

fun methodNameOf(first: String, vararg additional: String): String {
    val result = listOf(first, *additional).joinToString(" ").toLowerCamelCase()
    // if it does not start with a letter, prepend an underscore
    return if (result[0].isLetter()) result else "_$result"
}
