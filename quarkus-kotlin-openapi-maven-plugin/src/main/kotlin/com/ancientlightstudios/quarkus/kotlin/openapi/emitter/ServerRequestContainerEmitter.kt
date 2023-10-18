package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinMember
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry

class ServerRequestContainerEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        suite.requests.forEach {
            if (it.hasInputData()) {
                emitRequestContainer(it)
            }
        }
    }

    private fun EmitterContext.emitRequestContainer(request: Request) {
        kotlinFile(serverPackage(), request.name.extend(postfix = "Request").className()) {
            registerImport(modelPackage(), wildcardImport = true)

            kotlinClass(fileName) {
                request.parameters.forEach {
                    kotlinMember(it.name, it.type.safeType, private = false)
                }

                request.body?.let {
                    kotlinMember("body".variableName(), it.safeType, private = false)
                }
            }
        }.also { generateFile(it) }
    }
}
