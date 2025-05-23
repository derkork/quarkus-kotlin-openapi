package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec
import com.fasterxml.jackson.databind.JsonNode

class ParserStage(private val config: Config, private val json: JsonNode) : GeneratorStage {

    override fun process(spec: OpenApiSpec) {
        json.parseInto(spec, RequestFilter(config.endpoints), ContentTypeMapper(config))
    }

}