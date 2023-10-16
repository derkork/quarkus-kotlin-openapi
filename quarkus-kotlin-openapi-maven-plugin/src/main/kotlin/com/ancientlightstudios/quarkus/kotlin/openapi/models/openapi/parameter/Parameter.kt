package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema

// TODO: style, explode, content
sealed interface Parameter {

    val name: String
    val schema: Schema
    val description: String?

    sealed interface PathParameter : Parameter

    sealed interface QueryParameter : Parameter {

        val deprecated: Boolean
        val required: Boolean

    }

    sealed interface HeaderParameter : Parameter {

        val deprecated: Boolean
        val required: Boolean

    }

    sealed interface CookieParameter : Parameter {

        val deprecated: Boolean
        val required: Boolean
    }

}

sealed interface ParameterDefinition
sealed interface ParameterReference<P : Parameter> {

    val targetName: String
    val target: P

}
