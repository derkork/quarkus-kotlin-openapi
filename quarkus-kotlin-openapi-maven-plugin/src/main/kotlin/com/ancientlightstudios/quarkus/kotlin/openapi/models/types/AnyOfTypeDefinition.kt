package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

class AnyOfTypeDefinition(
    var className: ClassName,
    override val nullable: Boolean,
    val nested: List<TransformableSchemaUsage>
) : TypeDefinition