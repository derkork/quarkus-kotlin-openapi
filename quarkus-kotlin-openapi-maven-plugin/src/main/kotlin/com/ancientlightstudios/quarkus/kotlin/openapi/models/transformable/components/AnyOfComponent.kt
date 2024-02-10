package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

class AnyOfComponent(override val schemas: List<TransformableSchemaUsage>) : SomeOfComponent, ReferencingComponent
