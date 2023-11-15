package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.NullExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.valueExpression

data class InlinePrimitiveTypeDefinition(
    val primitiveTypeName: ClassName,
    val serializeMethodName:MethodName,
    val deserializeMethodName:MethodName,
    val sourceSchema: Schema.PrimitiveSchema
) : TypeDefinition {

    override fun useAs(valueRequired: Boolean) = InlinePrimitiveTypeUsage(this, valueRequired)

    override val validation = sourceSchema.validation

}

data class InlinePrimitiveTypeUsage(
    private val typeDefinition: InlinePrimitiveTypeDefinition,
    private val valueRequired: Boolean
) : TypeDefinitionUsage {

    val primitiveTypeName = typeDefinition.primitiveTypeName

    val serializeMethodName = typeDefinition.serializeMethodName

    val deserializeMethodName = typeDefinition.deserializeMethodName

    override val valueTransform = { value: String -> primitiveTypeName.valueExpression(value) }

    override val defaultValue = typeDefinition.sourceSchema.defaultValue?.let(valueTransform)

    // if the value is not required, the property can be nullable, even if the schema doesn't allow null values
    override val nullable = !valueRequired || typeDefinition.sourceSchema.nullable

    override val safeType = primitiveTypeName.typeName(nullable && defaultValue == null)

    override val unsafeType = "String".rawTypeName(true)

    override val validation = typeDefinition.validation

}
