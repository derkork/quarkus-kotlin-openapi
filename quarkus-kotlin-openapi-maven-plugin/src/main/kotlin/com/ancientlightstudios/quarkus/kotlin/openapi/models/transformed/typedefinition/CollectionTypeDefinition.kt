package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName

data class CollectionTypeDefinition(
    val name: ClassName,
    val innerType: TypeDefinition,
    private val sourceSchema: Schema.ArraySchema
) : TypeDefinition {

    override val isNullable: Boolean
        get() = sourceSchema.nullable

    override val defaultType: TypeName
        get() = name.typeName(isNullable).of(innerType.defaultType)

}