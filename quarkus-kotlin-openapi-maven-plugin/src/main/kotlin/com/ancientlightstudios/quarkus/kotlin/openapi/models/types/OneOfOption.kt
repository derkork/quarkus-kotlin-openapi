package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName

data class OneOfOption(var modelName: ClassName, val typeUsage: TypeUsage, val aliases: List<String>)