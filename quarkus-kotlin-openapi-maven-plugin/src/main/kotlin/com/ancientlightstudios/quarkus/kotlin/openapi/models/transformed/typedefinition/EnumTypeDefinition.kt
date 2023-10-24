package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.NullExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ConstantName.Companion.constantName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName

data class EnumTypeDefinition(
    val name: ClassName,
    val primitiveType: ClassName,
    val sourceSchema: Schema.EnumSchema
) : TypeDefinition {

    override fun useAs(valueRequired: Boolean) = EnumTypeUsage(this, valueRequired)

    override val validation = sourceSchema.validation

}

data class EnumTypeUsage(private val typeDefinition: EnumTypeDefinition, private val valueRequired: Boolean) :
    TypeDefinitionUsage {

    val name = typeDefinition.name

    override val valueTransform = { value: String -> name.pathExpression().then(value.constantName()) }

    override val defaultValue = typeDefinition.sourceSchema.defaultValue?.let(valueTransform)

    // if the value is not required, the property can be nullable, even if the schema doesn't allow null values
    override val nullable = !valueRequired || typeDefinition.sourceSchema.nullable

    override val safeType = name.typeName(nullable && defaultValue == null)

    override val unsafeType = "String".rawTypeName(true)

    override val validation = typeDefinition.validation

}