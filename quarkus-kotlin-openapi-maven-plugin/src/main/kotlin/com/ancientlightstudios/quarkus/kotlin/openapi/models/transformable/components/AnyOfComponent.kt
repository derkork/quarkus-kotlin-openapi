package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaUsage

class AnyOfComponent(override val schemas: List<SchemaUsage>) : SomeOfComponent
