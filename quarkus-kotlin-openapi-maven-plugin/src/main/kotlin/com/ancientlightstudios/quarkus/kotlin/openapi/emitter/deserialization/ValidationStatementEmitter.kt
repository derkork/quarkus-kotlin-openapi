package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.CollectionTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.PrimitiveTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition

class ValidationStatementEmitter(
    private val typeDefinition: TypeDefinition,
    baseStatement: KotlinExpression,
) : CodeEmitter {

    var resultStatement = baseStatement

    override fun EmitterContext.emit() {
        val validations = typeDefinition.validations

        if (typeDefinition is PrimitiveTypeDefinition) {
            resultStatement = emitStringValidation(resultStatement, validations.filterIsInstance<StringValidation>())
            resultStatement = emitNumberValidation(
                resultStatement, validations.filterIsInstance<NumberValidation>(), typeDefinition.baseType
            )
        }

        if (typeDefinition is CollectionTypeDefinition) {
            resultStatement = emitArrayValidation(resultStatement, validations.filterIsInstance<ArrayValidation>())
        }

        if (typeDefinition is ObjectTypeDefinition) {
            resultStatement = emitPropertyValidation(resultStatement, validations.filterIsInstance<PropertiesValidation>())
        }

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
        validations: List<NumberValidation>,
        baseType: ClassName
    ): KotlinExpression {
        if (validations.isEmpty()) {
            return statement
        }

        return statement.wrap()
            .invoke("validateNumber".rawMethodName()) {
                validations.forEach {
                    it.minimum?.let {
                        "it".variableName()
                            // TODO: this should be handled by the refactoring the same way as for default values
                            .invoke("minimum".rawMethodName(), baseType.literalFor(it.value), it.exclusive.literal()).statement()
                    }
                    it.maximum?.let {
                        "it".variableName()
                            .invoke("maximum".rawMethodName(), baseType.literalFor(it.value), it.exclusive.literal()).statement()
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

    private fun emitPropertyValidation(
        statement: KotlinExpression,
        validations: List<PropertiesValidation>
    ): KotlinExpression {
        if (validations.isEmpty()) {
            return statement
        }

        return statement.wrap()
            .invoke("validateProperties".rawMethodName()) {
                validations.forEach {
                    it.minProperties?.let { "it".variableName().invoke("minProperties".rawMethodName(), it.literal()).statement() }
                    it.maxProperties?.let { "it".variableName().invoke("maxProperties".rawMethodName(), it.literal()).statement() }
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