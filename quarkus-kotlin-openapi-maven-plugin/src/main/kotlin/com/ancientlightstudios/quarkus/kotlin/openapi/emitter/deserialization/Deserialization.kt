package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Kotlin
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.CollectionTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition

object Deserialization {

    // returns the "unsafe" type for a type in a request parameter or body
    // it's List<<DeserializationType for nested type>?> for collections
    // and String? for everything else
    // TODO: binary types
    fun TypeDefinition.getDeserializationSourceType(): TypeName {
        return when (this) {
            is CollectionTypeDefinition -> Kotlin.ListClass.typeName(true)
                .of(items.typeDefinition.getDeserializationSourceType())

            else -> Kotlin.StringClass.typeName(true)
        }
    }

}