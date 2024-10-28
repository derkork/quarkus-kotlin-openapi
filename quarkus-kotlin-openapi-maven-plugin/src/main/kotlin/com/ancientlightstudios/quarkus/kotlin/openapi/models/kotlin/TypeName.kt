package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

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
    data class GenericTypeName private constructor(val outerType: SimpleTypeName, val innerType: List<TypeName>) :
        TypeName {

        override val value = when (outerType.nullable) {
            true -> "${outerType.name.value}<${innerType.joinToString { it.value }}>?"
            false -> "${outerType.name.value}<${innerType.joinToString { it.value }}>"
        }

        companion object {

            fun SimpleTypeName.of(inner: TypeName, vararg additional: TypeName) =
                GenericTypeName(this, listOf(inner) + additional.toList())

        }
    }

    data class DelegateTypeName(
        val receiverType: TypeName? = null, val parameterTypes: List<TypeName> = listOf(),
        val returnType: TypeName, val nullable: Boolean = false
    ): TypeName {

        override val value: String
            get() {
                val parameters = parameterTypes.joinToString(prefix = "(", postfix = ")") { it.value }
                val receiverPart = when(receiverType) {
                    null -> ""
                    else -> "${receiverType.value}."
                }

                val delegate = "$receiverPart$parameters -> ${returnType.value}"
                return when(nullable) {
                    false -> delegate
                    else -> "($delegate)?"
                }
            }
    }


}
