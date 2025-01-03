package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the target model of a schema. only available if the schema mode is set to Model
object SchemaTargetModelHint : Hint<SchemaTargetModel> {

    var OpenApiSchema.schemaTargetModel: SchemaTargetModel
        get() = get(SchemaTargetModelHint) ?: ProbableBug("Schema target model not set")
        set(value) = set(SchemaTargetModelHint, value)

}

sealed interface SchemaTargetModel {

    fun compatibleWith(other: SchemaTargetModel): Boolean

    object ArrayModel : SchemaTargetModel {

        override fun compatibleWith(other: SchemaTargetModel) = other is ArrayModel

    }

    class EnumModel(val base: BaseType) : SchemaTargetModel {

        override fun compatibleWith(other: SchemaTargetModel) = when (other) {
            is EnumModel -> base == other.base
            else -> false
        }

    }

    object MapModel : SchemaTargetModel {

        override fun compatibleWith(other: SchemaTargetModel) = other is MapModel

    }

    object ObjectModel : SchemaTargetModel {

        override fun compatibleWith(other: SchemaTargetModel) = other is ObjectModel

    }

    object OneOfModel : SchemaTargetModel {

        override fun compatibleWith(other: SchemaTargetModel) = other is OneOfModel

    }

    class PrimitiveTypeModel(val base: BaseType) : SchemaTargetModel {

        override fun compatibleWith(other: SchemaTargetModel) = when (other) {
            is PrimitiveTypeModel -> base == other.base
            else -> false
        }

    }

}

sealed interface BaseType {

    object String : BaseType
    object ByteArray : BaseType
    object Float : BaseType
    object Double : BaseType
    object Int : BaseType
    object Long : BaseType
    object UInt : BaseType
    object ULong : BaseType
    object BigDecimal : BaseType
    object BigInteger : BaseType
    object Boolean : BaseType

    data class Custom(val name: kotlin.String, val packageName: kotlin.String) : BaseType

}
