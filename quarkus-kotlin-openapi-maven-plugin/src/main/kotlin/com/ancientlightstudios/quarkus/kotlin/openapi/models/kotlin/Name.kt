package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import java.util.*

class ClassName private constructor(val name: String) {

    override fun toString() = "ClassName($name)"

    companion object {

        fun String.rawClassName() = ClassName(this)

        fun String.className() = ClassName(this.toKotlinIdentifier()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() })
    }

}

class VariableName private constructor(val name: String) {

    override fun toString() = "VariableName($name)"

    companion object {
        fun String.rawVariableName() = VariableName(this)

        fun String.variableName() = VariableName(this.toKotlinIdentifier())
    }

}

class MethodName private constructor(val name: String) {

    override fun toString() = "VariableName($name)"

    companion object {
        fun String.rawMethodName() = MethodName(this)

        fun String.methodName() = MethodName(this.toKotlinIdentifier())
    }

}

private val camelCasePattern = Regex("([a-z])([A-Z])")
private val unwantedGroupPattern = Regex("[^a-zA-Z0-9]+")
private val snakeCasePattern = Regex("_([a-z])")

private fun String.toKotlinIdentifier(): String {
    // add an underscore into every combination of a lowercase letter followed by an uppercase letter
    val cleaned = this.replace(camelCasePattern, "$1_$2")
        // replace any sequence that is not a letter or a number with a single underscore
        .replace(unwantedGroupPattern, "_")
        // trim off any underscore at the start or end
        .trim('_')
        .lowercase()
        // replace any letter directly behind an underscore with its uppercase buddy
        .replace(snakeCasePattern) { it.groupValues[1].first().titlecase(Locale.ENGLISH) }

    // if it does not start with a letter, prepend an underscore
    return if (cleaned[0].isLetter()) cleaned else "_$cleaned"
}