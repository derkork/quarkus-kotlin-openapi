package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.OneOfModelClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.OneOfModelOption

class OneOfModelClassEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<OneOfModelClass>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(model: OneOfModelClass) {
        kotlinFile(model.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinInterface(name, sealed = true) {

                model.features.filterIsInstance<ModelSerializationFeature>().forEach { feature ->
                    getHandler<OneOfModelSerializationHandler, Unit> {
                        installSerializationFeature(model, feature)
                    }
                }

                // TODO
//                if (withTestSupport) {
//                    generateTestMethods()
//                }

                kotlinCompanion {
                    // TODO: generator method

                    model.features.filterIsInstance<ModelDeserializationFeature>().forEach { feature ->
                        getHandler<OneOfModelDeserializationHandler, Unit> {
                            installDeserializationFeature(model, feature)
                        }
                    }
                }
            }

            model.options.forEach {
                kotlinClass(it.name.asTypeName(), asDataClass = true, baseClass = KotlinBaseClass(name)) {
                    kotlinMember("value", it.model.asTypeReference(), accessModifier = null)

                    model.features.filterIsInstance<ModelSerializationFeature>().forEach { feature ->
                        getHandler<OneOfModelSerializationHandler, Unit> {
                            installSerializationFeature(it, model.discriminator?.name, feature)
                        }
                    }

                    kotlinCompanion {
                        // TODO
//                    if (withTestSupport) {
//                        generateUnsafeMethods(spec.serializationDirection)
//                    }
                    }
                }
            }
        }
    }
}

interface OneOfModelSerializationHandler : Handler {

    fun KotlinInterface.installSerializationFeature(model: OneOfModelClass, feature: ModelSerializationFeature):
            HandlerResult<Unit>

    fun KotlinClass.installSerializationFeature(
        model: OneOfModelOption, discriminatorProperty: String?, feature: ModelSerializationFeature
    ): HandlerResult<Unit>

}

interface OneOfModelDeserializationHandler : Handler {

    fun KotlinCompanion.installDeserializationFeature(model: OneOfModelClass, feature: ModelDeserializationFeature):
            HandlerResult<Unit>

}

