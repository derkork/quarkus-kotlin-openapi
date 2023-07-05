package com.tallence.quarkus.kotlin.openapi.writer

import com.tallence.quarkus.kotlin.openapi.GenerationContext
import com.tallence.quarkus.kotlin.openapi.Schema
import java.io.BufferedWriter
import java.util.*

fun Schema.toKotlinType(safe: Boolean): String {
    return when (this.typeName) {
        "string" -> "String"
        "integer" -> "Int"
        "number" -> "Double"
        "boolean" -> "Boolean"
        "array" -> "List<Any>" // TODO
        else -> typeName.substringAfterLast("/").toKotlinClassName() + if (!safe) "Unsafe" else ""
    }
}

fun String.toKotlinClassName() : String {
    return this.toKotlinIdentifier()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
}

fun String.toKotlinIdentifier() : String {
    // replace anything that is not a letter or a number with an underscore
    val cleaned = this.replace(Regex("[^a-zA-Z0-9]"), "_")

    // if it does not start with a letter, prepend an underscore
    return if (cleaned[0].isLetter()) cleaned else "_$cleaned"
}

fun Schema.ComplexSchema.write(context: GenerationContext, bufferedWriter: BufferedWriter) {
    //language=kotlin
    bufferedWriter.write(
        """
        package ${context.modelPackage}
        
        class ${this.toKotlinType(!context.assumeInvalidInput)}(
        """.trimIndent()
    )

    // properties
    for ((index, property) in properties.withIndex()) {
        bufferedWriter.write("val ${property.name}: ${context.schemaRegistry.resolve(property.type).toKotlinType(!context.assumeInvalidInput)}")
        if (context.assumeInvalidInput) {
            bufferedWriter.write("?")
        }
        if (index < properties.size - 1) {
            bufferedWriter.writeln(", ")
        }
    }

    bufferedWriter.write(")")
}

fun Schema.EnumSchema.write(context: GenerationContext, bufferedWriter: BufferedWriter) {
    //language=kotlin
    bufferedWriter.writeln(
        """
        package ${context.modelPackage}
        
        enum class ${this.toKotlinType(!context.assumeInvalidInput)}(val value:String) {
        """.trimIndent()
    )

    // properties
    for (value in values) {
        val enumEntryName = value.toKotlinClassName()
        bufferedWriter.write("$enumEntryName(\"$value\")")
        if (value != values.last()) {
            bufferedWriter.write(", ")
        }
        bufferedWriter.writeln()
    }

    bufferedWriter.write("}")
}

fun Schema.OneOfSchema.write(context: GenerationContext, bufferedWriter: BufferedWriter) {
    // NOT yet supported
}

fun Schema.AnyOfSchema.write(context: GenerationContext, bufferedWriter: BufferedWriter) {
    // NOT yet supported
}

fun Schema.AllOfSchema.write(context: GenerationContext, bufferedWriter: BufferedWriter) {
    // NOT yet supported
}