package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaUsage

class OneOfComponent(override val schemas: List<SchemaUsage>) : SomeOfComponent

// TODO: discriminator, additionalMapping