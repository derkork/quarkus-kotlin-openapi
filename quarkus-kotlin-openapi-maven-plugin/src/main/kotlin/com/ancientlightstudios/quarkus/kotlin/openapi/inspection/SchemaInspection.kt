package com.ancientlightstudios.quarkus.kotlin.openapi.inspection

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaComponent

class SchemaInspection(val schema: TransformableSchema) {

    inline fun <reified T : SchemaComponent> components(block: ComponentInspection<T>.() -> Unit) {
        schema.components.filterIsInstance<T>().forEach { ComponentInspection(it).block() }
    }

}

fun TransformableSchema.inspect(block: SchemaInspection.() -> Unit) =
    SchemaInspection(this).block()