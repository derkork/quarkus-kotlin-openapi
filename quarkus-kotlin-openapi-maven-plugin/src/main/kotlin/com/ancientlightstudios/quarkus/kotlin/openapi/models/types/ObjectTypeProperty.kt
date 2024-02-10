package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

class ObjectTypeProperty(val sourceName: String, val name: VariableName, val schema: TransformableSchemaUsage)