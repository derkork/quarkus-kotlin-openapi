package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.NullExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName

data class OneOfTypeDefinition(
    val name: ClassName, val isNullable: Boolean,
    override val validations: List<Validation>, val schemas: Map<TypeDefinitionUsage, List<String>>,
    val discriminator: String?
) : TypeDefinition {

    override fun useAs(valueRequired: Boolean) = OneOfTypeUsage(this, valueRequired)
}

data class OneOfTypeUsage(private val typeDefinition: OneOfTypeDefinition, private val valueRequired: Boolean) :
    TypeDefinitionUsage {

    val name = typeDefinition.name

    val unsafeName = name.extend(postfix = "Unsafe")

    override val nullable = !valueRequired || typeDefinition.isNullable

    override val safeType = name.typeName(nullable)

    override val unsafeType = unsafeName.typeName(true)

    override val valueTransform = { _: String -> NullExpression }

    override val defaultValue = null

    override val validations = typeDefinition.validations
    
}