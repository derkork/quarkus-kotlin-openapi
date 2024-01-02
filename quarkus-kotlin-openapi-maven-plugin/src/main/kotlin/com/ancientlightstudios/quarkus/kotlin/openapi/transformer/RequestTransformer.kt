package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.AdditionalInformation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request as OpenApiRequest

class RequestTransformer(private val source: OpenApiRequest) {

    fun registerNames(nameRegistry: NameRegistry) {
        if (source.body != null || source.parameters.isNotEmpty()) {
            nameRegistry.uniqueNameFor(source.operationId.methodName().className().extend(postfix = "Request"))
        }
        nameRegistry.uniqueNameFor(source.operationId.methodName().className().extend(postfix = "Response"))
    }

    fun initializeSchemaRegistry(schemaCollector: SchemaCollector) {
        schemaCollector.registerSchema(source.body?.schema, FlowDirection.Up) { "${source.operationId} body" }
        source.parameters.forEach {
            ParameterTransformer(it).initializeSchemaRegistry(schemaCollector, source.operationId)
        }
        source.responses.forEach { (code, response) ->
            schemaCollector.registerSchema(
                response.schema,
                FlowDirection.Down
            ) { "${source.operationId} $code response" }
            response.headers.forEach { (name, header) ->
                schemaCollector.registerSchema(
                    header.schema,
                    FlowDirection.Down
                ) { "${source.operationId} $code $name header" }
            }
        }
    }

    fun transform(typeDefinitionRegistry: TypeDefinitionRegistry): Request {
        return Request(
            source.operationId.methodName(),
            source.path,
            source.method,
            source.parameters.map { ParameterTransformer(it).transform(typeDefinitionRegistry) },
            transformRequestBody(source.body, typeDefinitionRegistry),
            source.responses.map { transformResponse(it, typeDefinitionRegistry) },
            AdditionalInformation(source.description, source.deprecated)
        )
    }

    private fun transformRequestBody(body: RequestBody?, typeDefinitionRegistry: TypeDefinitionRegistry) = body?.let {
        typeDefinitionRegistry.getTypeDefinition(it.schema, FlowDirection.Up)
            .useAs(it.required)
    }

    private fun transformResponse(
        response: Pair<ResponseCode, ResponseBody>,
        typeDefinitionRegistry: TypeDefinitionRegistry
    ) = response.first to response.second.schema?.let {
        typeDefinitionRegistry.getTypeDefinition(it, FlowDirection.Down).useAs(true)
    }

}
