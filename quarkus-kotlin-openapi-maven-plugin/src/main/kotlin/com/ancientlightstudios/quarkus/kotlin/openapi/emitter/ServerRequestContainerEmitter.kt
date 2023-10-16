package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinMember
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.VariableName.Companion.variableName

class ServerRequestContainerEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite) {
        suite.requests.forEach {
            if (it.hasInputData()) {
                emitRequestContainer(it)
            }
        }
    }

    private fun EmitterContext.emitRequestContainer(request: Request) {
        val fileName = request.name.extend(postfix = "Request").className()
        kotlinFile(serverPackage(), fileName) {
            kotlinClass(fileName) {
                request.parameters.forEach {
                    kotlinMember(it.name, it.type, private = false)
                }

                request.body?.let {
                    kotlinMember("body".variableName(), it, private = false)
                }
            }
        }.also { generateFile(it) }
    }
}
