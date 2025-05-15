package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SchemaValidation

sealed interface ModelInstance {

    val required: Boolean
    val nullable: Boolean
    val validations: List<SchemaValidation>

    // nullable | required -> isNullable
    // true     | true     -> true
    // true     | false    -> true
    // false    | true     -> false
    // false    | false    -> true
    fun isNullable() = nullable || !required

}