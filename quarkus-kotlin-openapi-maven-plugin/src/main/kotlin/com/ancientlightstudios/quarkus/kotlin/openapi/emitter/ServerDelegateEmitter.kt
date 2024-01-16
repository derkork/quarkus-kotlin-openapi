package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestBundleInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerNameHint.requestContainerName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestNameHint.requestName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerContainerHint.responseContainerName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerDelegateNameHint.serverDelegateName
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

    private fun RequestBundleInspection.emitDelegateFile() = kotlinFile(bundle.serverDelegateName) {
        registerImports(Library.AllClasses)

        kotlinInterface(fileName) {
            requests {
                kotlinMethod(request.requestName, true, request.responseContainerName.typeName()) {
                    if (request.hasInputParameter()) {
                        val requestType =
                            Library.MaybeClass.typeName().of(request.requestContainerName.typeName())
                        kotlinParameter("request".variableName(), requestType)
                    }
                }
            }
        }
    }
}
