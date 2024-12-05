package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.FileName

// if we have to create types with generic parameters, convert this into a sealed interface and create two
// implementations. SimpleTypeName and ParameterizedTypeName. KotlinFile should then use SimpleTypeName as its name
data class KotlinTypeName(val name: String, val packageName: String) {

    companion object {

        fun FileName.asTypeName() = KotlinTypeName(name, packageName)

    }

}