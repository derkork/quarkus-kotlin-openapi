package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Kotlin
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.CollectionTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition

object Serialization {

    // returns the "unsafe" type for a type in a request parameter or body
    // it's List<<DeserializationType for nested type>> for collections
    // and String for everything else. Nullable depends on the type and the forceNullable parameter
    // TODO: binary types
    fun TypeDefinition.getSerializationTargetType(forceNullable: Boolean): TypeName {
        val nullable = forceNullable || this.nullable
        return when (this) {
            is CollectionTypeDefinition -> Kotlin.ListClass.typeName(nullable)
                .of(items.typeDefinition.getSerializationTargetType(false))

            else -> Kotlin.StringClass.typeName(nullable)
        }
    }

}