package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ComponentName

sealed interface KotlinTypeReference {

    val nullable: Boolean

    fun nullable(includeInnerTypes: Boolean = false): KotlinTypeReference

}

data class KotlinSimpleTypeReference(val name: String, val packageName: String) : KotlinTypeReference {

    override var nullable: Boolean = false
        private set

    override fun nullable(includeInnerTypes: Boolean) =
        KotlinSimpleTypeReference(name, packageName).apply { nullable = true }

}

data class KotlinParameterizedTypeReference(
    val outerType: KotlinSimpleTypeReference,
    val innerTypes: List<KotlinTypeReference>
) : KotlinTypeReference {

    override val nullable get() = outerType.nullable

    override fun nullable(includeInnerTypes: Boolean): KotlinTypeReference {
        val newInnerTypes = when (includeInnerTypes) {
            true -> innerTypes.map { it.nullable(true) }
            false -> innerTypes
        }

        return KotlinParameterizedTypeReference(outerType.nullable(includeInnerTypes), newInnerTypes)
    }

}

fun ComponentName.asTypeReference(vararg inner: KotlinTypeReference) = asTypeName().asTypeReference(*inner)

fun KotlinTypeName.asTypeReference(vararg inner: KotlinTypeReference) = when (inner.isEmpty()) {
    true -> KotlinSimpleTypeReference(name, packageName)
    else -> KotlinParameterizedTypeReference(KotlinSimpleTypeReference(name, packageName), listOf(*inner))
}

