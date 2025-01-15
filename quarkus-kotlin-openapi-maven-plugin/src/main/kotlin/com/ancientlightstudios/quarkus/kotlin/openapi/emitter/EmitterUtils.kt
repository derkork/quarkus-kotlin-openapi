package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.StaticContextExpression.Companion.staticContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
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

fun ModelUsage.asTypeReference(): KotlinTypeReference = when (instance) {
    is CollectionModelInstance -> Kotlin.List.asTypeReference(instance.items.asTypeReference())
    is EnumModelInstance -> instance.ref.name.asTypeReference()
    is MapModelInstance -> Kotlin.Map.asTypeReference(Kotlin.String.asTypeReference(), instance.items.asTypeReference())
    is ObjectModelInstance -> instance.ref.name.asTypeReference()
    is OneOfModelInstance -> instance.ref.name.asTypeReference()
    is PrimitiveTypeModelInstance -> instance.itemType.asTypeReference()
}.run {
    when (isNullable()) {
        true -> this.acceptNull()
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

/**
 * returns the default value specified for an enum or primitive type model if there is any. In all other cases
 * returns [DefaultValue.None] if the usage don't allow null values or [DefaultValue.Null] otherwise.
 */
fun ModelUsage.getDefinedDefaultValue(): DefaultValue = when (instance) {
    is CollectionModelInstance -> DefaultValue.nullOrNone(isNullable())
    is EnumModelInstance -> when (instance.defaultValue) {
        null -> DefaultValue.nullOrNone(isNullable())
        else -> DefaultValue.EnumValue(instance.ref, instance.defaultValue)
    }

    is MapModelInstance -> DefaultValue.nullOrNone(isNullable())
    is ObjectModelInstance -> DefaultValue.nullOrNone(isNullable())
    is OneOfModelInstance -> DefaultValue.nullOrNone(isNullable())
    is PrimitiveTypeModelInstance -> when (instance.defaultValue) {
        null -> DefaultValue.nullOrNone(isNullable())
        else -> DefaultValue.StaticValue(instance.itemType, instance.defaultValue)
    }
}

fun AnnotationAware.addPathAnnotation(path: String) {
    kotlinAnnotation(Jakarta.PathAnnotation, "value" to path.literal())
}

fun AnnotationAware.addRequestMethodAnnotation(method: RequestMethod) {
    val className = when (method) {
        RequestMethod.Get -> Jakarta.GetAnnotation
        RequestMethod.Put -> Jakarta.PutAnnotation
        RequestMethod.Post -> Jakarta.PostAnnotation
        RequestMethod.Delete -> Jakarta.DeleteAnnotation
        RequestMethod.Options -> Jakarta.OptionsAnnotation
        RequestMethod.Head -> Jakarta.HeadAnnotation
        RequestMethod.Patch -> Jakarta.PatchAnnotation
        RequestMethod.Trace -> Jakarta.TraceAnnotation
    }
    kotlinAnnotation(className)
}

fun AnnotationAware.addConsumesAnnotation(vararg contentTypes: String) {
    kotlinAnnotation(Jakarta.ConsumesAnnotation, "value" to contentTypes.toList().arrayLiteral { it.literal() })
}

fun getSourceAnnotation(source: ParameterKind, name: String): KotlinAnnotation {
    val annotationClass = when (source) {
        ParameterKind.Path -> Jakarta.PathParamAnnotation
        ParameterKind.Query -> Jakarta.QueryParamAnnotation
        ParameterKind.Header -> Jakarta.HeaderParamAnnotation
        ParameterKind.Cookie -> Jakarta.CookieParamAnnotation
    }
    return KotlinAnnotation(annotationClass, null to name.literal())
}
