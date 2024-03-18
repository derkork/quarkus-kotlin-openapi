package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientErrorResponseClassNameHint.clientErrorResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientHttpResponseClassNameHint.clientHttpResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerClassNameHint.responseContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ConstantName.Companion.rawConstantName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody

class ClientResponseContainerEmitter : CodeEmitter {

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

        kotlinInterface(fileName, sealed = true) {}

        generateHttpResponseClass(this)

        generateErrorResponseInterface(this)
    }

    private fun RequestInspection.generateHttpResponseClass(container: KotlinFile) =
        with(container) {
            val me = request.clientHttpResponseClassName
            kotlinClass(me, sealed = true, interfaces = listOf(fileName)) {
                kotlinMember(
                    "status".variableName(), Misc.RestResponseStatusClass.typeName(), accessModifier = null, open = true
                )
                kotlinMember(
                    "unsafeBody".variableName(), Kotlin.AnyClass.typeName(true), accessModifier = null, open = true
                )

                request.responses.forEach { response ->
                    when (val responseCode = response.responseCode) {
                        is ResponseCode.Default -> emitDefaultResponseClass(me, response.body)
                        is ResponseCode.HttpStatusCode -> emitResponseClass(me, responseCode, response.body)
                    }
                }
            }
        }

    private fun ClassAware.emitDefaultResponseClass(parentClass: ClassName, body: TransformableBody?) {
        val bodyExpression: KotlinExpression = when (body) {
            null -> nullLiteral()
            else -> "safeBody".variableName()
        }

        val baseClass = KotlinBaseClass(parentClass, "status".variableName(), bodyExpression)

        kotlinClass("Default".className(""), asDataClass = true, baseClass = baseClass) {
            kotlinMember(
                "status".variableName(), Misc.RestResponseStatusClass.typeName(), accessModifier = null, override = true
            )
            body?.let {
                val type = it.content.typeUsage.buildValidType()
                kotlinMember("safeBody".variableName(), type, accessModifier = null)
            }
        }
    }

    private fun ClassAware.emitResponseClass(
        parentClass: ClassName, responseCode: ResponseCode.HttpStatusCode, body: TransformableBody?
    ) {
        val bodyExpression: KotlinExpression = when (body) {
            null -> nullLiteral()
            else -> "safeBody".variableName()
        }

        val statusName = responseCode.statusCodeName()
        val status = Misc.RestResponseStatusClass.companionObject().property(statusName.rawConstantName())
        val baseClass = KotlinBaseClass(parentClass, status, bodyExpression)

        val isDataClass = body != null
        kotlinClass(
            responseCode.statusCodeReason().className(""), asDataClass = isDataClass, baseClass = baseClass
        ) {
            body?.let {
                val type = it.content.typeUsage.buildValidType()
                kotlinMember("safeBody".variableName(), type, accessModifier = null)
            }
        }
    }

    private fun RequestInspection.generateErrorResponseInterface(container: KotlinFile) =
        with(container) {
            val me = request.clientErrorResponseClassName
            kotlinInterface(
                me, sealed = true, interfaces = listOf(fileName, Library.IsErrorClass)
            ) {
                generateErrorClass(
                    "RequestErrorTimeout", me, "A timeout occurred when communicating with the server.".literal()
                )
                generateErrorClass("RequestErrorUnreachable", me, "The server could not be reached.".literal())
                generateErrorClass(
                    "RequestErrorConnectionReset",
                    me,
                    "The connection was reset while communicating with the server.".literal()
                )
                generateErrorClass(
                    "RequestErrorUnknown",
                    me,
                    "An unknown error occurred when communicating with the server.".literal()
                ) {
                    kotlinMember("cause".variableName(), Kotlin.ExceptionClass.typeName(), accessModifier = null)
                }
                generateErrorClass(
                    "ResponseError",
                    me,
                    "reason".variableName()
                ) {
                    kotlinMember("reason".variableName(), Kotlin.StringClass.typeName(), accessModifier = null)
                    kotlinMember("response".variableName(), Jakarta.ResponseClass.typeName(), accessModifier = null)
                }
            }

        }

    private fun KotlinInterface.generateErrorClass(
        name: String, parent: ClassName, message: KotlinExpression, block: KotlinClass.() -> Unit = {}
    ) {
        kotlinClass(name.className(""), baseClass = KotlinBaseClass(parent)) {

            kotlinMember(
                "errorMessage".variableName(),
                Kotlin.StringClass.typeName(),
                override = true,
                initializedInConstructor = false,
                accessModifier = null,
                default = message
            )

            block()
        }
    }
}