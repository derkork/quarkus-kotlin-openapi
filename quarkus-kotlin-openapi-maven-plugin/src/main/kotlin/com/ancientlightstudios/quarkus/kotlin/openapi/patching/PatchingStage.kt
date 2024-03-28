package com.ancientlightstudios.quarkus.kotlin.openapi.patching

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.merge
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.patch
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File
import kotlin.io.path.Path

class PatchingStage(private val config: Config) {

    private val objectMapper = ObjectMapper(YAMLFactory())

    fun process(): JsonNode {
        val mergedSource = config.sourceFiles
            .map { read(it) }
            .reduce { acc, apiSpec -> acc.merge(apiSpec) }

        val patchedSource = config.patchFiles
            .map { read(it) }
            .fold(mergedSource) { document, patch -> document.patch(patch) }


        if (config.debugOutputFile != null) {
            Path(config.debugOutputFile).parent.toFile().mkdirs()
            File(config.debugOutputFile).writeText(patchedSource.toPrettyString())
        }

        patchJson(patchedSource)

        return patchedSource
    }

    private fun read(source: String): JsonNode = File(source).inputStream().use { objectMapper.readTree(it) }

    private fun patchJson(json: JsonNode) {
        ParameterSchemaPatch().process(json)
    }

}