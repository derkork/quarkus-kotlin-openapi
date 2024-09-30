package com.ancientlightstudios.quarkus.kotlin.openapi.patching

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.merge
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ConfigIssue
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue
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
        val mergedSource = config.sourceFiles
            .map { it.readAsJsonFile() }
            .reduce { acc, apiSpec -> acc.merge(apiSpec) }

        val patchedSource = config.patchFiles
            .fold(mergedSource) { document, patch -> document.applyPatch(patch) }

        if (config.debugOutputFile != null) {
            Path(config.debugOutputFile).parent.toFile().mkdirs()
            File(config.debugOutputFile).writeText(patchedSource.toPrettyString())
        }

        patchJson(patchedSource)

        return patchedSource
    }

    private fun JsonNode.applyPatch(patchFile: String): JsonNode {
        try {
            val schema = patchFile.substringBefore("://", "jsonpatch")
            val path = patchFile.substringAfter("://")

            return when (schema) {
                "jsonpatch" -> applyJsonPatch(path)
                "jsonata" -> applyJsonataPatch(path)
                else -> ConfigIssue("unsupported patch format $schema in $patchFile")
            }
        } catch (e:Exception) {
            SpecIssue("Unable to apply patch $patchFile. Reason is ${e.message}")
        }
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