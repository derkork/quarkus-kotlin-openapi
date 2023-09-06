package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinInterface
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class ServerDelegateQueueItem(private val requests: Set<Request>) : QueueItem() {

    override fun generate(config: Config, queue: (QueueItem) -> Unit): KotlinFile {
        val serverInterface = KotlinInterface("${config.interfaceName}Delegate".className())

        requests.forEach {
            generateRequest(serverInterface, it, queue)
        }

        return KotlinFile(serverInterface, "${config.packageName}.server").apply {
            imports.addAll(modelImports(config))
            imports.addAll(libraryImports())
        }
    }

    private fun generateRequest(serverInterface: KotlinInterface, request: Request, queue: (QueueItem) -> Unit) {
        val methodName = request.operationId.methodName()
        val returnType = request.returnType?.let {
            val queueItem = SafeModelQueueItem(it)
            queue(queueItem)
            queueItem.className()
        }

        val method = KotlinMethod(methodName, true, returnType).apply {
            // TODO: right datatype
            parameters.add(KotlinParameter("request".variableName(), "Maybe".className()))
        }

        serverInterface.methods.add(method)
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