//
//    private lateinit var emitterContext: EmitterContext
//
//    override fun EmitterContext.emit() {
//        emitterContext = this
//
//        kotlinFile(typeDefinition.modelName) {
//            registerImports(Library.AllClasses)
//            registerImports(getAdditionalImports())
//
//            kotlinInterface(fileName, sealed = true) {
//                generateSerializeMethods(spec.serializationDirection)
//
//                if (withTestSupport) {
//                    generateTestMethods()
//                }
//
//                kotlinCompanion {
//                    // TODO: detect collisions if multiple options have the same type (e.g. two ints)
//                    typeDefinition.options.forEach {
//                        kotlinMethod("of".methodName(), bodyAsAssignment = true) {
//                            kotlinParameter("value".variableName(), it.typeUsage.buildValidType())
//                            invoke(it.modelName.constructorName, "value".variableName()).statement()
//                        }
//                    }
//
//                    generateDeserializeMethods(spec.deserializationDirection)
//                }
//
//            }
//
//            typeDefinition.options.forEach {
//                kotlinClass(it.modelName, asDataClass = true, baseClass = KotlinBaseClass(fileName)) {
//                    kotlinMember("value".variableName(), it.typeUsage.buildValidType(), accessModifier = null)
//
//                    generateSerializeMethods(it, spec.serializationDirection)
//
//                    kotlinCompanion {
//                        generateUnsafeMethods(it, spec.serializationDirection)
//                    }
//                }
//            }
//
//        }.writeFile()
//
//    }
//
//
//
//    // generates a method like this
//    //
//    // fun Maybe<JsonNode?>.as<ModelName>(): Maybe<<ModelName>?> = onNotNull {
//    //
//    // }
//    private fun MethodAware.generateJsonDeserializeMethod() {
//        kotlinMethod(
//            typeDefinition.modelName.value.methodName(prefix = "as"),
//            returnType = Library.MaybeClass.typeName().of(typeDefinition.modelName.typeName(true)),
//            receiverType = Library.MaybeClass.typeName().of(Misc.JsonNodeClass.typeName(true)),
//            bodyAsAssignment = true
//        ) {
//            when (val discriminatorProperty = typeDefinition.discriminatorProperty) {
//                null -> generateJsonDeserializationWithoutDescriptor()
//                else -> generateJsonDeserializationWithDescriptor(discriminatorProperty.sourceName)
//            }
//        }
//    }
//
//    private fun StatementAware.generateJsonDeserializationWithoutDescriptor() {
//        // onSuccess instead of onNotNull in case one of the options is nullable. maybe we can find a better way to handle this
//        invoke("onSuccess".methodName()) {
//            val baseStatement = "this".rawVariableName()
//            val statements = typeDefinition.options.mapIndexed { index, option ->
//                val statement = emitterContext.runEmitter(
//                    DeserializationStatementEmitter(option.typeUsage, baseStatement, ContentType.ApplicationJson, false)
//                ).resultStatement.declaration("option${index}Maybe".variableName())
//                statement to option.modelName
//            }
//
//            val maybeParameters = listOf("context".variableName(), *statements.map { it.first }.toTypedArray())
//            invoke("maybeOneOf".rawMethodName(), maybeParameters) {
//                statements.forEach { (variableName, className) ->
//                    variableName.invoke("doOnSuccess".methodName()) {
//                        invoke(className.constructorName, "it".variableName()).returnStatement("maybeOneOf")
//                    }.statement()
//                }
//
//                invoke(
//                    Kotlin.IllegalStateExceptionClass.constructorName,
//                    "this should never happen".literal()
//                ).throwStatement()
//            }.statement()
//        }.statement()
//    }
//
//    private fun StatementAware.generateJsonDeserializationWithDescriptor(discriminatorProperty: String) {
//        // onSuccess instead of onNotNull in case one of the options is nullable. maybe we can find a better way to handle this
//        invoke("onNotNull".methodName()) {
//            // renders
//            //
//            // val discriminator = value.get("<discriminatorName>")?.asText()
//            val discriminatorVariable = "value".variableName()
//                .invoke("get".methodName(), discriminatorProperty.literal())
//                .nullCheck()
//                .invoke("asText".methodName())
//                .declaration("discriminator".variableName())
//
//            whenExpression(discriminatorVariable) {
//                optionBlock(nullLiteral()) {
//                    // renders
//                    //
//                    // failure(ValidationError("discriminator field '<discriminatorName>' is missing", context))
//                    InvocationExpression.invoke(
//                        "failure".methodName(),
//                        InvocationExpression.invoke(
//                            Library.ValidationErrorClass.constructorName,
//                            "discriminator field '$discriminatorProperty' is missing".literal(),
//                            "context".variableName()
//                        )
//                    ).statement()
//                }
//
//                typeDefinition.options.forEach {
//                    val aliases = it.aliases.map { it.literal() }
//                    optionBlock(*aliases.toTypedArray()) {
//                        // renders
//                        //
//                        // this.<DeserializationStatements>
//                        //     .onSuccess { success(<ContainerClass>(value)) }
//                        emitterContext.runEmitter(
//                            DeserializationStatementEmitter(
//                                it.typeUsage, "this".variableName(), ContentType.ApplicationJson, false
//                            )
//                        ).resultStatement
//                            .wrap()
//                            .invoke("onSuccess".methodName()) {
//                                InvocationExpression.invoke(
//                                    "success".methodName(),
//                                    InvocationExpression.invoke(it.modelName.constructorName, "value".variableName())
//                                ).statement()
//                            }
//                            .statement()
//                    }
//                }
//
//                optionBlock("else".variableName()) {
//                    // renders
//                    //
//                    // failure(ValidationError("discriminator field '<discriminatorName>' has invalid value '$discriminator'", context))
//                    InvocationExpression.invoke(
//                        "failure".methodName(),
//                        InvocationExpression.invoke(
//                            Library.ValidationErrorClass.constructorName,
//                            "discriminator field '$discriminatorProperty' has invalid value '\$discriminator'".literal(),
//                            "context".variableName()
//                        )
//                    ).statement()
//                }
//            }.statement()
//
//        }.statement()
//    }
//
//    private fun KotlinCompanion.generateUnsafeMethods(option: OneOfOption, serializationDirection: Direction) {
//        val types = typeDefinition.getContentTypes(serializationDirection)
//        if (types.contains(ContentType.ApplicationJson)) {
//            generateJsonUnsafeMethod(option)
//        }
//    }
//
//    private fun KotlinCompanion.generateJsonUnsafeMethod(option: OneOfOption) {
//        kotlinMethod(
//            "unsafeJson".methodName(),
//            returnType = Library.UnsafeJsonClass.typeName().of(option.modelName.typeName()),
//            bodyAsAssignment = true
//        ) {
//            kotlinParameter(
//                "value".variableName(),
//                option.typeUsage.buildUnsafeJsonType(),
//                expression = nullLiteral()
//            )
//
//            var serialization = emitterContext.runEmitter(
//                UnsafeSerializationStatementEmitter(
//                    option.typeUsage,
//                    "value".variableName(),
//                    ContentType.ApplicationJson
//                )
//            ).resultStatement
//
//            serialization =
//                serialization.nullFallback(Misc.NullNodeClass.companionObject().property("instance".variableName()))
//            invoke(Library.UnsafeJsonClass.constructorName, serialization).statement()
//        }
//    }
//
//    private fun KotlinInterface.generateTestMethods() {
//
//        /**
//         *     fun isBook(block: Book.() -> Unit) = when(this) {
//         *             is OneOfWithDiscriminatorBook ->  value.apply(block)
//         *             else -> throw AssertionFailedError("Assertion failed.", OneOfWithDiscriminatorBook::class.java.name, javaClass.name)
//         *         }
//         *     }
//         *
//         */
//
//        typeDefinition.options.forEach { option ->
//            kotlinMethod(option.modelName.value.methodName(prefix = "is"), bodyAsAssignment = true) {
//                kotlinParameter("block".variableName(), TypeName.DelegateTypeName(option.typeUsage.buildValidType(), returnType = Kotlin.UnitType))
//
//                whenExpression("this".variableName()) {
//                    optionBlock(AssignableExpression.assignable(option.modelName)) {
//                        "value".variableName().invoke("apply".methodName(), "block".variableName()).statement()
//                    }
//                    optionBlock("else".variableName()) {
//                        InvocationExpression.invoke(
//                            Misc.AssertionFailedErrorClass.constructorName,
//                            "Assertion failed.".literal(),
//                            option.modelName.javaClass().property("name".variableName()),
//                            "javaClass".variableName().property("name".variableName())
//                        ).throwStatement()
//                    }
//                }.statement()
//            }
//        }
//    }
//
//}
