package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.AdditionalInformation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Parameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Source
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.overrideWhenOptional
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter as OpenApiParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter.CookieParameter as OpenApiCookieParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter.HeaderParameter as OpenApiHeaderParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter.PathParameter as OpenApiPathParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter.QueryParameter as OpenApiQueryParameter

class ParameterTransformer(private val source: OpenApiParameter) {

    fun initializeSchemaRegistry(schemaCollector: SchemaCollector, nameHint: String) {
        schemaCollector.registerSchema(source.schema, FlowDirection.Up) { "$nameHint ${source.name} parameter" }
    }

    fun transform(typeDefinitionRegistry: TypeDefinitionRegistry): Parameter {
        val name = source.name.variableName()
        val type = typeDefinitionRegistry.getTypeDefinition(source.schema, FlowDirection.Up).defaultType
        return when (source) {
            is OpenApiPathParameter -> Parameter(name, type, Source.Path, AdditionalInformation(source.description))
            is OpenApiQueryParameter -> Parameter(
                name,
                type.overrideWhenOptional(!source.required),
                Source.Query,
                AdditionalInformation(source.description, source.deprecated)
            )

            is OpenApiHeaderParameter -> Parameter(
                name,
                type.overrideWhenOptional(!source.required),
                Source.Header,
                AdditionalInformation(source.description, source.deprecated)
            )

            is OpenApiCookieParameter -> Parameter(
                name,
                type.overrideWhenOptional(!source.required),
                Source.Cookie,
                AdditionalInformation(source.description, source.deprecated)
            )
        }
    }

}
