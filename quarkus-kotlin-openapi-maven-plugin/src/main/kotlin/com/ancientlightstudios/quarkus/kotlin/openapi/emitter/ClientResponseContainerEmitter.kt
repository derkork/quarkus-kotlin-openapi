package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientErrorResponseClassNameHint.clientErrorResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientHttpResponseClassNameHint.clientHttpResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContextClassNameHint.requestContextClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerClassNameHint.responseContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter

class ClientResponseContainerEmitter(private val withTestSupport: Boolean) : CodeEmitter {

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
                    "status".variableName(), Kotlin.IntClass.typeName(), accessModifier = null, open = true
                )
                kotlinMember(
                    "unsafeBody".variableName(), Kotlin.AnyClass.typeName(true), accessModifier = null, open = true
                )

                request.responses.forEach { response ->
                    when (val responseCode = response.responseCode) {
                        is ResponseCode.Default -> emitDefaultResponseClass(me, response.body, response.headers)
                        is ResponseCode.HttpStatusCode -> emitResponseClass(
                            me,
                            responseCode,
                            response.body,
                            response.headers
                        )
                    }
                }
            }
        }

    private fun ClassAware.emitDefaultResponseClass(
        parentClass: ClassName,
        body: TransformableBody?,
        headers: List<TransformableParameter>
    ) {
        val bodyExpression: KotlinExpression = when (body) {
            null -> nullLiteral()
            else -> "safeBody".variableName()
        }

        val baseClass = KotlinBaseClass(parentClass, "status".variableName(), bodyExpression)

        kotlinClass("Default".className(""), baseClass = baseClass) {
            kotlinMember(
                "status".variableName(), Kotlin.IntClass.typeName(), accessModifier = null, override = true
            )

            body?.let {
                val type = it.content.typeUsage.buildValidType()
                kotlinMember("safeBody".variableName(), type, accessModifier = null)
            }

            headers.forEach {
                kotlinMember(
                    it.parameterVariableName, it.content.typeUsage.buildValidType(), accessModifier = null
                )
            }
        }
    }

    private fun ClassAware.emitResponseClass(
        parentClass: ClassName,
        responseCode: ResponseCode.HttpStatusCode,
        body: TransformableBody?,
        headers: List<TransformableParameter>
    ) {
        val bodyExpression: KotlinExpression = when (body) {
            null -> nullLiteral()
            else -> "safeBody".variableName()
        }

        val status = responseCode.value.literal()
        val baseClass = KotlinBaseClass(parentClass, status, bodyExpression)

        kotlinClass(
            responseCode.statusCodeReason().className(""), baseClass = baseClass
        ) {

            headers.forEach {
                kotlinMember(
                    it.parameterVariableName, it.content.typeUsage.buildValidType(), accessModifier = null
                )
            }

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
                    "RequestErrorTimeout", me, Library.IsTimeoutErrorClass,  "A timeout occurred when communicating with the server.".literal()
                )
                generateErrorClass("RequestErrorUnreachable", me,  Library.IsUnreachableErrorClass, "The server could not be reached.".literal())
                generateErrorClass(
                    "RequestErrorConnectionReset",
                    me,
                    Library.IsConnectionResetErrorClass,
                    "The connection was reset while communicating with the server.".literal()
                )
                generateErrorClass(
                    "RequestErrorUnknown",
                    me,
                    Library.IsUnknownErrorClass,
                    "An unknown error occurred when communicating with the server.".literal()
                ) {
                    kotlinMember("cause".variableName(), Kotlin.ExceptionClass.typeName(), accessModifier = null, override = true)
                }

                val responseClass = when(withTestSupport) {
                    true -> RestAssured.ResponseClass
                    false -> Jakarta.ResponseClass
                }

                generateErrorClass(
                    "ResponseError",
                    me,
                    Library.IsResponseErrorClass,
                    "reason".variableName()
                ) {
                    kotlinMember("reason".variableName(), Kotlin.StringClass.typeName(), accessModifier = null)
                    kotlinMember("response".variableName(), responseClass.typeName(), accessModifier = null)
                }
            }

        }

    private fun KotlinInterface.generateErrorClass(
        name: String, parent: ClassName, errorInterface:ClassName, message: KotlinExpression, block: KotlinClass.() -> Unit = {}
    ) {
        kotlinClass(name.className(""), baseClass = KotlinBaseClass(parent), interfaces = listOf(errorInterface)) {

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