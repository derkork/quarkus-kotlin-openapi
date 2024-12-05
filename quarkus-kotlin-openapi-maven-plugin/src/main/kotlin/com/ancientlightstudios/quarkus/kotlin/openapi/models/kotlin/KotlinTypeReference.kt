package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.FileName

data class KotlinTypeReference(val name: String, val packageName: String) {

    var nullable: Boolean = false
        private set

    fun nullable() = KotlinTypeReference(name, packageName).apply { nullable = true }

    companion object {

        fun FileName.asTypeReference() = asTypeName().asTypeReference()

        fun KotlinTypeName.asTypeReference() = KotlinTypeReference(name, packageName)

    }

}
