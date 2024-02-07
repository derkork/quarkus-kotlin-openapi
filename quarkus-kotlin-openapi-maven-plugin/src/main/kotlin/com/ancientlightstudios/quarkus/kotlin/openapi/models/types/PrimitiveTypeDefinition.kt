package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName

class PrimitiveTypeDefinition(var className: ClassName, override val nullable: Boolean) : TypeDefinition