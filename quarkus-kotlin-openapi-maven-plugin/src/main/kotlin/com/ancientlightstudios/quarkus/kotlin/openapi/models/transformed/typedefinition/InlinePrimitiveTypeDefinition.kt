package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.valueExpression

data class InlinePrimitiveTypeDefinition(
    val primitiveTypeName: ClassName,
    val sourceSchema: Schema.PrimitiveSchema
) : TypeDefinition {

    override fun useAs(valueRequired: Boolean) = InlinePrimitiveTypeUsage(this, valueRequired)
}

data class InlinePrimitiveTypeUsage(
    private val typeDefinition: InlinePrimitiveTypeDefinition,
    private val valueRequired: Boolean
) : TypeDefinitionUsage {

    val primitiveTypeName = typeDefinition.primitiveTypeName

    override val defaultValue = typeDefinition.sourceSchema.defaultValue?.let { primitiveTypeName.valueExpression(it) }

    // if the value is not required, the property can be nullable, even if the schema doesn't allow null values
    override val nullable = !valueRequired || typeDefinition.sourceSchema.nullable

    override val safeType = primitiveTypeName.typeName(nullable && defaultValue == null)

    override val unsafeType = "String".rawTypeName(true)

}
