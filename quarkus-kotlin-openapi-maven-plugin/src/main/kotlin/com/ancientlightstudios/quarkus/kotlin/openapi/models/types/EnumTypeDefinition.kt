package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName

class EnumTypeDefinition(
    val className: ClassName,
    override val nullable: Boolean,
    val baseType: ClassName,
    val items: List<String>
) : TypeDefinition