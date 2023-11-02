package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.NullExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.AnyOfSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName

data class AnyOfTypeDefinition(
    val name: ClassName, val isNullable: Boolean,
    override val validation: Validation, val schemas: List<TypeDefinitionUsage>
) : TypeDefinition {

    override fun useAs(valueRequired: Boolean) = AnyOfTypeUsage(this, valueRequired)
}

data class AnyOfTypeUsage(private val typeDefinition: AnyOfTypeDefinition, private val valueRequired: Boolean) :
    TypeDefinitionUsage {

    val name = typeDefinition.name

    val unsafeName = name.extend(postfix = "Unsafe")

    override val nullable = typeDefinition.isNullable

    override val safeType = name.typeName(nullable)

    override val unsafeType = unsafeName.typeName(true)

    override val valueTransform = { _: String -> NullExpression }

    override val defaultValue = null

    override val validation = typeDefinition.validation
    
}