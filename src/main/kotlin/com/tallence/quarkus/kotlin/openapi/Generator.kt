package com.tallence.quarkus.kotlin.openapi

import com.tallence.quarkus.kotlin.openapi.builder.SchemaRegistry
import com.tallence.quarkus.kotlin.openapi.builder.parseAsApiSpec
import com.tallence.quarkus.kotlin.openapi.writer.writeInterface
import com.tallence.quarkus.kotlin.openapi.writer.writeSchemas

import java.io.File

class Generator(private val config: Config) {

    fun generate() {
        val schemaRegistry = SchemaRegistry()

        val apiSpec = config.sourceFiles
            .map { read(File(it).inputStream()) }
            .reduce { acc, apiSpec -> acc.merge(apiSpec) }
            .parseAsApiSpec(schemaRegistry)

        val validModelContext = GenerationContext(false, schemaRegistry, config)
        apiSpec.writeSchemas(validModelContext)

        // BOTH is != SERVER AND != CLIENT so both interfaces are generated
        if (config.interfaceType != InterfaceType.CLIENT) {
            val serverContext = GenerationContext(true, schemaRegistry, config)
            apiSpec.writeInterface(serverContext)
            apiSpec.writeSchemas(serverContext)
        }
        if (config.interfaceType != InterfaceType.SERVER) {
            val clientContext = GenerationContext(false, schemaRegistry, config)
            apiSpec.writeInterface(clientContext)
        }
    }
}