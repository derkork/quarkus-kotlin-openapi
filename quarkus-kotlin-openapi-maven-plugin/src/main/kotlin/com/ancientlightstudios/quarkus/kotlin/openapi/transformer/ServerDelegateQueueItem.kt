package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinInterface
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class ServerDelegateQueueItem(private val config: Config, private val requests: Set<Request>) : QueueItem {

    fun className() = "${config.interfaceName}Delegate".className()

    override fun generate(queue: (QueueItem) -> Unit): KotlinFile {
        val serverInterface = KotlinInterface(className())

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
            val inner = SafeModelQueueItem(config, it).enqueue(queue).className()
            it.containerAsList(inner, false, false)
        }


        val method = KotlinMethod(methodName, true, returnType).apply {
            RequestContainerQueueItem.enqueueRequestContainer(config, request, queue)?.let {
                parameters.add(
                    KotlinParameter(
                        "request".variableName(),
                        "Maybe".rawTypeName().of(it.className().typeName())
                    )
                )
            }
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
