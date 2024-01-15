package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

class EmitterContext(val spec: TransformableSpec, private val config: Config) {
}