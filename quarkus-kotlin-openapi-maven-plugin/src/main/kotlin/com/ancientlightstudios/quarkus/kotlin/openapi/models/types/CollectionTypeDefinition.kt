package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

class CollectionTypeDefinition(val itemSchema: TransformableSchemaUsage, override val nullable: Boolean) : TypeDefinition