package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

// TODO: add validation to definition and reference
sealed interface Schema {

    val description: String?
    val nullable: Boolean

    sealed interface PrimitiveSchema : Schema {

        val type: String
        val format: String?
        val defaultValue: String?

    }

    sealed interface EnumSchema : Schema {

        val type: String
        val format: String?
        val values: List<String>
        val defaultValue: String?

    }

    sealed interface ArraySchema : Schema {

        val itemSchema: Schema

    }

    sealed interface ObjectSchema : Schema {

        val properties: List<Pair<String, SchemaProperty>>

    }

    sealed interface OneOfSchema : Schema {

        val schemas: List<Schema>
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
sealed interface SchemaReference {

    val targetName: String

}
