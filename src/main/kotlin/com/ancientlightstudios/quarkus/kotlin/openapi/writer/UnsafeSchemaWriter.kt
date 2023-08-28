package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import com.ancientlightstudios.quarkus.kotlin.openapi.GenerationContext
import com.ancientlightstudios.quarkus.kotlin.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.SchemaRef
import java.io.BufferedWriter

fun Schema.ComplexSchema.writeUnsafe(context: GenerationContext, bufferedWriter: BufferedWriter) {
    bufferedWriter.writeUnsafeSchemaClass(this, context) {
        for (property in properties) {
            bufferedWriter.writeUnsafeProperty(context, property.name, property.type)
        }
    }

}

fun Schema.EnumSchema.writeUnsafe(context: GenerationContext, bufferedWriter: BufferedWriter) {
    //language=kotlin
    writeSafe(context, bufferedWriter)
}

fun Schema.OneOfSchema.writeUnsafe(context: GenerationContext, bufferedWriter: BufferedWriter) {
    bufferedWriter.writeUnsafeSchemaClass(this, context) {
        bufferedWriter.writeUnsafeSchemaPropertiesOf(oneOf, context)
    }
}

fun Schema.AnyOfSchema.writeUnsafe(context: GenerationContext, bufferedWriter: BufferedWriter) {
    bufferedWriter.writeUnsafeSchemaClass(this, context) {
        bufferedWriter.writeUnsafeSchemaPropertiesOf(anyOf, context)
    }
}

fun Schema.AllOfSchema.writeUnsafe(context: GenerationContext, bufferedWriter: BufferedWriter) {
    //language=kotlin
    bufferedWriter.writeUnsafeSchemaClass(this, context) {
        bufferedWriter.writeUnsafeSchemaPropertiesOf(allOf, context)
    }
}

fun BufferedWriter.writeUnsafeSchemaPropertiesOf(schemas: List<SchemaRef>, context: GenerationContext) {
    // first get all properties
    val propertyList = schemas.map { context.schemaRegistry.resolve(it) }
        .filterIsInstance<Schema.ComplexSchema>()
        .flatMap { it.properties }
        .groupBy { it.name }

    for ((name, properties) in propertyList) {
        // check if we have the same type for all defined properties with this name
        check(properties.distinctBy { it.type }.size == 1) { "Property $name has multiple different types." }

        writeUnsafeProperty(context, name, properties.first().type)
    }
}

private fun BufferedWriter.writeUnsafeProperty(
    context: GenerationContext,
    name: String,
    type: SchemaRef
) {
    writeln("@field:JsonProperty(value = \"$name\")")
    val resolvedType = context.schemaRegistry.resolve(type)
    // for enum types jackson may not be able to convert this to an enum if we receive string that is not
    // an enum value. therefore we use a string here.
    if (resolvedType is Schema.EnumSchema) {
        write("val ${name.toKotlinIdentifier()}: String?")
    }
    else {
        write(
            "val ${name.toKotlinIdentifier()}: ${
                context.schemaRegistry.resolve(type).toKotlinType(false)
            }?"
        )
    }
    writeln(", ")
}

fun BufferedWriter.writeUnsafeSchemaClass(schema: Schema, context: GenerationContext, block: () -> Unit) {
    write(
        """
        package ${context.modelPackage}
        
        import com.fasterxml.jackson.annotation.JsonProperty
        import io.quarkus.runtime.annotations.RegisterForReflection
        
        @RegisterForReflection
        class ${schema.toKotlinType(false)}(
        """.trimIndent()
    )

    block()

    write(")")
}
