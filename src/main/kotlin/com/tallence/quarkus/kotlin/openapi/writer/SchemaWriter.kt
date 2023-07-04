package com.tallence.quarkus.kotlin.openapi.writer

import com.tallence.quarkus.kotlin.openapi.GenerationContext
import com.tallence.quarkus.kotlin.openapi.Schema
import java.io.BufferedWriter

fun Schema.toKotlinType(safe: Boolean): String {
    return when (this.typeName) {
        "string" -> "String"
        "integer" -> "Int"
        "number" -> "Double"
        "boolean" -> "Boolean"
        "array" -> "List<Any>" // TODO
        else -> typeName + if (!safe) "Unsafe" else ""
    }
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