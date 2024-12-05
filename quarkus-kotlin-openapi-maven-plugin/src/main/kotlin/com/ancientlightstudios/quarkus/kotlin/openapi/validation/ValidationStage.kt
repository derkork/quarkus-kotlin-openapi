package com.ancientlightstudios.quarkus.kotlin.openapi.validation

import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec

class ValidationStage : GeneratorStage {

    override fun process(spec: OpenApiSpec) {
        listOf(
            OneOfDiscriminatorCheck(),
            OneOfDiscriminatorMappingCheck(),
            GetRequestWithBodyCheck()
            // TODO: check that the enum-default value is a valid item
        ).runChecks(spec)
    }

    private fun List<Check>.runChecks(spec: OpenApiSpec) {
        forEach {
            it.verify(spec)
        }
    }

}