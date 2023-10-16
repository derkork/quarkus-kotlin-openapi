package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinInterface
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.VariableName.Companion.variableName

class ServerDelegateEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite) {
        val fileName = suite.name.extend(postfix = "Delegate")
        kotlinFile(serverPackage(), fileName) {
            registerImport("com.ancientlightstudios.quarkus.kotlin.openapi.*")

            kotlinInterface(fileName) {
                suite.requests.forEach {
                    val responseType = it.name.extend(postfix = "Response").typeName()
                    kotlinMethod(it.name, true, responseType) {
                        if (it.hasInputData()) {
                            val requestType = "Maybe".rawTypeName().of(it.name.extend(postfix = "Request").typeName())
                            kotlinParameter("request".variableName(), requestType)
                        }
                    }
                }
            }
        }.also { generateFile(it) }
    }
}