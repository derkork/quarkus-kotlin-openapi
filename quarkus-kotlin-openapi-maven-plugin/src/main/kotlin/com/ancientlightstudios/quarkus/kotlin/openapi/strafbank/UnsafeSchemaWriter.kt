package com.ancientlightstudios.quarkus.kotlin.openapi.strafbank

import com.ancientlightstudios.quarkus.kotlin.openapi.GenerationContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaRef
import java.io.BufferedWriter

fun Schema.ObjectTypeSchema.writeUnsafe(context: GenerationContext, bufferedWriter: BufferedWriter) {
    bufferedWriter.writeUnsafeSchemaClass(this, context) {
        for (property in properties) {
            bufferedWriter.writeUnsafeProperty(property.name, property.type)
        }
    }
}


fun Schema.EnumSchema.writeUnsafe(context: GenerationContext, bufferedWriter: BufferedWriter) {
    bufferedWriter.writeln(
        """
        package ${context.modelPackage}
        
        @JvmInline
        value class ${this.toKotlinType(false, false)}(val value:String)
        """.trimIndent()
    )
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
    val propertyList = context.schemaRegistry.getPropertiesOf(schemas)
        .groupBy { it.name }

    for ((name, properties) in propertyList) {
        // check if we have the same type for all defined properties with this name
        check(properties.distinctBy { it.type }.size == 1) { "Property $name has multiple different types." }

        writeUnsafeProperty(name, properties.first().type)
    }
}

private fun BufferedWriter.writeUnsafeProperty(
    name: String,
    type: SchemaRef
) {
    writeln("@field:JsonProperty(value = \"$name\")")
    val resolvedType = type.resolve()
    // for enum types jackson may not be able to convert this to an enum if we receive string that is not
    // an enum value. therefore we use a string here.
    if (resolvedType is Schema.EnumSchema) {
        write("val ${name.toKotlinIdentifier()}: String?")
    } else {
        write(
            "val ${name.toKotlinIdentifier()}: ${
                resolvedType.toKotlinType(false, false)
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
        class ${schema.toKotlinType(false, false)}(
        """.trimIndent()
    )

    block()

    write(")")
}