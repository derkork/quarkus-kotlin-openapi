package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import TransformerContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinInterface
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class ServerDelegateQueueItem(private val requests: Set<Request>, private val context: TransformerContext) : QueueItem {

    fun className() = "${context.config.interfaceName}Delegate".className()

    override fun generate(): KotlinFile {
        val serverInterface = KotlinInterface(className())

        requests.forEach {
            generateRequest(serverInterface, it)
        }

        return KotlinFile(serverInterface, "${context.config.packageName}.server").apply {
            imports.addAll(modelImports(context.config))
            imports.addAll(libraryImports())
        }
    }

    private fun generateRequest(serverInterface: KotlinInterface, request: Request) {
        val methodName = request.operationId.methodName()

        val returnType = ResponseContainerQueueItem(request, context).apply {
            context.enqueue(this)
        }.className()

        serverInterface.addMethod(methodName, true, returnType.typeName()).apply {
            context.requestContainerFor(request)?.let {
                addParameter("request".variableName(), "Maybe".rawTypeName().of(it.className().typeName()))
            }
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
