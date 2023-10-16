package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.AdditionalInformation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Parameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Source
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter as OpenApiParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter.CookieParameter as OpenApiCookieParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter.HeaderParameter as OpenApiHeaderParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter.PathParameter as OpenApiPathParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter.QueryParameter as OpenApiQueryParameter

class ParameterTransformer(private val source: OpenApiParameter) {

    fun initializeSchemaRegistry(schemaCollector: SchemaCollector) {
        schemaCollector.registerSchema(source.schema, FlowDirection.Up)
    }

    fun transform(typeDefinitionRegistry: TypeDefinitionRegistry): Parameter {
        val name = source.name.variableName()
        val type = "String".rawTypeName(true)
        return when (source) {
            is OpenApiPathParameter -> Parameter(name, type, Source.Path, AdditionalInformation(source.description))
            is OpenApiQueryParameter -> Parameter(
                name,
                type,
                Source.Query,
                AdditionalInformation(source.description, source.deprecated)
            )

            is OpenApiHeaderParameter -> Parameter(
                name,
                type,
                Source.Header,
                AdditionalInformation(source.description, source.deprecated)
            )

            is OpenApiCookieParameter -> Parameter(
                name,
                type,
                Source.Cookie,
                AdditionalInformation(source.description, source.deprecated)
            )
        }
    }

}
