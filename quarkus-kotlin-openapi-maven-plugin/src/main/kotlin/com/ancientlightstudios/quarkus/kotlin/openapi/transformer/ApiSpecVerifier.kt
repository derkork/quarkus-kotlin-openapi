package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.SchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class ApiSpecVerifier(val apiSpec: ApiSpec) {

    fun verify() {
        verifyOneOfSchemas()
    }

    private fun verifyOneOfSchemas() {
        val directSchemas = mutableSetOf<Schema>()

        apiSpec.requests.forEach { request ->
            request.body?.let { directSchemas.add(it.schema) }
            request.parameters.forEach { directSchemas.add(it.schema) }
            request.responses.forEach { (_, body) ->
                body.schema?.let { directSchemas.add(it) }
                body.headers.forEach { (_, header) -> directSchemas.add(header.schema) }
            }
        }

        val allSchemas = getAllSubSchemas(directSchemas)

        // find all oneOf schemas, that have a discriminator
        val oneOfSchemasWithDiscriminators =
            allSchemas.filterIsInstance<Schema.OneOfSchema>().filter { it.discriminator != null }

        // if the oneOf schema has a discriminator, verify that the discriminator property is
        // present in all sub-schemas and that ALL sub-schemas are not inline schemas
        oneOfSchemasWithDiscriminators.forEach {
            // check that we have no inline schemas
            if (it.schemas.any { subSchema -> subSchema is SchemaDefinition }) {
                SpecIssue("Schema ${it.originPath} is a oneOf schema with a discriminator, but has inline schemas as sub-schemas. This is not supported.")
            }

            // check that all sub-schemas have the discriminator property
            if (!guaranteedPropertyNames(it).contains(it.discriminator)) {
                SpecIssue("Schema ${it.originPath} is a oneOf schema with a discriminator, but not all sub-schemas have the discriminator property.")
            }
        }
    }

    private fun guaranteedPropertyNames(schema: Schema): Set<String> {
        return when (schema) {
            is Schema.ObjectSchema -> schema.properties.map { it.first }.toSet()
            is Schema.OneOfSchema -> schema.schemas.map { guaranteedPropertyNames(it) }
                .reduce { acc, list -> acc.intersect(list) }

            is Schema.AnyOfSchema -> schema.schemas.map { guaranteedPropertyNames(it) }
                .reduce { acc, list -> acc.intersect(list) }

            is Schema.AllOfSchema -> schema.schemas.map { guaranteedPropertyNames(it) }
                .reduce { acc, list -> acc.union(list) }

            else -> emptySet()
        }
    }

    private fun getAllSubSchemas(schemas:Set<Schema>) : Set<Schema> {
        val done = mutableSetOf<Schema>()
        val toProcess = ArrayDeque<Schema>(schemas)

        while (toProcess.size > 0) {
            val aSchema = toProcess.removeFirst()
            if (!done.add(aSchema)) {
                continue
            }

            when (aSchema) {
                is Schema.ObjectSchema -> aSchema.properties.forEach { toProcess.add(it.second.schema) }
                is Schema.ArraySchema -> toProcess.add(aSchema.itemSchema)
                is Schema.OneOfSchema -> aSchema.schemas.forEach { toProcess.add(it) }
                is Schema.AnyOfSchema -> aSchema.schemas.forEach { toProcess.add(it) }
                is Schema.AllOfSchema -> aSchema.schemas.forEach { toProcess.add(it) }
                else -> {}
            }
        }
        return done
    }
}