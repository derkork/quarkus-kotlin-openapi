package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.RequestMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

class ApiSpecBuilder(
    private val spec: TransformableSpec,
    private val requestFilter: RequestFilter,
    private val node: ObjectNode
) {

    private val parseContext = ParseContext(extractOpenApiVersion(), node, JsonPointer.fromPath("#"))

//    private val schemaCollector = SchemaCollector()
//    private val referenceResolver = ReferenceResolver(openApiVersion, node, schemaCollector)

    fun build() {
        val requests = extractRequests(requestFilter)
//        val schemas = extractSchemas()

        // just create a default bundle with all the available requests
        spec.bundles = listOf(TransformableRequestBundle(null, requests))
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
                            context.contextFor(itemNode, "parameters[$index]").parseAsRequestParameter()
                        }
                }
            }
    }

//    private fun extractSchemas(): List<OpenApiSchema> {
//        var nextRef = schemaCollector.nextUnresolvedReference()
//        while (nextRef != null) {
//            val pointer = JsonPointer.fromPath(nextRef)
//            val schemaNode =
//                node.resolvePointer(pointer) ?: throw IllegalArgumentException("Unresolvable schema reference $nextRef")
//            ParseContext(
//                openApiVersion,
//                schemaNode,
//                pointer,
//                referenceResolver,
//                schemaCollector
//            ).parseAsSchema(nextRef.targetName())
//
//            nextRef = schemaCollector.nextUnresolvedReference()
//        }
//
//        return schemaCollector.allSchemas
//    }

}

fun JsonNode.parseInto(spec: TransformableSpec, requestFilter: RequestFilter) {
    asObjectNode { "Json object expected" }.let { ApiSpecBuilder(spec, requestFilter, it).build() }
}

