package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.builder.RequestFilter
import com.ancientlightstudios.quarkus.kotlin.openapi.builder.SchemaRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.builder.parseAsApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.*

import java.io.File

class Generator(private val config: Config) {

    fun generate() {
        val schemaRegistry = SchemaRegistry()

        val apiSpec = config.sourceFiles
            .map { read(File(it).inputStream()) }
            .reduce { acc, apiSpec -> acc.merge(apiSpec) }
            .parseAsApiSpec(schemaRegistry, RequestFilter(config.endpoints))

        val modelContext = GenerationContext("", schemaRegistry, config)
        apiSpec.writeSafeSchemas(modelContext)
        apiSpec.writeUnsafeSchemas(modelContext)

        // BOTH is != SERVER AND != CLIENT so both interfaces are generated
        if (config.interfaceType != InterfaceType.CLIENT) {
            val serverContext = GenerationContext("server", schemaRegistry, config)
            apiSpec.writeServerInterface(serverContext)
            apiSpec.writeServerDelegate(serverContext)
        }
        if (config.interfaceType != InterfaceType.SERVER) {
            val clientContext = GenerationContext("client", schemaRegistry, config)
            apiSpec.writeClientInterface(clientContext)
        }
    }
}