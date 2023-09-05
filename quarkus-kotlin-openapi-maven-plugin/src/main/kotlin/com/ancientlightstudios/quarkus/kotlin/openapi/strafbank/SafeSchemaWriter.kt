package com.ancientlightstudios.quarkus.kotlin.openapi.strafbank

import com.ancientlightstudios.quarkus.kotlin.openapi.GenerationContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaRef
import java.io.BufferedWriter

fun Schema.ObjectTypeSchema.writeSafe(context: GenerationContext, bufferedWriter: BufferedWriter) {
    bufferedWriter.writeSafeSchemaClass(this, context) {
        for (property in properties) {
            bufferedWriter.writeSafeProperty(true, property.name, property.type)
        }
    }

}

fun Schema.EnumSchema.writeSafe(context: GenerationContext, bufferedWriter: BufferedWriter) {
    bufferedWriter.writeln(
        """
        package ${context.modelPackage}
        
        import com.fasterxml.jackson.annotation.JsonProperty
        
        enum class ${this.toKotlinType(true, true)}(val value:String) {
        """.trimIndent()
    )

    // properties
    for (value in values) {
        val enumEntryName = value.toKotlinClassName()
        bufferedWriter.writeln("@JsonProperty(\"$value\")")
        bufferedWriter.write("$enumEntryName(\"$value\")")
        if (value != values.last()) {
            bufferedWriter.write(", ")
        }
        bufferedWriter.writeln()
    }

    bufferedWriter.write("}")
}

fun Schema.OneOfSchema.writeSafe(context: GenerationContext, bufferedWriter: BufferedWriter) {
    bufferedWriter.writeSafeSchemaClass(this, context) {
        bufferedWriter.writeSafeSchemaPropertiesOf(oneOf, context)
    }
}

fun Schema.AnyOfSchema.writeSafe(context: GenerationContext, bufferedWriter: BufferedWriter) {
    bufferedWriter.writeSafeSchemaClass(this, context) {
        bufferedWriter.writeSafeSchemaPropertiesOf(anyOf, context)
    }
}

fun Schema.AllOfSchema.writeSafe(context: GenerationContext, bufferedWriter: BufferedWriter) {
    //language=kotlin
    bufferedWriter.writeSafeSchemaClass(this, context) {
        bufferedWriter.writeSafeSchemaPropertiesOf(allOf, context)
    }
}

fun BufferedWriter.writeSafeSchemaPropertiesOf(schemas: List<SchemaRef>, context: GenerationContext) {
    // first get all properties
    val propertyList = context.schemaRegistry.getPropertiesOf(schemas)
        .groupBy { it.name }

    // TODO: move this up into getPropertiesOf
    for ((name, properties) in propertyList) {
        // check if we have the same type for all defined properties with this name
        check(properties.distinctBy { it.type }.size == 1) { "Property $name has multiple different types." }

        writeSafeProperty(false, name, properties.first().type)
    }
}

private fun BufferedWriter.writeSafeProperty(
    isPublic: Boolean,
    name: String,
    type: SchemaRef
) {
    writeln("@field:JsonProperty(value = \"$name\")")
    if (!isPublic) {
        write("private ")
    }
    write(
        "val ${name.toKotlinIdentifier()}: ${
            type.resolve().toKotlinType(true, true)
        }"
    )
    writeln(", ")
}

fun BufferedWriter.writeSafeSchemaClass(schema: Schema, context: GenerationContext, block: () -> Unit) {
    write(
        """
        package ${context.modelPackage}
        
        import com.fasterxml.jackson.annotation.JsonProperty
        import io.quarkus.runtime.annotations.RegisterForReflection
        
        @RegisterForReflection
        class ${schema.toKotlinType(true, true)}(
        """.trimIndent()
    )

    block()

    write(")")
}