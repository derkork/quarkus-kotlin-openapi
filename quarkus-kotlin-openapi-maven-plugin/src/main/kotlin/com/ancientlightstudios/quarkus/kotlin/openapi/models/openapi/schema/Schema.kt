package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation

sealed interface Schema {
    /**
     * Path to the location in the API specification where this schema was defined.
     */
    val originPath:String
    val description: String?
    val nullable: Boolean
    val validations: List<Validation>

    sealed interface PrimitiveSchema : Schema {

        val type: String
        val format: String?
        val defaultValue: String?

    }

    sealed interface ArraySchema : Schema {

        val itemSchema: Schema

    }

    sealed interface ObjectSchema : Schema {

        val properties: List<Pair<String, SchemaProperty>>

    }

    sealed interface OneOfSchema : Schema {

        val schemas: Map<Schema, List<String>>
        val discriminator: String?
    }

    sealed interface AnyOfSchema : Schema {

        val schemas: List<Schema>

    }

    sealed interface AllOfSchema : Schema {

        val schemas: List<Schema>

    }

}

sealed interface SchemaDefinition
sealed interface SchemaReference<S : Schema> {

    val targetName: String
    val target: S

}
