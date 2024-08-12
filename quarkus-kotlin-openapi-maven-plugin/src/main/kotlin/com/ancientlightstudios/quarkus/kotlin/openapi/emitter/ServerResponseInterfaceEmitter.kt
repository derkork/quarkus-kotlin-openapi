package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseInterfaceNameHint.responseInterfaceName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableResponse

class ServerResponseInterfaceEmitter : CodeEmitter {

    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this
        val responseInterfaces = mutableMapOf<ClassName, TransformableResponse>()

        spec.inspect {
            bundles {
                requests {
                    responses {
                        response.responseInterfaceName?.let {
                            responseInterfaces.putIfAbsent(it, response)
                        }
                    }
                }
            }
        }

        responseInterfaces.forEach { (className, response) ->
            response.emitInterfaceFile(className)
                .writeFile()
        }
    }

    private fun TransformableResponse.emitInterfaceFile(className: ClassName) = kotlinFile(className) {
        registerImports(Library.AllClasses)
        registerImports(emitterContext.getAdditionalImports())

        kotlinInterface(fileName) {

            when (val code = responseCode) {
                is ResponseCode.HttpStatusCode -> emitStatusMethod(code, body, headers)
                is ResponseCode.Default -> emitDefaultStatusMethod(body, headers)
            }

        }
    }

    private fun KotlinInterface.emitStatusMethod(
        statusCode: ResponseCode.HttpStatusCode,
        body: TransformableBody?,
        headers: List<TransformableParameter>
    ) {
        kotlinMethod(statusCode.statusCodeReason().methodName(), returnType = Kotlin.NothingType) {
            emitMethodBody(body, headers)
        }
    }

    private fun KotlinInterface.emitDefaultStatusMethod(
        body: TransformableBody?,
        headers: List<TransformableParameter>
    ) {
        kotlinMethod("defaultStatus".methodName(), returnType = Kotlin.NothingType) {
            kotlinParameter("status".variableName(), Kotlin.IntClass.typeName())
            emitMethodBody(body, headers)
        }

    }

    private fun KotlinMethod.emitMethodBody(
        body: TransformableBody?,
        headers: List<TransformableParameter>
    ) {
        if (body != null) {
            val typeUsage = body.content.typeUsage
            kotlinParameter("body".variableName(), typeUsage.buildValidType())
        }

        headers.forEach {
            kotlinParameter(it.parameterVariableName, it.content.typeUsage.buildValidType())
        }
    }
}