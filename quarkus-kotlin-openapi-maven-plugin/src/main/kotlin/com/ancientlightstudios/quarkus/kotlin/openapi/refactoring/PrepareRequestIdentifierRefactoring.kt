package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerClassNameHint.requestContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestMethodNameHint.requestMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerClassNameHint.responseContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest

class PrepareRequestIdentifierRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                requests {
                    val name = generateName(request)
                    request.requestMethodName = name.methodName()
                    request.requestContainerClassName = name.className(serverPackage, postfix = "Request")
                    request.responseContainerClassName = name.className(serverPackage, postfix = "Response")

                    parameters {
                        parameter.parameterVariableName = parameter.name.variableName()
                    }

                    responses {
                        headers {
                            header.parameterVariableName = header.name.variableName()
                        }
                    }
                }
            }
        }
    }

    private fun generateName(request: TransformableRequest): String {
        val operationId = request.operationId
        return if (operationId.isNullOrBlank()) {
            "${request.method.name} ${request.path}"
        } else {
            operationId.trim()
        }
    }

}