package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.NullExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName

data class CollectionTypeDefinition(
    override val name: ClassName,
    val innerType: TypeDefinition,
    val sourceSchema: Schema.ArraySchema
) : TypeDefinition {

    override fun useAs(valueRequired: Boolean) = CollectionTypeUsage(this, valueRequired)

    override val validations = sourceSchema.validations

}

data class CollectionTypeUsage(
    private val typeDefinition: CollectionTypeDefinition,
    private val valueRequired: Boolean
) : TypeDefinitionUsage {

    val innerType = typeDefinition.innerType.useAs(true)

    // if the value is not required, the property can be nullable, even if the schema doesn't allow null values
    override val nullable = !valueRequired || typeDefinition.sourceSchema.nullable

    override val safeType = "List".rawTypeName(nullable).of(innerType.safeType)

    override val unsafeType = "List".rawTypeName(true).of(typeDefinition.innerType.useAs(false).unsafeType)

    override val valueTransform = { _: String -> NullExpression }

    override val defaultValue = null

    override val validations = typeDefinition.validations

}