package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.parser.*
import com.ancientlightstudios.quarkus.kotlin.openapi.strafbank.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.transform
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.write

import java.io.File

class Generator(private val config: Config) {

    fun generate() {
        val schemaRegistry = SchemaRegistry()

        val apiSpec = config.sourceFiles
            .map { read(File(it).inputStream()) }
            .reduce { acc, apiSpec -> acc.merge(apiSpec) }
            .parseAsApiSpec(schemaRegistry, RequestFilter(config.endpoints))

        val files = transform(apiSpec, config)
        write(files, config)
    }
}