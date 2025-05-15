package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.EnumModelClass

sealed interface DefaultValue {

    object None : DefaultValue
    object Null : DefaultValue
    object EmptyList : DefaultValue
    object EmptyMap : DefaultValue
    object EmptyByteArray : DefaultValue
    data class StaticValue(val type: BaseType, val value: String) : DefaultValue
    data class EnumValue(val model: EnumModelClass, val value: String) : DefaultValue

    companion object {

        /**
         * convenience method to select [Null] or [None] as a default value
         */
        fun nullOrNone(canBeNull: Boolean) = nullOr(None, canBeNull)

        /**
         * returns [Null] if the given value is true (and the variable or property can be null) or [ifNotNull] if the value
         * is false
         */
        fun nullOr(ifNotNull: DefaultValue, canBeNull: Boolean) = when (canBeNull) {
            true -> Null
            false -> ifNotNull
        }
    }
}