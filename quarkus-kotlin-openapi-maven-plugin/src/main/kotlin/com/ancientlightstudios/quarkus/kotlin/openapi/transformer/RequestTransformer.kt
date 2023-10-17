package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.AdditionalInformation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request as OpenApiRequest

class RequestTransformer(private val source: OpenApiRequest) {

    fun initializeSchemaRegistry(schemaCollector: SchemaCollector) {
        schemaCollector.registerSchema(source.body?.schema, FlowDirection.Up) { "${source.operationId} body" }
        source.parameters.forEach {
            ParameterTransformer(it).initializeSchemaRegistry(schemaCollector, source.operationId)
        }
        source.responses.forEach { (code, response) ->
            schemaCollector.registerSchema(response.schema, FlowDirection.Down) { "${source.operationId} $code response" }
            response.headers.forEach { (name, header) ->
                schemaCollector.registerSchema(header.schema, FlowDirection.Down) { "${source.operationId} $code $name header" }
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
