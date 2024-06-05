package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientErrorResponseClassNameHint.clientErrorResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientHttpResponseClassNameHint.clientHttpResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestBuilderClassNameHint.requestBuilderClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerClassNameHint.requestContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestMethodNameHint.requestMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContextClassNameHint.requestContextClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerClassNameHint.responseContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseValidatorClassNameHint.responseValidatorClassName
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
                    request.requestContainerClassName = name.className(interfacePackage, postfix = "Request")
                    request.requestContextClassName = name.className(interfacePackage, postfix = "Context")
                    request.requestBuilderClassName = name.className(interfacePackage, postfix = "Builder")
                    request.responseContainerClassName = name.className(interfacePackage, postfix = "Response")
                    request.responseValidatorClassName = name.className(interfacePackage, postfix = "Validator")
                    request.clientHttpResponseClassName = name.className(interfacePackage, postfix = "HttpResponse")
                    request.clientErrorResponseClassName = name.className(interfacePackage, postfix = "Error")

                    parameters {
                        parameter.parameterVariableName = parameter.name.variableName()
                    }

                    body {
                        body.parameterVariableName = "body".variableName()
                    }

                    responses {
                        headers {
                            header.parameterVariableName = header.name.variableName()
                        }

                        body {
                            body.parameterVariableName = "body".variableName()
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