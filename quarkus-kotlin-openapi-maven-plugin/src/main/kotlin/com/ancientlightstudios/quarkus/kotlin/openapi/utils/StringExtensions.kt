package com.ancientlightstudios.quarkus.kotlin.openapi.utils

import java.util.*

private val camelCasePattern = Regex("([a-z])([A-Z])")
private val numberFollowedByLetterPattern = Regex("([0-9])([a-zA-Z])")
private val unwantedGroupPattern = Regex("[^a-zA-Z0-9]+")
private val snakeCasePattern = Regex("_([a-z0-9])")

fun String.toKotlinIdentifier(): String {
    if (isEmpty()) {
        return ""
    }

    // add an underscore into every combination of a lowercase letter followed by an uppercase letter
    val cleaned = this.replace(camelCasePattern, "$1_$2")
        // add an underscore into every combination of a number followed by a letter
        .replace(numberFollowedByLetterPattern, "$1_$2")
        // replace any sequence that is not a letter or a number with a single underscore
        .replace(unwantedGroupPattern, "_")
        // trim off any underscore at the start or end
        .trim('_')
        .lowercase()
        // replace any letter directly behind an underscore with its uppercase buddy
        // also remove underscores in front of numbers
        .replace(snakeCasePattern) { it.groupValues[1].first().titlecase(Locale.ENGLISH) }

    // if it does not start with a letter, prepend an underscore
    return if (cleaned[0].isLetter()) cleaned else "_$cleaned"
}