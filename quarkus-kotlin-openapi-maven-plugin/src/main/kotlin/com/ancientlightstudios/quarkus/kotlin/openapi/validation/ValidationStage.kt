package com.ancientlightstudios.quarkus.kotlin.openapi.validation

import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

class ValidationStage : GeneratorStage {

    override fun process(spec: TransformableSpec) {
        OneOfDiscriminatorCheck().verify(spec)
        OneOfDiscriminatorMappingCheck().verify(spec)
        GetRequestWithBodyCheck().verify(spec)
    }

}