package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.AdditionalInformation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request as OpenApiRequest

class RequestTransformer(private val source: OpenApiRequest) {

    fun initializeSchemaRegistry(schemaCollector: SchemaCollector) {
        schemaCollector.registerSchema(source.body?.schema, FlowDirection.Up)
        source.parameters.forEach {
            ParameterTransformer(it).initializeSchemaRegistry(schemaCollector)
        }
        source.responses.forEach { (_, response) ->
            schemaCollector.registerSchema(response.schema, FlowDirection.Down)
            response.headers.forEach { (_, header) ->
                schemaCollector.registerSchema(header.schema, FlowDirection.Down)
            }
        }
    }

    fun transform(typeDefinitionRegistry: TypeDefinitionRegistry): Request {
        return Request(
            source.operationId.methodName(),
            source.path,
            source.method,
            source.parameters.map { ParameterTransformer(it).transform(typeDefinitionRegistry) },
            transformRequestBody(source.body),
            source.responses.map(::transformResponse),
            AdditionalInformation(source.description, source.deprecated)
        )
    }

    private fun transformRequestBody(body: RequestBody?): TypeName? {
        return body?.let { "String".rawTypeName(true) }
    }

    private fun transformResponse(response: Pair<ResponseCode, ResponseBody>): Pair<ResponseCode, TypeName?> {
        val type = response.second.schema?.let { "String".rawTypeName(true) }
        return response.first to type
    }

}
