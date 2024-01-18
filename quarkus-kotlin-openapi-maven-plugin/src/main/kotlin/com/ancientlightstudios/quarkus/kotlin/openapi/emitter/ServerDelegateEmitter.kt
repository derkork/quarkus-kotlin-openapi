package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestBundleInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerClassNameHint.requestContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestMethodNameHint.requestMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerClassNameHint.responseContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerDelegateClassNameHint.serverDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName

class ServerDelegateEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.inspect {
            bundles {
                emitDelegateFile().writeFile()
            }
        }
    }

    private fun RequestBundleInspection.emitDelegateFile() = kotlinFile(bundle.serverDelegateClassName) {
        registerImports(Library.AllClasses)

        kotlinInterface(fileName) {
            requests {
                kotlinMethod(request.requestMethodName, true, request.responseContainerClassName.typeName()) {
                    if (request.hasInputParameter()) {
                        val requestType =
                            Library.MaybeClass.typeName().of(request.requestContainerClassName.typeName())
                        kotlinParameter("request".variableName(), requestType)
                    }
                }
            }
        }
    }
}
