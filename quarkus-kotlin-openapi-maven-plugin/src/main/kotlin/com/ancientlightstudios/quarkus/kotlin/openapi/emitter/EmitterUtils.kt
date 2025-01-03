package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.PrimitiveTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

fun BaseType.asTypeReference() = when (this) {
    BaseType.BigDecimal -> Kotlin.BigDecimal.asTypeReference()
    BaseType.BigInteger -> Kotlin.BigInteger.asTypeReference()
    BaseType.Boolean -> Kotlin.Boolean.asTypeReference()
    BaseType.ByteArray -> Kotlin.ByteArray.asTypeReference()
    is BaseType.Custom -> KotlinSimpleTypeReference(name, packageName)
    BaseType.Double -> Kotlin.Double.asTypeReference()
    BaseType.Float -> Kotlin.Float.asTypeReference()
    BaseType.Int -> Kotlin.Int.asTypeReference()
    BaseType.Long -> Kotlin.Long.asTypeReference()
    BaseType.String -> Kotlin.String.asTypeReference()
    BaseType.UInt -> Kotlin.UInt.asTypeReference()
    BaseType.ULong -> Kotlin.ULong.asTypeReference()
}

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

fun ModelUsage.asTypeReference(withDefault: Boolean = false): KotlinTypeReference = when (this) {
    is CollectionModelUsage -> Kotlin.List.asTypeReference(items.asTypeReference())
    is EnumModelUsage -> ref.name.asTypeReference()
    is MapModelUsage -> Kotlin.Map.asTypeReference(Kotlin.String.asTypeReference(), items.asTypeReference())
    is ObjectModelUsage -> ref.name.asTypeReference()
    is OneOfModelUsage -> ref.name.asTypeReference()
    is PrimitiveTypeModelUsage -> itemType.asTypeReference()
}.run {
    val nullable = !withDefault && isNullable()

    when(nullable) {
        true -> this.nullable()
        else -> this
    }
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

fun TypeUsage.isNullable(): Boolean {
    if (!nullable) {
        // null values are just not allowed
        return false
    }

    val hasDefault = when (val safeType = type) {
        is PrimitiveTypeDefinition -> safeType.defaultValue != null
//        is EnumTypeDefinition -> safeType.defaultValue != null
        else -> false
    }

    if (hasDefault) {
        // if there is a default value set, this type never accepts null values
        return false
    }

    return nullable
}
