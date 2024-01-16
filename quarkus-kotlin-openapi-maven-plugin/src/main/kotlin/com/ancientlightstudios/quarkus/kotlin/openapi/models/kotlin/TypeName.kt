package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName

sealed interface TypeName {

    val value: String

    @Suppress("DataClassPrivateConstructor")
    data class SimpleTypeName private constructor(val name: ClassName, val nullable: Boolean) : TypeName {

        override val value = when (nullable) {
            true -> "${name.value}?"
            false -> name.value
        }

        companion object {

            fun ClassName.typeName(nullable: Boolean = false) = SimpleTypeName(this, nullable)

        }

    }

    @Suppress("DataClassPrivateConstructor")
    data class GenericTypeName private constructor(val outerType: SimpleTypeName, val innerType: TypeName) : TypeName {

        override val value = when (outerType.nullable) {
            true -> "${outerType.name.value}<${innerType.value}>?"
            false -> "${outerType.name.value}<${innerType.value}>"
        }

        companion object {

            fun SimpleTypeName.of(inner: TypeName) = GenericTypeName(this, inner)

            fun SimpleTypeName.of(inner: ClassName, nullable: Boolean = false) = of(inner.typeName(nullable))

        }

    }
}