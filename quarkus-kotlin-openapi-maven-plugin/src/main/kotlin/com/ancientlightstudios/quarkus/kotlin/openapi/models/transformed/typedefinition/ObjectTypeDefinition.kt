package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName

data class ObjectTypeDefinition(
    val name: ClassName,
    override val isNullable: Boolean,
    val properties: List<ObjectProperty>
) : TypeDefinition {

    override val defaultType: TypeName
        get() = name.typeName(isNullable)

}