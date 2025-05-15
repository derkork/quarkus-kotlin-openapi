package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

fun withValidation(source: KotlinExpression, model: ModelInstance): KotlinExpression {
    var result = source
    val validations = model.validations

    when (model) {
        is CollectionModelInstance -> {
            result = emitArrayValidation(result, validations.filterIsInstance<ArrayValidation>())
        }

        is EnumModelInstance -> {}
        is MapModelInstance -> {
            result = emitPropertyValidation(result, validations.filterIsInstance<PropertiesValidation>())
        }

        is ObjectModelInstance -> {
            result = emitPropertyValidation(result, validations.filterIsInstance<PropertiesValidation>())
        }

        is OneOfModelInstance -> {}
        is PrimitiveTypeModelInstance -> {
            result = emitStringValidation(model, result, validations.filterIsInstance<StringValidation>())
            result = emitNumberValidation(model, result, validations.filterIsInstance<NumberValidation>())
        }
    }

    result = emitCustomConstraintsValidation(result, validations.filterIsInstance<CustomConstraintsValidation>())
    return result
}

private fun emitStringValidation(
    model: PrimitiveTypeModelInstance,
    statement: KotlinExpression,
    validations: List<StringValidation>
): KotlinExpression {
    if (validations.isEmpty()) {
        return statement
    }

    val methodName = when (model.itemType) {
        is BaseType.ByteArray -> "validateByteArray"
        else -> "validateString"
    }

    return statement.wrap()
        .invoke(methodName) {
            validations.forEach {
                it.minLength?.let { min -> "it".identifier().invoke("minLength", min.literal()).statement() }
                it.maxLength?.let { max -> "it".identifier().invoke("maxLength", max.literal()).statement() }
                it.pattern?.let { pattern -> "it".identifier().invoke("pattern", pattern.literal()).statement() }
            }
        }
}

private fun emitNumberValidation(
    model: PrimitiveTypeModelInstance,
    statement: KotlinExpression,
    validations: List<NumberValidation>
): KotlinExpression {
    if (validations.isEmpty()) {
        return statement
    }

    val baseType = model.itemType
    return statement.wrap()
        .invoke("validateNumber") {
            validations.forEach {
                it.minimum?.let { min ->
                    "it".identifier().invoke("minimum", baseType.literalFor(min.value), min.exclusive.literal())
                        .statement()
                }
                it.maximum?.let { max ->
                    "it".identifier().invoke("maximum", baseType.literalFor(max.value), max.exclusive.literal())
                        .statement()
                }
            }
        }
}

private fun emitArrayValidation(
    statement: KotlinExpression,
    validations: List<ArrayValidation>
): KotlinExpression {
    if (validations.isEmpty()) {
        return statement
    }

    return statement.wrap()
        .invoke("validateList") {
            validations.forEach {
                it.minSize?.let { min -> "it".identifier().invoke("minSize", min.literal()).statement() }
                it.maxSize?.let { max -> "it".identifier().invoke("maxSize", max.literal()).statement() }
            }
        }
}

private fun emitPropertyValidation(
    statement: KotlinExpression,
    validations: List<PropertiesValidation>
): KotlinExpression {
    if (validations.isEmpty()) {
        return statement
    }

    return statement.wrap()
        .invoke("validateProperties") {
            validations.forEach {
                it.minProperties?.let { min ->
                    "it".identifier().invoke("minProperties", min.literal()).statement()
                }
                it.maxProperties?.let { max ->
                    "it".identifier().invoke("maxProperties", max.literal()).statement()
                }
            }
        }
}

private fun emitCustomConstraintsValidation(
    statement: KotlinExpression,
    validations: List<CustomConstraintsValidation>
): KotlinExpression {
    var result = statement

    validations
        .flatMap { it.constraints }
        .forEach {
            result = result.wrap()
                .invoke("validate", Library.DefaultValidator.identifier().functionReference(it))
        }
    return result
}

fun withDefault(source: KotlinExpression, model: ModelInstance): KotlinExpression {
    val defaultValue = when (model) {
        is EnumModelInstance -> when (model.defaultValue) {
            null -> DefaultValue.None
            else -> DefaultValue.EnumValue(model.ref, model.defaultValue)
        }

        is PrimitiveTypeModelInstance -> when (model.defaultValue) {
            null -> DefaultValue.None
            else -> DefaultValue.StaticValue(model.itemType, model.defaultValue)
        }

        else -> null
    }?.toKotlinExpression()

    return when (defaultValue) {
        null -> source
        else -> source.wrap().invoke("default") {
            defaultValue.statement()
        }
    }
}
