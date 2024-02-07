package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaProperty

class ObjectTypeDefinition(
    var className: ClassName,
    override val nullable: Boolean,
    val properties: List<TransformableSchemaProperty>
) : TypeDefinition