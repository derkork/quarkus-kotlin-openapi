package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.ArrayValidation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.CustomConstraintsValidation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.NumberValidation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.StringValidation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition

class ValidationStatementEmitter(
    private val typeDefinition: TypeDefinition,
    baseStatement: KotlinExpression,
) : CodeEmitter {

    var resultStatement = baseStatement

    override fun EmitterContext.emit() {
        val validations = typeDefinition.validations
        resultStatement = emitStringValidation(resultStatement, validations.filterIsInstance<StringValidation>())
        resultStatement = emitNumberValidation(resultStatement, validations.filterIsInstance<NumberValidation>())
        resultStatement = emitArrayValidation(resultStatement, validations.filterIsInstance<ArrayValidation>())
        resultStatement = emitCustomConstraintsValidation(
            resultStatement, validations.filterIsInstance<CustomConstraintsValidation>()
        )
    }

    private fun emitStringValidation(
        statement: KotlinExpression,
        validations: List<StringValidation>
    ): KotlinExpression {
        if (validations.isEmpty()) {
            return statement
        }

        return statement.wrap()
            .invoke("validateString".rawMethodName()) {
                validations.forEach {
                    it.minLength?.let {
                        "it".variableName().invoke("minLength".rawMethodName(), it.literal()).statement()
                    }
                    it.maxLength?.let {
                        "it".variableName().invoke("maxLength".rawMethodName(), it.literal()).statement()
                    }
                    it.pattern?.let { "it".variableName().invoke("pattern".rawMethodName(), it.literal()).statement() }
                }
            }
    }

    private fun emitNumberValidation(
        statement: KotlinExpression,
        validations: List<NumberValidation>
    ): KotlinExpression {
        if (validations.isEmpty()) {
            return statement
        }

        return statement.wrap()
            .invoke("validateNumber".rawMethodName()) {
                validations.forEach {
                    it.minimum?.let {
                        "it".variableName()
                            .invoke("minimum".rawMethodName(), it.value.literal(), it.exclusive.literal()).statement()
                    }
                    it.maximum?.let {
                        "it".variableName()
                            .invoke("maximum".rawMethodName(), it.value.literal(), it.exclusive.literal()).statement()
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
            .invoke("validateList".rawMethodName()) {
                validations.forEach {
                    it.minSize?.let { "it".variableName().invoke("minSize".rawMethodName(), it.literal()).statement() }
                    it.maxSize?.let { "it".variableName().invoke("maxSize".rawMethodName(), it.literal()).statement() }
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
                    .invoke(
                        "validate".rawMethodName(),
                        functionReference(Library.DefaultValidatorClass, it.rawMethodName())
                    )
            }
        return result
    }

}