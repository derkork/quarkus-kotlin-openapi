package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.DeserializationDirectionHint.deserializationDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SerializationDirectionHint.serializationDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.Direction
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeDefinition

class EnumModelClassEmitter(private val typeDefinition: EnumTypeDefinition) : CodeEmitter {

    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this

        kotlinFile(typeDefinition.modelName) {
            registerImports(Library.AllClasses)
            registerImports(getAdditionalImports())

            kotlinEnum(fileName) {
                kotlinMember(
                    "value".variableName(),
                    typeDefinition.baseType.typeName(),
                    accessModifier = null
                )
                typeDefinition.items.forEach {
                    kotlinEnumItem(it.name, it.value)
                }

                generateSerializeMethods(spec.serializationDirection)
                generateDeserializeMethods(spec.deserializationDirection)

            }
        }.writeFile()
    }

    private fun KotlinEnum.generateSerializeMethods(serializationDirection: Direction) {
        val types = typeDefinition.getContentTypes(serializationDirection)
        if (types.contains(ContentType.TextPlain)) {
            generatePlainSerializeMethod()
        }

        if (types.contains(ContentType.ApplicationJson)) {
            generateJsonSerializeMethod()
        }
    }

    private fun KotlinEnum.generateDeserializeMethods(deserializationDirection: Direction) {
        val types = typeDefinition.getContentTypes(deserializationDirection)
            // the only formats which are important for deserialization right now
            .intersect(listOf(ContentType.TextPlain, ContentType.ApplicationJson))

        if (types.isNotEmpty()) {
            val methodName = typeDefinition.modelName.value.methodName(prefix = "as")
            kotlinCompanion {
                // plain deserialization is reused by the other content types, so it is always generated
                generatePlainDeserializeMethod(methodName)

                if (types.contains(ContentType.ApplicationJson)) {
                    generateJsonDeserializeMethod(methodName)
                }
            }
        }
    }

    // generates a method like this
    //
    // fun asString(): String = value.asString()
    private fun MethodAware.generatePlainSerializeMethod() {
        kotlinMethod("asString".rawMethodName(), returnType = Kotlin.StringClass.typeName(), bodyAsAssignment = true) {
            "value".variableName().invoke("asString".rawMethodName()).statement()
        }
    }

    // generates a method like this
    //
    // fun asJson(): JsonNode = value.asJson()
    private fun MethodAware.generateJsonSerializeMethod() {
        kotlinMethod("asJson".rawMethodName(), returnType = Misc.JsonNodeClass.typeName(), bodyAsAssignment = true) {
            "value".variableName().invoke("asJson".rawMethodName()).statement()
        }
    }

    // generates a method like this
    //
    // fun Maybe<String?>.as<ModelName>(): Maybe<<ModelName>?> = onNotNull {
    //     when(value) {
    //         <itemValue> -> success(<ModelName>(<itemValue>))
    //         else -> failure(ValidationError("is not a valid value", context))
    //     }
    // }
    //
    // there is an option for every enum value
    private fun MethodAware.generatePlainDeserializeMethod(methodName: MethodName) {
        kotlinMethod(
            methodName,
            returnType = Library.MaybeClass.typeName().of(typeDefinition.modelName.typeName(true)),
            receiverType = Library.MaybeClass.typeName().of(Kotlin.StringClass.typeName(true)),
            bodyAsAssignment = true
        ) {
            invoke("onNotNull".rawMethodName()) {
                WhenExpression.whenExpression("value".variableName()) {
                    typeDefinition.items.forEach {
                        // build something like
                        // "first" -> success(SimpleEnum.First)
                        optionBlock(it.value) {
                            InvocationExpression.invoke(
                                "success".rawMethodName(), typeDefinition.modelName.companionObject().property(it.name)
                            ).statement()
                        }

                    }

                    // build something like
                    // else -> failure(ValidationError("is not a valid value", context))
                    optionBlock("else".variableName()) {
                        val validationError = InvocationExpression.invoke(
                            Library.ValidationErrorClass.constructorName,
                            "is not a valid value".literal(),
                            "context".variableName()
                        )
                        InvocationExpression.invoke("failure".rawMethodName(), validationError).statement()
                    }
                }.statement()
            }.statement()
        }
    }

    // generates a method like this
    //
    // @JvmName(name = "as<ModelName>FromJson")
    // fun Maybe<JsonNode?>.as<ModelName>(): Maybe<<ModelName>?> = asString().as<ModelName>()
    private fun MethodAware.generateJsonDeserializeMethod(methodName: MethodName) {
        kotlinMethod(
            methodName,
            returnType = Library.MaybeClass.typeName().of(typeDefinition.modelName.typeName(true)),
            receiverType = Library.MaybeClass.typeName().of(Misc.JsonNodeClass.typeName(true)),
            bodyAsAssignment = true
        ) {
            kotlinAnnotation(Kotlin.JvmNameClass, "name".variableName() to "${methodName.value}FromJson".literal())
            invoke("asString".rawMethodName()).invoke(methodName).statement()
        }
    }
}
