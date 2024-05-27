package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestBundleInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestMethodNameHint.requestMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContextClassNameHint.requestContextClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerDelegateClassNameHint.serverDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName

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
                kotlinMethod(
                    request.requestMethodName, true, Kotlin.NothingType,
                    request.requestContextClassName.typeName()
                )
            }
        }
    }
}
