package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.SchemaProperty
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName.SimpleTypeName.Companion.typeName

data class AllOfTypeDefinition(
    val name: ClassName,
    val sourceSchema: Schema.AllOfSchema,
    val propertyFilter: (SchemaProperty) -> Boolean
) : TypeDefinition {

    override val isNullable: Boolean
        get() = sourceSchema.nullable

    override val defaultType: TypeName
        get() = name.typeName(isNullable)

}