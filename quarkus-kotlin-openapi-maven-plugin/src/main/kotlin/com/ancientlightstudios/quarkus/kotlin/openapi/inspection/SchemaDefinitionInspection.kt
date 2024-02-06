package com.ancientlightstudios.quarkus.kotlin.openapi.inspection

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*

class SchemaDefinitionInspection(val schemaDefinition: TransformableSchemaDefinition) {

    inline fun <reified T : SchemaDefinitionComponent> components(block: ComponentInspection<T>.() -> Unit) {
        schemaDefinition.components.filterIsInstance<T>().forEach { ComponentInspection(it).block() }
    }

    fun nestedSchemas(block: SchemaUsageInspection.() -> Unit) {
        components<BaseDefinitionComponent> { SchemaUsageInspection(component.innerSchema).block() }
        components<ArrayComponent> { SchemaUsageInspection(component.itemsSchema).block() }
        components<AllOfComponent> { component.schemas.forEach { SchemaUsageInspection(it).block() } }
        components<AnyOfComponent> { component.schemas.forEach { SchemaUsageInspection(it).block() } }
        components<OneOfComponent> { component.schemas.forEach { SchemaUsageInspection(it).block() } }
        components<ObjectComponent> { component.properties.forEach { SchemaUsageInspection(it.schema).block() } }
    }

}

fun TransformableSchemaDefinition.inspect(block: SchemaDefinitionInspection.() -> Unit) =
    SchemaDefinitionInspection(this).block()