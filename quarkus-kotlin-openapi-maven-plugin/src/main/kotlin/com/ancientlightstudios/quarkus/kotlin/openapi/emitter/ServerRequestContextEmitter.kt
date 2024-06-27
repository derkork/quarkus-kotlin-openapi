package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.SerializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerClassNameHint.requestContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContextClassNameHint.requestContextClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter

class ServerRequestContextEmitter : CodeEmitter {

    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this

        spec.inspect {
            bundles {
                requests {
                    emitContainerFile().writeFile()
                }
            }
        }
    }

    private fun RequestInspection.emitContainerFile() = kotlinFile(request.requestContextClassName) {
        val defaultResponseExists = request.responses.any { it.responseCode == ResponseCode.Default }

        registerImports(Library.AllClasses)
        registerImports(emitterContext.getAdditionalImports())

        val interfaces = when(defaultResponseExists) {
            true -> emptyList()
            else -> listOf(Library.ResponseWithGenericStatusInterface)
        }

        kotlinClass(fileName, interfaces = interfaces) {
            if (request.hasInputParameter()) {
                val requestType =
                    Library.MaybeClass.typeName().of(request.requestContainerClassName.typeName())
                kotlinMember("request".variableName(), requestType, accessModifier = null)
            }
            kotlinMember("objectMapper".variableName(), Misc.ObjectMapperClass.typeName())

            emitGenericStatusMethod(defaultResponseExists)

            request.responses.forEach {
                when (val code = it.responseCode) {
                    is ResponseCode.HttpStatusCode -> emitStatusMethod(code, it.body, it.headers)
                    is ResponseCode.Default -> emitDefaultStatusMethod(it.body, it.headers)
                }
            }
        }
    }

    private fun KotlinClass.emitGenericStatusMethod(defaultResponseExists: Boolean) {
        val accessModifier = when (defaultResponseExists) {
            true -> KotlinAccessModifier.Private
            false -> null
        }

        val parameterDefaultValue = when(defaultResponseExists) {
            true -> nullLiteral()
            false -> null
        }

        val statusVariable = "status".variableName()
        val typeVariable = "mediaType".variableName()
        val bodyVariable = "body".variableName()
        val headersVariable = "headers".variableName()

        kotlinMethod(
            "status".methodName(), bodyAsAssignment = true, accessModifier = accessModifier,
            returnType = Kotlin.NothingType, override = !defaultResponseExists) {
            kotlinParameter(statusVariable, Kotlin.IntClass.typeName())
            kotlinParameter(typeVariable, Kotlin.StringClass.typeName(true), parameterDefaultValue)
            kotlinParameter(bodyVariable, Kotlin.AnyClass.typeName(true), parameterDefaultValue)
            kotlinParameter(
                headersVariable, Kotlin.PairClass.typeName().of(
                    Kotlin.StringClass.typeName(), Kotlin.AnyClass.typeName(true)
                ), asParameterList = true
            )

            val statement = Misc.ResponseBuilderClass.companionObject()
                .invoke("create".rawMethodName(), statusVariable, genericTypes = listOf(Kotlin.AnyClass.typeName(true)))
                .invoke("entity".rawMethodName(), bodyVariable)
                .wrap()
                .invoke("type".rawMethodName(), typeVariable)
                .invoke("apply".rawMethodName()) {
                    headersVariable.invoke("forEach".rawMethodName()) {
                        invoke(
                            "headers".rawMethodName(),
                            "it".variableName().property("first".variableName()),
                            "it".variableName().property("second".variableName())
                        ).statement()
                    }.statement()
                }
                .invoke("build".rawMethodName())
            invoke(Library.RequestHandledSignalClass.constructorName, statement).throwStatement()
        }
    }

    private fun KotlinClass.emitStatusMethod(
        statusCode: ResponseCode.HttpStatusCode,
        body: TransformableBody?,
        headers: List<TransformableParameter>
    ) {
        kotlinMethod(statusCode.statusCodeReason().methodName(), bodyAsAssignment = true, returnType = Kotlin.NothingType) {
            emitMethodBody(statusCode.value.literal(), body, headers)
        }
    }

    private fun KotlinClass.emitDefaultStatusMethod(
        body: TransformableBody?,
        headers: List<TransformableParameter>
    ) {
        kotlinMethod("defaultStatus".methodName(), bodyAsAssignment = true, returnType = Kotlin.NothingType) {
            val statusVariable = "status".variableName()
            kotlinParameter(statusVariable, Kotlin.IntClass.typeName())
            emitMethodBody(statusVariable, body, headers)
        }
    }

    private fun KotlinMethod.emitMethodBody(
        status: KotlinExpression,
        body: TransformableBody?,
        headers: List<TransformableParameter>
    ) {
        var bodyExpression: KotlinExpression = nullLiteral()
        var mediaTypeExpression: KotlinExpression = nullLiteral()

        if (body != null) {
            val bodyVariable = "body".variableName()
            val typeUsage = body.content.typeUsage
            kotlinParameter(bodyVariable, typeUsage.buildValidType())

            bodyExpression = emitterContext.runEmitter(
                SerializationStatementEmitter(typeUsage, bodyVariable, body.content.mappedContentType)
            ).resultStatement

            if (body.content.mappedContentType == ContentType.ApplicationJson) {
                bodyExpression = bodyExpression.invoke("asString".methodName(), "objectMapper".variableName())
            }

            mediaTypeExpression = body.content.rawContentType.literal()
        }

        val headerExpressions = headers.map {
            kotlinParameter(it.parameterVariableName, it.content.typeUsage.buildValidType())
            val serializationExpression = emitterContext.runEmitter(
                SerializationStatementEmitter(it.content.typeUsage, it.parameterVariableName, it.content.mappedContentType)
            ).resultStatement
            invoke(Kotlin.PairClass.constructorName, it.name.literal(), serializationExpression)
        }

        invoke(
            "status".rawMethodName(),
            status,
            mediaTypeExpression,
            bodyExpression,
            *headerExpressions.toTypedArray()
        ).statement()
    }

}