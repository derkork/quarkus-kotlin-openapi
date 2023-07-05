package com.tallence.quarkus.kotlin.openapi.builder

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.tallence.quarkus.kotlin.openapi.ApiSpec
import com.tallence.quarkus.kotlin.openapi.RequestMethod
import com.tallence.quarkus.kotlin.openapi.Schema
import com.tallence.quarkus.kotlin.openapi.resolvePath

class ApiSpecBuilder(private val node: ObjectNode, private val schemaRegistry: SchemaRegistry) {

    fun build(requestFilter: RequestFilter): ApiSpec {
        val requests = buildRequests(requestFilter)
        val schemas = buildSchemas()

        return ApiSpec(requests, schemas)
    }

    private fun buildRequests(requestFilter: RequestFilter) =
        node.with("paths")
            .fields().asSequence()
            .flatMap { (path, requests) ->
                requests.fields().asSequence()
                    .filter { (method, _) -> requestFilter.accept(path, RequestMethod.fromString(method)) }
                    .map { (method, request) ->
                        request.parseAsRequest(
                            path,
                            RequestMethod.fromString(method),
                            schemaRegistry
                        )
                    }
            }.toSet()

    private fun buildSchemas(): Set<Schema> {
        var queue = schemaRegistry.unresolved()
        val result = mutableSetOf<Schema>()
        do {
            queue.mapTo(result) {
                val nodeName = it.id.substringAfterLast("/")
                val schemaNode = node.resolvePath(it.id) ?: throw IllegalArgumentException("can't find schema for path ${it.id}")

                val schema = schemaNode.parseAsSchema(nodeName, schemaRegistry)
                schemaRegistry.resolveRef(it.id, schema)
                schema
            }
            queue = schemaRegistry.unresolved()
        } while (queue.isNotEmpty())
        return result
    }
}

fun JsonNode.parseAsApiSpec(schemaRegistry: SchemaRegistry, requestFilter: RequestFilter): ApiSpec {
    require(this.isObject) { "Json object expected" }

    return ApiSpecBuilder(this as ObjectNode, schemaRegistry).build(requestFilter)
}