package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName

sealed interface TypeDefinitionUsage {

    val nullable: Boolean

    val safeType: TypeName

    val unsafeType: TypeName

    val valueTransform: (String) -> Expression

    val defaultValue: Expression?

    val validation: Validation

}