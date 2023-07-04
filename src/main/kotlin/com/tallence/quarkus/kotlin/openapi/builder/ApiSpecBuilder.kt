package com.tallence.quarkus.kotlin.openapi.builder

import com.tallence.quarkus.kotlin.openapi.ApiSpec
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

class ApiSpecBuilder(private val node: ObjectNode) {

    fun build(): ApiSpec {
        val schemaRegistry = SchemaRegistry()

        val requests = buildRequests(schemaRegistry)
        val schemas = buildSchemas(schemaRegistry)

        schemaRegistry.validate()

        return ApiSpec(requests, schemas)
    }

    private fun buildRequests(schemaRegistry: SchemaRegistry) =
        node.with("paths")
            .fields().asSequence()
            .flatMap { (path, requests) ->
                requests.fields().asSequence()
                    .map { (method, request) -> request.parseAsRequest(path, method, schemaRegistry) }
            }.toSet()

    private fun buildSchemas(schemaRegistry: SchemaRegistry) = node.with("components")
        .with("schemas")
        .fields().asSequence()
        .map { (typeName, schemaNode) ->
            schemaNode.parseAsSchema(typeName, schemaRegistry)
        }
        .onEach {
            schemaRegistry.resolveRef("#/components/schemas/${it.typeName}", it)
        }
        .toSet()
}

fun JsonNode.parseAsApiSpec(): ApiSpec {
    if (!this.isObject) {
        throw IllegalArgumentException("Json object expected")
    }

    return ApiSpecBuilder(this as ObjectNode).build()
}