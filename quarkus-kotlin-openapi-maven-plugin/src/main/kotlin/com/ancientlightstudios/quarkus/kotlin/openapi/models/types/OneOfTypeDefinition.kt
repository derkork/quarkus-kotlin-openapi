package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

class OneOfTypeDefinition(
    var className: ClassName,
    override val nullable: Boolean,
    val nested: List<TransformableSchemaUsage>
) : TypeDefinition