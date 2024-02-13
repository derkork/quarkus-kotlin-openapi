package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.companionMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.WhenExpression.Companion.whenExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeItem
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class DeserializationMethodEmitter(
    private val typeDefinition: TypeDefinition,
    private val contentType: ContentType
) : CodeEmitter {

    lateinit var generatedMethod: KotlinMethod
    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this

        generatedMethod = when (typeDefinition) {
            is EnumTypeDefinition -> emitForEnumType(typeDefinition)
            is ObjectTypeDefinition -> emitForObjectType(typeDefinition)
            else -> ProbableBug("Unsupported type ${typeDefinition.javaClass} for deserialization method")
        }
    }

    private fun emitForEnumType(typeDefinition: EnumTypeDefinition): KotlinMethod {
        val methodName = typeDefinition.modelName.companionMethod("as ${typeDefinition.modelName.value}")
        return when (contentType) {
            ContentType.TextPlain -> emitPlainForEnumType(typeDefinition, methodName)
            ContentType.ApplicationJson -> emitJsonForEnumType(typeDefinition, methodName)
            else -> ProbableBug("Unsupported content type $contentType for enum deserialization method")
        }
    }

    // if it's an enum type and text/plain, generates a method like this
    //
    // fun Maybe<String?>.as<ModelName>(): Maybe<<ModelName>?> = onNotNull {
    //     when(value) {
    //         <itemValue> -> success(<ModelName>(<itemValue>))
    //         else -> failure(ValidationError("is not a valid value", context))
    //     }
    // }
    //
    // there is an option for every enum value
    private fun emitPlainForEnumType(typeDefinition: EnumTypeDefinition, methodName: MethodName): KotlinMethod {
        val methodReturnType = Library.MaybeClass.typeName().of(typeDefinition.modelName, true)
        val methodReceiverType = Library.MaybeClass.typeName().of(Kotlin.StringClass, true)

        return KotlinMethod(
            methodName,
            returnType = methodReturnType,
            receiverType = methodReceiverType,
            bodyAsAssignment = true
        ).apply {
            invoke("onNotNull".rawMethodName()) {
                whenExpression("value".variableName()) {
                    typeDefinition.items.forEach {
                        generateItemOption(typeDefinition.modelName, it)
                    }
                    generateElseOption(typeDefinition.modelName)
                }.statement()
            }.statement()
        }
    }

    // build something like
    // "first" -> success(SimpleEnum.First)
    private fun WhenOptionAware.generateItemOption(enumName: ClassName, item: EnumTypeItem) {
        optionBlock(item.value) {
            invoke(
                "success".rawMethodName(), enumName.companionObject().property(item.name)
            ).statement()
        }
    }

    // build something like
    // else -> failure(ValidationError("is not a valid value", context))
    private fun WhenOptionAware.generateElseOption(enumName: ClassName) {
        optionBlock("else".variableName()) {
            val validationError = invoke(
                Library.ValidationErrorClass.constructorName,
                "is not a valid value".literal(),
                "context".variableName()
            )
            invoke("failure".rawMethodName(), validationError).statement()
        }
    }

    // if it's an enum type and application/json, generates a method like this
    //
    // @JvmName(name = "as<ModelName>FromJson")
    // fun Maybe<JsonNode?>.as<ModelName>(): Maybe<<ModelName>?> = asString().as<ModelName>()
    private fun emitJsonForEnumType(typeDefinition: EnumTypeDefinition, methodName: MethodName): KotlinMethod {
        val methodReturnType = Library.MaybeClass.typeName().of(typeDefinition.modelName, true)
        val methodReceiverType = Library.MaybeClass.typeName().of(Misc.JsonNodeClass, true)

        return KotlinMethod(
            methodName,
            returnType = methodReturnType,
            receiverType = methodReceiverType,
            bodyAsAssignment = true
        ).apply {
            kotlinAnnotation(Kotlin.JvmNameClass, "name".variableName() to "${methodName.value}FromJson".literal())
            invoke("asString".rawMethodName()).invoke(methodName).statement()
        }
    }

    // TODO: this is the old unsafe->safe conversion
    private fun emitForObjectType(typeDefinition: ObjectTypeDefinition): KotlinMethod {
        val methodName = typeDefinition.modelName.companionMethod("as ${typeDefinition.modelName.value}")
        val methodReturnType = Library.MaybeClass.typeName().of(typeDefinition.modelName, true)
        val methodReceiverType = Library.MaybeClass.typeName().of(Misc.JsonNodeClass, true)

        return KotlinMethod(
            methodName, returnType = methodReturnType, receiverType = methodReceiverType, bodyAsAssignment = true
        ).apply {
            invoke("onNotNull".rawMethodName()) {
                // iterate over all members and create a deserialize statement for each
                val required = typeDefinition.required
                val root = "value".variableName()
                val objectParts = typeDefinition.properties.map {
                    val isPropertyRequired = required.contains(it.sourceName)

                    val statement = root.invoke(
                        "findProperty".rawMethodName(),
                        it.sourceName.literal(),
                        "\${context}.${it.sourceName}".literal()
                    )

                    emitterContext.runEmitter(
                        DeserializationStatementEmitter(
                            it.schema.typeDefinition, !isPropertyRequired, statement, ContentType.ApplicationJson, false
                        )
                    ).resultStatement.assignment("${it.sourceName}Maybe".variableName())
                }

                if (objectParts.isEmpty()) {
                    // just return a new instance
                    invoke(typeDefinition.modelName.constructorName).statement()
                } else {
                    emitterContext.runEmitter(
                        CombineIntoObjectStatementEmitter(
                            "context".variableName(), typeDefinition.modelName, objectParts
                        )
                    ).resultStatement?.statement()
                }
            }.statement()
        }

    }
}