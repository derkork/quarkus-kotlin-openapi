package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.AdditionalInformation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite

class ApiSpecTransformer(private val source: ApiSpec) {

    fun transform(interfaceName: String): Pair<RequestSuite, TypeDefinitionRegistry> {
        val typeDefinitionRegistry = initializeTypeDefinitionRegistry()
        return transform(interfaceName, typeDefinitionRegistry) to typeDefinitionRegistry
    }

    private fun initializeTypeDefinitionRegistry(): TypeDefinitionRegistry {
        val schemaCollector = SchemaCollector()

        source.requests.forEach {
            RequestTransformer(it).initializeSchemaRegistry(schemaCollector)
        }

        return schemaCollector.getTypeDefinitionRegistry()
    }

    private fun transform(interfaceName: String, typeDefinitionRegistry: TypeDefinitionRegistry): RequestSuite {
        return RequestSuite(
            interfaceName.className(),
            source.version,
            source.requests.map { RequestTransformer(it).transform(typeDefinitionRegistry) },
            AdditionalInformation(source.description)
        )
    }
}
