package com.ancientlightstudios.quarkus.kotlin.openapi.patching

import com.fasterxml.jackson.databind.JsonNode

interface JsonPatch {

    fun process(json: JsonNode)

}