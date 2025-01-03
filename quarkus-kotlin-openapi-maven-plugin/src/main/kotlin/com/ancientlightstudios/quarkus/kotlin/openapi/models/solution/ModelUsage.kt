package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

sealed interface ModelUsage {

    val required: Boolean
    val nullable: Boolean

    // validations

    // nullable | required -> isNullable
    // true     | true     -> true
    // true     | false    -> true
    // false    | true     -> false
    // false    | false    -> true
    fun isNullable() = nullable || !required

}