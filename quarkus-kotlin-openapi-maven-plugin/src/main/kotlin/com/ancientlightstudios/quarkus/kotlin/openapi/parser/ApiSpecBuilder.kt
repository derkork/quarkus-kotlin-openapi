package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiVersion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestMethod
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

class ApiSpecBuilder(private val node: ObjectNode) {

    private val openApiVersion = extractOpenApiVersion()
    private val referenceResolver = ReferenceResolver(openApiVersion, node)

    fun build(requestFilter: RequestFilter): ApiSpec {
        val requests = extractRequests(requestFilter)
        val infoNode = node.with("info")
        return ApiSpec(requests, infoNode.getTextOrNull("description"), infoNode.getTextOrNull("version"))
    }

    private fun extractOpenApiVersion() = node.getTextOrNull("openapi")?.let { OpenApiVersion.fromString(it) }
        ?: throw IllegalStateException("OpenAPI version not specified")

    private fun extractRequests(requestFilter: RequestFilter) = node.with("paths")
        .propertiesAsList()
        .map { (path, pathNode) ->
            pathNode.asObjectNode { "Json object expected for path '$path'" }
                .extractPathRequests(path, requestFilter)
        }
        .flatten()

    private fun ObjectNode.extractPathRequests(path: String, requestFilter: RequestFilter): List<Request> {
        val context = ParseContext(openApiVersion, this, "#/paths/$path", referenceResolver)

        // a path can define default parameters for all of its operations
        val defaultParameter = withArray("parameters")
            .mapIndexed { index, itemNode ->
                context.contextFor(itemNode, "parameters[$index]")
                    .parseAsRequestParameter()
            }

        // now extract all defined operations in this path
        return propertiesAsList()
            // ignore the parameters key, as this is not a valid operation
            .filter { (name, _) -> name != "parameters" }
            // ignore all operations which are not required
            .filter { (operation, _) -> requestFilter.accept(path, RequestMethod.fromString(operation)) }
            .map { (operation, operationNode) ->
                context.contextFor(operationNode, "operation")
                    .parseAsRequest(path, RequestMethod.fromString(operation), defaultParameter)
            }
    }

}

fun JsonNode.parseAsApiSpec(requestFilter: RequestFilter) = asObjectNode { "Json object expected" }
    .let { ApiSpecBuilder(it).build(requestFilter) }
