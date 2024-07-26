package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema

class NullableComponent(val nullable: Boolean) : SchemaComponent, MetaComponent {

    companion object {

        fun TransformableSchema.nullableComponent(): NullableComponent? {
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