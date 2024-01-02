package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.AdditionalInformation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite

class ApiSpecTransformer(private val source: ApiSpec, private val config:Config) {

    fun transform(): Pair<RequestSuite, TypeDefinitionRegistry> {
        val typeDefinitionRegistry = initializeTypeDefinitionRegistry()
        return transform(config.interfaceName, typeDefinitionRegistry) to typeDefinitionRegistry
    }

    private fun initializeTypeDefinitionRegistry(): TypeDefinitionRegistry {
        val nameRegistry = NameRegistry()
        source.requests.forEach {
            RequestTransformer(it).registerNames(nameRegistry)
        }

        val schemaCollector = SchemaCollector(nameRegistry)

        source.requests.forEach {
            RequestTransformer(it).initializeSchemaRegistry(schemaCollector)
        }

        return schemaCollector.getTypeDefinitionRegistry(config)
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
