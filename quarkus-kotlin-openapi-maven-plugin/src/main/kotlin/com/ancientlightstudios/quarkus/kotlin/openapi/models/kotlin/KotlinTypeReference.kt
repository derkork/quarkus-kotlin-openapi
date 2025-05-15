package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ComponentName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelUsage

sealed interface KotlinTypeReference {

    val nullable: Boolean

    fun acceptNull(includeInnerTypes: Boolean = false): KotlinTypeReference

    fun rejectNull(includeInnerTypes: Boolean = false): KotlinTypeReference

}

data class KotlinSimpleTypeReference(val name: String, val packageName: String) : KotlinTypeReference {

    override var nullable: Boolean = false
        private set

    override fun acceptNull(includeInnerTypes: Boolean) =
        KotlinSimpleTypeReference(name, packageName).apply { nullable = true }

    override fun rejectNull(includeInnerTypes: Boolean) =
        KotlinSimpleTypeReference(name, packageName).apply { nullable = false }

}

data class KotlinParameterizedTypeReference(
    val outerType: KotlinTypeName,
    val innerTypes: List<KotlinTypeReference>
) : KotlinTypeReference {

    override var nullable: Boolean = false
        private set

    override fun acceptNull(includeInnerTypes: Boolean): KotlinTypeReference {
        val newInnerTypes = when (includeInnerTypes) {
            true -> innerTypes.map { it.acceptNull(true) }
            false -> innerTypes
        }

        return KotlinParameterizedTypeReference(outerType, newInnerTypes).apply { nullable = true }
    }

    override fun rejectNull(includeInnerTypes: Boolean): KotlinTypeReference {
        val newInnerTypes = when (includeInnerTypes) {
            true -> innerTypes.map { it.rejectNull(true) }
            false -> innerTypes
        }

        return KotlinParameterizedTypeReference(outerType, newInnerTypes).apply { nullable = false }
    }

}

data class KotlinDelegateTypeReference(
    val receiver: KotlinTypeReference?,
    val returnType: KotlinTypeReference,
    val parameters: List<KotlinTypeReference> = listOf()
) : KotlinTypeReference {

    override var nullable: Boolean = false
        private set

    override fun acceptNull(includeInnerTypes: Boolean) =
        KotlinDelegateTypeReference(receiver, returnType, parameters).apply { nullable = true }

    override fun rejectNull(includeInnerTypes: Boolean) =
        KotlinDelegateTypeReference(receiver, returnType, parameters).apply { nullable = false }

}


fun ComponentName.asTypeReference(vararg inner: KotlinTypeReference) = asTypeName().asTypeReference(*inner)

fun KotlinTypeName.asTypeReference(vararg inner: KotlinTypeReference) = when (inner.isEmpty()) {
    true -> KotlinSimpleTypeReference(name, packageName)
    else -> KotlinParameterizedTypeReference(this, listOf(*inner))
}

