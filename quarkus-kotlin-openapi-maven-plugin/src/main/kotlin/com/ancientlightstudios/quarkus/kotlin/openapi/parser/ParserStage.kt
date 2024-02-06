package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File
import kotlin.io.path.Path

class ParserStage(private val config: Config) : GeneratorStage {

    private val objectMapper = ObjectMapper(YAMLFactory())

    override fun process(spec: TransformableSpec) {
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

        patchedSource.parseInto(spec, RequestFilter(config.endpoints), ContentTypeMapper(config))
    }

    private fun read(source: String): JsonNode = File(source).inputStream().use { objectMapper.readTree(it) }

}