package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

class NullableComponent(val nullable: Boolean) : SchemaComponent, MetaComponent {

    companion object {

        fun OpenApiSchema.nullableComponent(): NullableComponent? {
            val components = components.filterIsInstance<NullableComponent>()

            if (components.isEmpty()) {
                return null
            }

            // if there is a nullable component with the value true, it wins
            return NullableComponent(
                components.map { it.nullable }
                    .reduce { acc, cur -> acc && cur }
            )
        }
    }

}