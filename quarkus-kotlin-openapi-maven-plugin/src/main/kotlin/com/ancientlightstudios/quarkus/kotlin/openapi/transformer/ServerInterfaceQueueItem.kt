package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Name
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class ServerInterfaceQueueItem(val requests: Set<Request>) : QueueItem() {


    override fun generate(config: Config, queue: (QueueItem) -> Unit): KotlinFile {

        val serverInterface = KotlinClass(Name.ClassName(config.interfaceName + "Server"))
        serverInterface.annotations.add(
            Name.ClassName("Path"),
            Name.VariableName("value") to "/"
        )

        requests.forEach {
            generateRequest(serverInterface, it, queue)
        }

        return KotlinFile(
            serverInterface, config.packageName + ".server", listOf(
                "javax.ws.rs.Path",
            )
        )
    }

    private fun generateRequest(serverInterface: KotlinClass, request: Request, queue: (QueueItem) -> Unit) {
        val methodName = Name.MethodName(request.operationId)
        val returnType = request.returnType?.let {
            val queueItem = SafeModelQueueItem(it)
            queue(queueItem)
            queueItem.className()
        }

        val method = KotlinMethod(methodName, returnType, emptyList(), null)
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
