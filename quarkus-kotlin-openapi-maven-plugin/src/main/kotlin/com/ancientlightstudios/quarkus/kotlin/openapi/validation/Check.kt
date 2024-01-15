package com.ancientlightstudios.quarkus.kotlin.openapi.validation

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

interface Check {

    fun verify(spec: TransformableSpec)

}