package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.setOriginPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.RequestMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest
import com.fasterxml.jackson.databind.node.ObjectNode

class RequestBuilder(
    private val path: String, private val method: RequestMethod,
    private val defaultParameter: () -> List<TransformableParameter>, private val node: ObjectNode
) {

    fun ParseContext.build() = TransformableRequest(
        path, method,
        node.getTextOrNull("operationId") ?: "",
        node.withArray("tags").map { it.asText() },
        extractParameters(),
        extractRequestBody(),
        extractResponses()
    ).apply {
        setOriginPath(contextPath)
    }

    private fun ParseContext.extractParameters(): List<TransformableParameter> {
        val localParameter = node
            .withArray("parameters")
            .mapIndexed { index, itemNode ->
                contextFor(itemNode, "parameters[$index]").parseAsRequestParameter()
            }

        val localParameterNames = localParameter.map { it.name }.toSet()
        // overwrite parameters in the default list if they are redefined here
        return defaultParameter().filterNot { localParameterNames.contains(it.name) } + localParameter
    }

    private fun ParseContext.extractRequestBody() = node.get("requestBody")
        ?.let { contextFor(it, "requestBody").parseAsRequestBody() }

    private fun ParseContext.extractResponses() = node.with("responses")
        .propertiesAsList()
        .map { (code, responseNode) ->
            contextFor(responseNode, "responses", code).parseAsResponse(ResponseCode.fromString(code))
        }

}

fun ParseContext.parseAsRequest(
    path: String,
    method: RequestMethod,
    defaultParameter: () -> List<TransformableParameter>
) =
    contextNode.asObjectNode { "Json object expected for ${this.contextPath}" }
        .let {
            RequestBuilder(path, method, defaultParameter, it).run { this@parseAsRequest.build() }
        }
