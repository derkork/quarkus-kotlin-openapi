package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

interface GeneratorStage {

    fun process(spec: TransformableSpec)

}