package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName

data class ObjectTypeDefinition(
    val name: ClassName,
    val isNullable: Boolean,
    val properties: List<ObjectProperty>
) : TypeDefinition {

    override fun useAs(valueRequired: Boolean) = ObjectTypeUsage(this, valueRequired)
}

data class ObjectTypeUsage(private val typeDefinition: ObjectTypeDefinition, private val valueRequired: Boolean) :
    TypeDefinitionUsage {

    val name = typeDefinition.name

    val unsafeName = name.extend(postfix = "Unsafe")

    // if the value is not required, the property can be nullable, even if the schema doesn't allow null values
    override val nullable = !valueRequired || typeDefinition.isNullable

    override val safeType = name.typeName(nullable)

    override val unsafeType = unsafeName.typeName(true)

    override val defaultValue = null
}