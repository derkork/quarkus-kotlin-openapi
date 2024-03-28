package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.ParserStage
import com.ancientlightstudios.quarkus.kotlin.openapi.patching.PatchingStage
import com.ancientlightstudios.quarkus.kotlin.openapi.refactoring.RefactoringStage
import com.ancientlightstudios.quarkus.kotlin.openapi.validation.ValidationStage

class Generator(private val config: Config) {

    fun generate() {
        val json = PatchingStage(config).process()
        val spec = TransformableSpec()
        ParserStage(config, json).process(spec)
        ValidationStage().process(spec)
        RefactoringStage(config).process(spec)
        // TODO: needs another validation stage. e.g. check that the enum-default value is a valid item
        EmitterStage(config).process(spec)
    }

}
