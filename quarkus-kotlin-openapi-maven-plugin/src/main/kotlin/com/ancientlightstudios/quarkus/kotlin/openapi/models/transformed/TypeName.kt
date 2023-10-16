package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName.SimpleTypeName.Companion.typeName

sealed interface TypeName {

    fun render(): String

    @Suppress("DataClassPrivateConstructor")
    data class SimpleTypeName private constructor(val name: ClassName, val nullable: Boolean) : TypeName {

        override fun render() = when (nullable) {
            true -> "${name.render()}?"
            false -> name.render()
        }

        companion object {

            fun ClassName.typeName(nullable: Boolean = false) = SimpleTypeName(this, nullable)

            fun String.rawTypeName(nullable: Boolean = false) = rawClassName().typeName(nullable)

            fun String.typeName(nullable: Boolean = false) = className().typeName(nullable)

            fun MethodName.typeName(nullable: Boolean = false) = render().className().typeName(nullable)

            fun VariableName.typeName(nullable: Boolean = false) = render().className().typeName(nullable)

        }

    }

    @Suppress("DataClassPrivateConstructor")
    data class GenericTypeName private constructor(val outerType: SimpleTypeName, val innerType: TypeName) : TypeName {

        override fun render() = when (outerType.nullable) {
            true -> "${outerType.name.render()}<${innerType.render()}>?"
            false -> "${outerType.name.render()}<${innerType.render()}>"
        }

        companion object {

            fun SimpleTypeName.of(inner: TypeName) = GenericTypeName(this, inner)

            fun SimpleTypeName.of(inner: ClassName, nullable: Boolean = false) = of(inner.typeName(nullable))

        }

    }
}