package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

class OneOfComponent(override val schemas: List<TransformableSchemaUsage>) : SomeOfComponent, ReferencingComponent

// TODO: discriminator, additionalMapping