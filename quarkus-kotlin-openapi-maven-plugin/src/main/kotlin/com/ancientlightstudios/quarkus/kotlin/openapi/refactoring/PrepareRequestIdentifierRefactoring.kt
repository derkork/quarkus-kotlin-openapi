package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerNameHint.requestContainerName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestNameHint.requestName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerContainerHint.responseContainerName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest

class PrepareRequestIdentifierRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                requests {
                    val name = generateName(request)
                    request.requestName = name.methodName()
                    request.requestContainerName = name.className(serverPackage, postfix = "Request")
                    request.responseContainerName = name.className(serverPackage, postfix = "Response")

                    // TODO: add names to the name registry to avoid collisions
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