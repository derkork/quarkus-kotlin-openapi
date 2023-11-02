package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName

sealed interface TypeName : Name {

    fun extend(prefix: String = "", postfix: String = ""): TypeName

    fun alterNullable(nullable: Boolean) : TypeName

    @Suppress("DataClassPrivateConstructor")
    data class SimpleTypeName private constructor(val name: ClassName, val nullable: Boolean) : TypeName {

        override fun extend(prefix: String, postfix: String) = name.extend(prefix, postfix).typeName(nullable)

        override fun alterNullable(nullable: Boolean) = SimpleTypeName(name, nullable)

        override fun render() = when (nullable) {
            true -> "${name.render()}?"
            false -> name.render()
        }

        companion object {

            fun ClassName.typeName(nullable: Boolean = false) = SimpleTypeName(this, nullable)

            fun String.rawTypeName(nullable: Boolean = false) = rawClassName().typeName(nullable)

            fun String.typeName(nullable: Boolean = false) = className().typeName(nullable)

            fun Name.typeName(nullable: Boolean = false) = render().className().typeName(nullable)

        }

    }

    @Suppress("DataClassPrivateConstructor")
    data class GenericTypeName private constructor(val outerType: SimpleTypeName, val innerType: TypeName) : TypeName {

        override fun extend(prefix: String, postfix: String) = outerType.extend(prefix, postfix).of(innerType)

        override fun alterNullable(nullable: Boolean) = GenericTypeName(outerType.alterNullable(nullable), innerType)

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