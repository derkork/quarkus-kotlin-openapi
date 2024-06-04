package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.DeserializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.SerializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.DeserializationDirectionHint.deserializationDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SerializationDirectionHint.serializationDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.rawVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.WhenExpression.Companion.whenExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.Direction
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.OneOfOption
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.OneOfTypeDefinition

class OneOfModelClassEmitter(private val typeDefinition: OneOfTypeDefinition) :
    CodeEmitter {

    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this

        kotlinFile(typeDefinition.modelName) {
            registerImports(Library.AllClasses)
            registerImports(getAdditionalImports())

            kotlinInterface(fileName, sealed = true) {
                generateSerializeMethods(spec.serializationDirection)

                kotlinCompanion {
                    // TODO: detect collisions if multiple options have the same type (e.g. two ints)
                    typeDefinition.options.forEach {
                        kotlinMethod("of".methodName(), bodyAsAssignment = true) {
                            kotlinParameter("value".variableName(), it.typeUsage.buildValidType())
                            invoke(it.modelName.constructorName, "value".variableName()).statement()
                        }
                    }

                    generateDeserializeMethods(spec.deserializationDirection)
                }

            }

            typeDefinition.options.forEach {
                kotlinClass(it.modelName, asDataClass = true, baseClass = KotlinBaseClass(fileName)) {
                    kotlinMember("value".variableName(), it.typeUsage.buildValidType(), accessModifier = null)

                    generateSerializeMethods(it, spec.serializationDirection)
                }
            }

        }.writeFile()

    }

    private fun KotlinInterface.generateSerializeMethods(serializationDirection: Direction) {
        val types = typeDefinition.getContentTypes(serializationDirection)
        if (types.contains(ContentType.ApplicationJson)) {
            kotlinMethod("asJson".rawMethodName(), returnType = Misc.JsonNodeClass.typeName())
        }
    }

    private fun KotlinCompanion.generateDeserializeMethods(deserializationDirection: Direction) {
        val types = typeDefinition.getContentTypes(deserializationDirection)
            .intersect(listOf(ContentType.ApplicationJson))

        if (types.isNotEmpty()) {
            if (types.contains(ContentType.ApplicationJson)) {
                generateJsonDeserializeMethod()
            }
        }
    }

    private fun KotlinClass.generateSerializeMethods(option: OneOfOption, serializationDirection: Direction) {
        val types = typeDefinition.getContentTypes(serializationDirection)
        if (types.contains(ContentType.ApplicationJson)) {
            generateJsonSerializeMethod(option, typeDefinition.discriminatorProperty?.name)
        }
    }

    // generates a method like this
    //
    // fun asJson(): JsonNode = value?.asJson() ?: NullNode.instance
    //
    // or
    //
    // fun asJson(): JsonNode = value.asJson()
    private fun MethodAware.generateJsonSerializeMethod(option: OneOfOption, discriminatorProperty: VariableName?) {
        kotlinMethod(
            "asJson".rawMethodName(), returnType = Misc.JsonNodeClass.typeName(),
            bodyAsAssignment = true, override = true
        ) {
            var serialization: KotlinExpression = when (discriminatorProperty) {
                null -> "value".variableName()
                else -> "value".variableName()
                    .invoke("copy".methodName(), discriminatorProperty to option.aliases.first().literal())
            }

            serialization = emitterContext.runEmitter(
                SerializationStatementEmitter(option.typeUsage, serialization, ContentType.ApplicationJson)
            ).resultStatement

            if (option.typeUsage.nullable) {
                serialization =
                    serialization.nullFallback(Misc.NullNodeClass.companionObject().property("instance".variableName()))
            }

            serialization.statement()
        }
    }

    // generates a method like this
    //
    // fun Maybe<JsonNode?>.as<ModelName>(): Maybe<<ModelName>?> = onNotNull {
    //
    // }
    private fun MethodAware.generateJsonDeserializeMethod() {
        kotlinMethod(
            typeDefinition.modelName.value.methodName(prefix = "as"),
            returnType = Library.MaybeClass.typeName().of(typeDefinition.modelName.typeName(true)),
            receiverType = Library.MaybeClass.typeName().of(Misc.JsonNodeClass.typeName(true)),
            bodyAsAssignment = true
        ) {
            when (val discriminatorProperty = typeDefinition.discriminatorProperty) {
                null -> generateJsonDeserializationWithoutDescriptor()
                else -> generateJsonDeserializationWithDescriptor(discriminatorProperty.sourceName)
            }
        }
    }

    private fun StatementAware.generateJsonDeserializationWithoutDescriptor() {
        // onSuccess instead of onNotNull in case one of the options is nullable. maybe we can find a better way to handle this
        invoke("onSuccess".methodName()) {
            val baseStatement = "this".rawVariableName()
            val statements = typeDefinition.options.mapIndexed { index, option ->
                val statement = emitterContext.runEmitter(
                    DeserializationStatementEmitter(option.typeUsage, baseStatement, ContentType.ApplicationJson, false)
                ).resultStatement.declaration("option${index}Maybe".variableName())
                statement to option.modelName
            }

            val maybeParameters = listOf("context".variableName(), *statements.map { it.first }.toTypedArray())
            invoke("maybeOneOf".rawMethodName(), maybeParameters) {
                statements.forEach { (variableName, className) ->
                    variableName.invoke("doOnSuccess".methodName()) {
                        invoke(className.constructorName, "it".variableName()).returnStatement("maybeOneOf")
                    }.statement()
                }

                invoke(
                    Kotlin.IllegalStateExceptionClass.constructorName,
                    "this should never happen".literal()
                ).throwStatement()
            }.statement()
        }.statement()
    }

    private fun StatementAware.generateJsonDeserializationWithDescriptor(discriminatorProperty: String) {
        // onSuccess instead of onNotNull in case one of the options is nullable. maybe we can find a better way to handle this
        invoke("onNotNull".methodName()) {
            // renders
            //
            // val discriminator = value.get("<discriminatorName>")?.asText()
            val discriminatorVariable = "value".variableName()
                .invoke("get".methodName(), discriminatorProperty.literal())
                .nullCheck()
                .invoke("asText".methodName())
                .declaration("discriminator".variableName())

            whenExpression(discriminatorVariable) {
                optionBlock(nullLiteral()) {
                    // renders
                    //
                    // failure(ValidationError("discriminator field '<discriminatorName>' is missing", context))
                    InvocationExpression.invoke(
                        "failure".methodName(),
                        InvocationExpression.invoke(
                            Library.ValidationErrorClass.constructorName,
                            "discriminator field '$discriminatorProperty' is missing".literal(),
                            "context".variableName()
                        )
                    ).statement()
                }

                typeDefinition.options.forEach {
                    val aliases = it.aliases.map { it.literal() }
                    optionBlock(*aliases.toTypedArray()) {
                        // renders
                        //
                        // this.<DeserializationStatements>
                        //     .onSuccess { success(<ContainerClass>(value)) }
                        emitterContext.runEmitter(
                            DeserializationStatementEmitter(
                                it.typeUsage, "this".variableName(), ContentType.ApplicationJson, false
                            )
                        ).resultStatement
                            .wrap()
                            .invoke("onSuccess".methodName()) {
                                InvocationExpression.invoke(
                                    "success".methodName(),
                                    InvocationExpression.invoke(it.modelName.constructorName, "value".variableName())
                                ).statement()
                            }
                            .statement()
                    }
                }

                optionBlock("else".variableName()) {
                    // renders
                    //
                    // failure(ValidationError("discriminator field '<discriminatorName>' has invalid value '$discriminator'", context))
                    InvocationExpression.invoke(
                        "failure".methodName(),
                        InvocationExpression.invoke(
                            Library.ValidationErrorClass.constructorName,
                            "discriminator field '$discriminatorProperty' has invalid value '\$discriminator'".literal(),
                            "context".variableName()
                        )
                    ).statement()
                }
            }.statement()

        }.statement()
    }
}
