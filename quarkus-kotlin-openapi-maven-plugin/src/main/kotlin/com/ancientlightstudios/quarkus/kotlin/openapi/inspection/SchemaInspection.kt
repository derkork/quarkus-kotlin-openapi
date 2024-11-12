package com.ancientlightstudios.quarkus.kotlin.openapi.inspection

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SchemaComponent

class SchemaInspection(val schema: OpenApiSchema) {

    inline fun <reified T : SchemaComponent> components(block: ComponentInspection<T>.() -> Unit) {
        schema.components.filterIsInstance<T>().forEach { ComponentInspection(it).block() }
    }

}

fun OpenApiSchema.inspect(block: SchemaInspection.() -> Unit) =
    SchemaInspection(this).block()