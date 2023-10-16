package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName

sealed interface TypeDefinition {

    val isNullable: Boolean

    val defaultType: TypeName

}