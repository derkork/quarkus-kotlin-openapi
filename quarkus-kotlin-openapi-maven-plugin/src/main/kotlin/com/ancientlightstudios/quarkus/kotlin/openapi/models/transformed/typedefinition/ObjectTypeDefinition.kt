package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName.SimpleTypeName.Companion.typeName

// TODO: include property filter
data class ObjectTypeDefinition(
    val name: ClassName,
    private val sourceSchema: Schema.ObjectSchema
) : TypeDefinition {

    override val isNullable: Boolean
        get() = sourceSchema.nullable

    override val defaultType: TypeName
        get() = name.typeName(isNullable)

}