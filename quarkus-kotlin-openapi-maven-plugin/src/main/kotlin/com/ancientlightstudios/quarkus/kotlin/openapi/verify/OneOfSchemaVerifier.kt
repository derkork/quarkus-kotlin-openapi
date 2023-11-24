package com.ancientlightstudios.quarkus.kotlin.openapi.verify

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class OneOfSchemaVerifier : Verifier {

    override fun verify(apiSpec: ApiSpec) {
        // find all oneOf schemas, that have a discriminator
        val oneOfSchemasWithDiscriminators = apiSpec.getAllSchemas()
            .filterIsInstance<Schema.OneOfSchema>()
            .filter { it.discriminator != null }

        // if the oneOf schema has a discriminator, verify that the discriminator property is
        // present in all sub-schemas and that ALL sub-schemas are not inline schemas
        oneOfSchemasWithDiscriminators.forEach {
            // check that all sub-schemas have the discriminator property
            if (!guaranteedPropertyNames(it).contains(it.discriminator)) {
                SpecIssue("Schema ${it.originPath} is a oneOf schema with a discriminator, but not all sub-schemas have the discriminator property.")
            }
        }
    }

    private fun guaranteedPropertyNames(schema: Schema): Set<String> {
        return when (schema) {
            is Schema.ObjectSchema -> schema.properties.map { it.first }.toSet()
            is Schema.OneOfSchema -> schema.schemas.keys.map { guaranteedPropertyNames(it) }
                .reduce { acc, list -> acc.intersect(list) }

            is Schema.AnyOfSchema -> schema.schemas.map { guaranteedPropertyNames(it) }
                .reduce { acc, list -> acc.intersect(list) }

            is Schema.AllOfSchema -> schema.schemas.map { guaranteedPropertyNames(it) }
                .reduce { acc, list -> acc.union(list) }

            else -> emptySet()
        }
    }

}