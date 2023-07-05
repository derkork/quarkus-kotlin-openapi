package com.tallence.quarkus.kotlin.openapi.builder

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.tallence.quarkus.kotlin.openapi.ApiSpec
import com.tallence.quarkus.kotlin.openapi.RequestMethod
import com.tallence.quarkus.kotlin.openapi.resolvePath

class ApiSpecBuilder(private val node: ObjectNode, private val schemaRegistry: SchemaRegistry) {

    fun build(requestFilter: RequestFilter): ApiSpec {
        val requests = buildRequests(requestFilter)
        buildRequiredSchemas()

        return ApiSpec(requests, schemaRegistry.resolvedSchemas)
    }

    private fun buildRequests(requestFilter: RequestFilter) =
        // get all paths
        node.with("paths")
            .fields().asSequence()
            .flatMap { (path, requests) ->
                // get all methods below each path
                requests.fields().asSequence()
                    // filter out the ones we need
                    .filter { (method, _) -> requestFilter.accept(path, RequestMethod.fromString(method)) }
                    // and parse them into a Request
                    .map { (method, request) ->
                        request.parseAsRequest(
                            path,
                            RequestMethod.fromString(method),
                            schemaRegistry
                        )
                    }
            }.toSet()

    private fun buildRequiredSchemas() {
        var queue = schemaRegistry.unresolved()
        do {
            queue.forEach {
                val schemaNode = node.resolvePath(it.id) ?: throw IllegalArgumentException("can't find schema for path ${it.id}")
                schemaNode.parseAsSchema(it.id, schemaRegistry)
            }
            queue = schemaRegistry.unresolved()
        } while (queue.isNotEmpty())
    }
}

fun JsonNode.parseAsApiSpec(schemaRegistry: SchemaRegistry, requestFilter: RequestFilter): ApiSpec {
    require(this.isObject) { "Json object expected" }

    return ApiSpecBuilder(this as ObjectNode, schemaRegistry).build(requestFilter)
}