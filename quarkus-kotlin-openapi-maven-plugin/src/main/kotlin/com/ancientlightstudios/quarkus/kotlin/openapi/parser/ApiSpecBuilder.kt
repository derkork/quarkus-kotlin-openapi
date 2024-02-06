package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

class ApiSpecBuilder(
    private val spec: TransformableSpec,
    private val requestFilter: RequestFilter,
    private val contentTypeMapper: ContentTypeMapper,
    private val node: ObjectNode
) {

    private val schemaDefinitionCollector = SchemaDefinitionCollector()
    private val parseContext = ParseContext(
        extractOpenApiVersion(), node,
        JsonPointer.fromPath("#"), schemaDefinitionCollector, contentTypeMapper
    )

    fun build() {
        val requests = extractRequests(requestFilter)
        val schemas = extractSchemas()

        // just create a default bundle with all the available requests
        spec.bundles = listOf(TransformableRequestBundle(null, requests))
        spec.schemaDefinitions = schemas
        spec.version = node.with("info").getTextOrNull("version")
    }

    private fun extractOpenApiVersion() = node.getTextOrNull("openapi")?.let(ApiVersion::fromString)
        ?: SpecIssue("OpenAPI version not specified")

    private fun extractRequests(requestFilter: RequestFilter) = node.with("paths")
        .propertiesAsList()
        .map { (path, pathNode) ->
            pathNode.asObjectNode { "Json object expected for path '$path'" }
                .extractPathRequests(path, requestFilter)
        }
        .flatten()

    private fun ObjectNode.extractPathRequests(path: String, requestFilter: RequestFilter): List<TransformableRequest> {
        val context = parseContext.contextFor(node, "paths", path)

        // now extract all defined operations in this path
        return propertiesAsList()
            // ignore the parameters key, as this is not a valid operation
            .filter { (name, _) -> name != "parameters" }
            // ignore all operations which are not required
            .filter { (operation, _) -> requestFilter.accept(path, RequestMethod.fromString(operation)) }
            .map { (operation, operationNode) ->
                context.contextFor(operationNode, operation).parseAsRequest(path, RequestMethod.fromString(operation)) {
                    // extract default parameters defined next to the operations
                    // this is done for every operation again to avoid side effects later in the transformation
                    withArray("parameters")
                        .mapIndexed { index, itemNode ->
                            context.contextFor(itemNode, "parameters", "$index").parseAsRequestParameter()
                        }
                }
            }
    }

    private fun extractSchemas(): List<TransformableSchemaDefinition> {
        val result = mutableListOf<TransformableSchemaDefinition>()
        var current = schemaDefinitionCollector.nextUnresolvedSchemaDefinition
        while (current != null) {
            parseContext.contextFor(JsonPointer.fromPath(current.originPath))
                .parseAsSchemaDefinitionInto(current)
            result.add(current)
            current = schemaDefinitionCollector.nextUnresolvedSchemaDefinition
        }

        return result
    }

}

fun JsonNode.parseInto(spec: TransformableSpec, requestFilter: RequestFilter, contentTypeMapper: ContentTypeMapper) {
    asObjectNode { "Json object expected" }.let { ApiSpecBuilder(spec, requestFilter, contentTypeMapper, it).build() }
}

