package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.SerializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerClassNameHint.responseContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import jakarta.ws.rs.core.Response

class ServerResponseContainerEmitter : CodeEmitter {

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

    private fun RequestInspection.emitContainerFile() = kotlinFile(request.responseContainerClassName) {
        val defaultResponseExists = request.responses.any { it.responseCode == ResponseCode.Default }

        registerImports(Library.AllClasses)
        registerImports(emitterContext.getAdditionalImports())

        kotlinClass(fileName, constructorAccessModifier = KotlinAccessModifier.Private) {
            kotlinMember(
                "response".variableName(),
                Misc.RestResponseClass.typeName().of(Kotlin.Star),
                accessModifier = null
            ) {}
            kotlinCompanion {
                emitGenericStatusMethod(fileName, defaultResponseExists)

                request.responses.forEach {
                    when (val code = it.responseCode) {
                        is ResponseCode.HttpStatusCode -> emitStatusMethod(code, it.body)
                        is ResponseCode.Default -> emitDefaultStatusMethod(it.body)
                    }
                }
            }
        }
    }

    private fun KotlinCompanion.emitGenericStatusMethod(className: ClassName, defaultResponseExists: Boolean) {
        val accessModifier = when (defaultResponseExists) {
            true -> KotlinAccessModifier.Private
            false -> null
        }
        kotlinMethod("status".methodName(), bodyAsAssignment = true, accessModifier = accessModifier) {
            val statusVariable = "status".variableName()
            val typeVariable = "mediaType".variableName()
            val bodyVariable = "body".variableName()
            kotlinParameter(statusVariable, Kotlin.IntClass.typeName())
            kotlinParameter(typeVariable, Kotlin.StringClass.typeName(true), nullLiteral())
            kotlinParameter(bodyVariable, Kotlin.AnyClass.typeName(true), nullLiteral())

            val statusCodeStatement = Misc.RestResponseClass.companionObject()
                .property("Status".rawClassName("", true))
                .invoke("fromStatusCode".rawMethodName(), statusVariable)
            val statement = Misc.ResponseBuilderClass.companionObject()
                .invoke("create".rawMethodName(), statusCodeStatement, bodyVariable)
                .invoke("type".rawMethodName(), typeVariable)
                .invoke("build".rawMethodName())
            invoke(className.constructorName, statement).statement()
        }
    }

    private fun KotlinCompanion.emitStatusMethod(statusCode: ResponseCode.HttpStatusCode, body: TransformableBody?) {
        kotlinMethod(statusCode.value.statusCodeReason().methodName(), bodyAsAssignment = true) {
            if (body != null) {
                val bodyVariable = "body".variableName()
                val typeDefinition = body.content.schema.typeDefinition
                kotlinParameter(bodyVariable, typeDefinition.buildValidType(!body.required))
                val serializationStatement = emitterContext.runEmitter(
                    SerializationStatementEmitter(
                        typeDefinition, !body.required, bodyVariable, body.content.mappedContentType
                    )
                ).resultStatement
                invoke(
                    "status".rawMethodName(),
                    statusCode.value.literal(),
                    body.content.rawContentType.literal(),
                    serializationStatement
                ).statement()
            } else {
                invoke("status".rawMethodName(), statusCode.value.literal()).statement()
            }
        }
    }

    private fun KotlinCompanion.emitDefaultStatusMethod(body: TransformableBody?) {
        kotlinMethod("defaultStatus".methodName(), bodyAsAssignment = true) {
            val statusVariable = "status".variableName()
            kotlinParameter(statusVariable, Kotlin.IntClass.typeName())
            if (body != null) {
                val bodyVariable = "body".variableName()
                val typeDefinition = body.content.schema.typeDefinition
                kotlinParameter(bodyVariable, typeDefinition.buildValidType(!body.required))
                val serializationStatement = emitterContext.runEmitter(
                    SerializationStatementEmitter(
                        typeDefinition, !body.required, bodyVariable, body.content.mappedContentType
                    )
                ).resultStatement
                invoke(
                    "status".rawMethodName(),
                    statusVariable,
                    body.content.rawContentType.literal(),
                    serializationStatement
                ).statement()
            } else {
                invoke("status".rawMethodName(), statusVariable).statement()
            }
        }
    }

    private fun Int.statusCodeReason() = Response.Status.fromStatusCode(this)?.reasonPhrase ?: "status${this}"

}