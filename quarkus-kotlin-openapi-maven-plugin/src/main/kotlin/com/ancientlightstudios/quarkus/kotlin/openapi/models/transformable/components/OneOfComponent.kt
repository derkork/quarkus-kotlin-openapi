package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

class OneOfComponent(override var schemas: List<TransformableSchemaUsage>) : SomeOfComponent

// TODO: discriminator, additionalMapping