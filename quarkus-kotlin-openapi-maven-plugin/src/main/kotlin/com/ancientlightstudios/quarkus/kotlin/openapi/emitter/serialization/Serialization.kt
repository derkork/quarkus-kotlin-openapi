package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Kotlin
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.CollectionTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeUsage

object Serialization {

//    // returns the "unsafe" type for a type in a request parameter or body
//    // it's List<<DeserializationType for nested type>> for collections
//    // and String for everything else. Nullable depends on the type and the forceNullable parameter
//    // TODO: binary types
//    fun TypeUsage.getSerializationTargetType(omitList: Boolean = false): TypeName {
//        return when (val safeType = type) {
//            is CollectionTypeDefinition -> {
//                if (omitList) {
//                    safeType.items.getSerializationTargetType(true)
//                } else {
//                    Kotlin.ListClass.typeName(isNullable())
//                        .of(safeType.items.getSerializationTargetType())
//                }
//            }
//
//            else -> Kotlin.StringClass.typeName(isNullable())
//        }
//    }

}