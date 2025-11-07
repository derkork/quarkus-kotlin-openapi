package com.ancientlightstudios.quarkus.kotlin.openapi.patching

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.SourceFile
import com.ancientlightstudios.quarkus.kotlin.openapi.Sources
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.merge
import com.dashjoin.jsonata.Jsonata.jsonata
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.flipkart.zjsonpatch.JsonPatch
import java.io.File
import kotlin.io.path.Path

class PatchingStage(private val config: Config) {

    private val objectMapper = ObjectMapper(YAMLFactory())

    fun process(): JsonNode {

        var finalFile: JsonNode = objectMapper.createObjectNode()
        for (fileSet in config.sourceFiles) {
            finalFile = fileSet.mergeWith(finalFile)
        }

        if (config.debugOutputFile != null) {
            Path(config.debugOutputFile).parent.toFile().mkdirs()
            File(config.debugOutputFile).writeText(finalFile.toPrettyString())
        }

        patchJson(finalFile)

        return finalFile
    }

    private fun Sources.mergeWith(other: JsonNode): JsonNode {
        val firstIsOpenApi = files.first() is SourceFile.OpenApi
        var result = if (firstIsOpenApi) objectMapper.createObjectNode() else other
        for (file in files) {
            result = when (file) {
                is SourceFile.OpenApi -> result.merge(file.path.readAsJsonFile())
                is SourceFile.JsonPatch -> result.applyJsonPatch(file.path)
                is SourceFile.JsonataPatch -> result.applyJsonataPatch(file.path)
            }
        }
        if (firstIsOpenApi) {
            result = other.merge(result)
        }
        return result
    }


    private fun JsonNode.applyJsonPatch(patchFile: String): JsonNode = JsonPatch.apply(patchFile.readAsJsonFile(), this)

    private fun JsonNode.applyJsonataPatch(patchFile: String): JsonNode {
        // jsonata doesn't know jackson, so we have to convert everything into plain objects first
        var plainJson = objectMapper.convertValue(this, Any::class.java)
        plainJson = jsonata(patchFile.readAsTextFile()).evaluate(plainJson)
        // and convert it back into jackson's json nodes
        return objectMapper.convertValue(plainJson, JsonNode::class.java)
    }

    private fun String.readAsTextFile() = File(this).readText()
    private fun String.readAsJsonFile(): JsonNode = File(this).inputStream().use { objectMapper.readTree(it) }

    private fun patchJson(json: JsonNode) {
        ParameterSchemaPatch().process(json)
    }

}