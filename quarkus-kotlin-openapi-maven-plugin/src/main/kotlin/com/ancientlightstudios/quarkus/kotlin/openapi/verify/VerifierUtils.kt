package com.ancientlightstudios.quarkus.kotlin.openapi.verify

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema

fun ApiSpec.getAllSchemas(): Set<Schema> {
    val directSchemas = mutableSetOf<Schema>()

    requests.forEach { request ->
        request.body?.let { directSchemas.add(it.schema) }
        request.parameters.forEach { directSchemas.add(it.schema) }
        request.responses.forEach { (_, body) ->
            body.schema?.let { directSchemas.add(it) }
            body.headers.forEach { (_, header) -> directSchemas.add(header.schema) }
        }
    }

    return getAllSubSchemas(directSchemas)
}

private fun getAllSubSchemas(schemas: Set<Schema>): Set<Schema> {
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
            is Schema.OneOfSchema -> aSchema.schemas.keys.forEach { toProcess.add(it) }
            is Schema.AnyOfSchema -> aSchema.schemas.forEach { toProcess.add(it) }
            is Schema.AllOfSchema -> aSchema.schemas.forEach { toProcess.add(it) }
            else -> {}
        }
    }
    return done
}
