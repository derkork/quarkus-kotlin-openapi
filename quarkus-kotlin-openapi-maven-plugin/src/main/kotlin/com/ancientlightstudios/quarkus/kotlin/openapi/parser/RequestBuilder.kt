package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter
import com.fasterxml.jackson.databind.node.ObjectNode

class RequestBuilder(
    private val path: String, private val method: RequestMethod,
    private val defaultParameter: List<Parameter>, private val node: ObjectNode
) {

    private val operationId = node.getTextOrNull("operationId") ?: "${method.name} $path"

    fun ParseContext.build(): Request {
        val parameters = extractParameters()
        val bodyType = extractRequestBody()
        val returnType = extractResponseBodies()

        return Request(
            path, method, operationId,
            node.getTextOrNull("description"),
            node.getBooleanOrNull("deprecated") ?: false,
            parameters, bodyType, returnType
        )
    }

    private fun ParseContext.extractParameters(): List<Parameter> {
        val definedParameter = node
            .withArray("parameters")
            .mapIndexed { index, itemNode ->
                contextFor(itemNode, "parameters[$index]").parseAsRequestParameter()
            }

        val definedParameterNames = definedParameter.map { it.name }.toSet()
        // overwrite parameters in the default list if they are redefined here
        return defaultParameter.filterNot { definedParameterNames.contains(it.name) } + definedParameter
    }

    private fun ParseContext.extractRequestBody() = node.get("requestBody")
        ?.let { contextFor(it, "requestBody").parseAsRequestBody() }

    private fun ParseContext.extractResponseBodies() = node.with("responses")
        .propertiesAsList()
        .map { (code, responseNode) ->
            val body = contextFor(responseNode, "responses.$code").parseAsResponseBody()
            ResponseCode.fromString(code) to body
        }

}

fun ParseContext.parseAsRequest(path: String, method: RequestMethod, defaultParameter: List<Parameter>) =
    contextNode.asObjectNode { "Json object expected for ${this.contextPath}" }
        .let {
            RequestBuilder(path, method, defaultParameter, it).run { this@parseAsRequest.build() }
        }
