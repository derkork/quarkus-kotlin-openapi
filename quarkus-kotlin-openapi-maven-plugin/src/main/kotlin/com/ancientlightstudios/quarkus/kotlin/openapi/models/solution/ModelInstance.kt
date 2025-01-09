package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

sealed interface ModelInstance {

    val required: Boolean
    val nullable: Boolean

    // TODO: validations

    // nullable | required -> isNullable
    // true     | true     -> true
    // true     | false    -> true
    // false    | true     -> false
    // false    | false    -> true
    fun isNullable() = nullable || !required

}