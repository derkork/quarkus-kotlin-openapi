package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName

data class InlinePrimitiveTypeDefinition(
    val primitiveType: ClassName,
    val sourceSchema: Schema.PrimitiveSchema
) : TypeDefinition {

    override val isNullable: Boolean
        get() = sourceSchema.nullable

    override val defaultType: TypeName
        get() = primitiveType.typeName(isNullable)

}