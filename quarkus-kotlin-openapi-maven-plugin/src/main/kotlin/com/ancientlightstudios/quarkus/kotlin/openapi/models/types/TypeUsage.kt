package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

class TypeUsage(val required: Boolean) {

    lateinit var type: TypeDefinition

    constructor(required: Boolean, type: TypeDefinition) : this(required) {
        this.type = type
    }

    val nullable: Boolean
        get() = type.nullable || !required

    //                  type.nullable | required -> usage.nullable             | isNullable
    //                  true           | true    -> true                         true
    //                  true           | false   -> true                         true
    //                  false          | true    -> false                        false
    //                  false          | false   -> true                         true
}