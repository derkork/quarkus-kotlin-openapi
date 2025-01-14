package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.StaticContextExpression.Companion.staticContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.PrimitiveTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

fun BaseType.asTypeName() = when (this) {
    BaseType.BigDecimal -> Kotlin.BigDecimal
    BaseType.BigInteger -> Kotlin.BigInteger
    BaseType.Boolean -> Kotlin.Boolean
    BaseType.ByteArray -> Kotlin.ByteArray
    is BaseType.Custom -> KotlinTypeName(name, packageName)
    BaseType.Double -> Kotlin.Double
    BaseType.Float -> Kotlin.Float
    BaseType.Int -> Kotlin.Int
    BaseType.Long -> Kotlin.Long
    BaseType.String -> Kotlin.String
    BaseType.UInt -> Kotlin.UInt
    BaseType.ULong -> Kotlin.ULong
}

fun BaseType.asTypeReference() = asTypeName().asTypeReference()

fun BaseType.literalFor(value: String): KotlinExpression = when (this) {
    BaseType.BigDecimal -> value.bigDecimalLiteral()
    BaseType.BigInteger -> value.bigIntegerLiteral()
    BaseType.Boolean -> value.booleanLiteral()
    BaseType.Double -> value.doubleLiteral()
    BaseType.Float -> value.floatLiteral()
    BaseType.Int -> value.intLiteral()
    BaseType.Long -> value.longLiteral()
    BaseType.String -> value.literal()
    BaseType.UInt -> value.uintLiteral()
    BaseType.ULong -> value.ulongLiteral()
    else -> ProbableBug("Unable to create literal expression for base type ${this::class.java}")
}

fun ModelUsage.adjustToDefault(defaultValue: DefaultValue) = when (defaultValue) {
    DefaultValue.None -> this
    DefaultValue.Null -> this.acceptNull()
    else -> this.rejectNull()
}

fun ModelUsage.asTypeReference(): KotlinTypeReference = instance.asTypeReference(overrideNullableWith)

fun ModelInstance.asTypeReference(overrideNullableWith: Boolean? = null): KotlinTypeReference = when (this) {
    is CollectionModelInstance -> Kotlin.List.asTypeReference(items.asTypeReference())
    is EnumModelInstance -> ref.name.asTypeReference()
    is MapModelInstance -> Kotlin.Map.asTypeReference(Kotlin.String.asTypeReference(), items.asTypeReference())
    is ObjectModelInstance -> ref.name.asTypeReference()
    is OneOfModelInstance -> ref.name.asTypeReference()
    is PrimitiveTypeModelInstance -> itemType.asTypeReference()
}.run {
    val nullable = when (overrideNullableWith) {
        null -> isNullable()
        else -> overrideNullableWith
    }

    when (nullable) {
        true -> this.nullable()
        else -> this
    }
}

fun DefaultValue.toKotlinExpression(): KotlinExpression? = when (val value = this) {
    DefaultValue.EmptyByteArray -> invoke("byteArrayOf")
    DefaultValue.EmptyList -> invoke("listOf")
    DefaultValue.EmptyMap -> invoke("mapOf")
    is DefaultValue.EnumValue -> {
        val enum = value.model
        enum.name.staticContext().property(enum.items.first { it.value == value.value }.name)
    }

    DefaultValue.None -> null
    DefaultValue.Null -> nullLiteral()
    is DefaultValue.StaticValue -> value.type.literalFor(value.value)
}

//fun AnnotationAware.addPathAnnotation(path: String) {
//    kotlinAnnotation(Jakarta.PathAnnotationClass, "value".variableName() to path.literal())
//}
//
//fun AnnotationAware.addRequestMethodAnnotation(method: RequestMethod) {
//    val className = when (method) {
//        RequestMethod.Get -> Jakarta.GetAnnotationClass
//        RequestMethod.Put -> Jakarta.PutAnnotationClass
//        RequestMethod.Post -> Jakarta.PostAnnotationClass
//        RequestMethod.Delete -> Jakarta.DeleteAnnotationClass
//        RequestMethod.Options -> Jakarta.OptionsAnnotationClass
//        RequestMethod.Head -> Jakarta.HeadAnnotationClass
//        RequestMethod.Patch -> Jakarta.PatchAnnotationClass
//        RequestMethod.Trace -> Jakarta.TraceAnnotationClass
//    }
//    kotlinAnnotation(className)
//}
//
//fun AnnotationAware.addConsumesAnnotation(vararg contentTypes: String) {
//    kotlinAnnotation(Jakarta.ConsumesAnnotationClass,
//        "value".variableName() to contentTypes.toList().arrayLiteral { it.literal() })
//}
//
//fun getSourceAnnotation(source: ParameterKind, name: String): KotlinAnnotation {
//    val annotationClass = when (source) {
//        ParameterKind.Path -> Jakarta.PathParamAnnotationClass
//        ParameterKind.Query -> Jakarta.QueryParamAnnotationClass
//        ParameterKind.Header -> Jakarta.HeaderParamAnnotationClass
//        ParameterKind.Cookie -> Jakarta.CookieParamAnnotationClass
//    }
//    return KotlinAnnotation(annotationClass, null to name.literal())
//}
//
//fun TypeUsage.buildValidType(): TypeName {
//    return when (val safeType = this.type) {
//        is PrimitiveTypeDefinition -> safeType.baseType.typeName(isNullable())
//        is EnumTypeDefinition -> safeType.modelName.typeName(isNullable())
//        is ObjectTypeDefinition -> {
//            if (safeType.isPureMap) {
//                Kotlin.MapClass.typeName(isNullable())
//                    .of(Kotlin.StringClass.typeName(false), safeType.additionalProperties!!.buildValidType())
//            } else {
//                safeType.modelName.typeName(isNullable())
//            }
//        }
//        is OneOfTypeDefinition -> safeType.modelName.typeName(isNullable())
//        is CollectionTypeDefinition -> Kotlin.ListClass.typeName(isNullable())
//            .of(safeType.items.buildValidType())
//    }
//}
//
//fun TypeUsage.buildUnsafeJsonType(outerTypeNullable: Boolean = true): TypeName {
//    return when (val safeType = this.type) {
//        is PrimitiveTypeDefinition -> safeType.baseType.typeName(outerTypeNullable)
//        is EnumTypeDefinition -> safeType.modelName.typeName(outerTypeNullable)
//        is ObjectTypeDefinition -> {
//            if (safeType.isPureMap) {
//                Kotlin.MapClass.typeName(outerTypeNullable)
//                    .of(Kotlin.StringClass.typeName(), safeType.additionalProperties!!.buildUnsafeJsonType(true))
//            } else {
//                Library.UnsafeJsonClass.typeName(outerTypeNullable).of(safeType.modelName.typeName())
//            }
//        }
//        is OneOfTypeDefinition -> Library.UnsafeJsonClass.typeName(outerTypeNullable).of(safeType.modelName.typeName())
//        is CollectionTypeDefinition -> Kotlin.ListClass.typeName(outerTypeNullable)
//            .of(safeType.items.buildUnsafeJsonType(true))
//    }
//}
