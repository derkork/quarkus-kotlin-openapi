package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class ServerInterfaceQueueItem(private val requests: Set<Request>) : QueueItem() {

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
                "jakarta.ws.rs.GET",
                "jakarta.ws.rs.POST",
                "jakarta.ws.rs.PUT",
                "jakarta.ws.rs.DELETE",
                "jakarta.ws.rs.Path",
                "jakarta.ws.rs.PathParam",
                "jakarta.ws.rs.QueryParam",
                "jakarta.ws.rs.HeaderParam",
                "jakarta.ws.rs.CookieParam",
                "com.fasterxml.jackson.databind.ObjectMapper",
                "com.ancientlightstudios.example.model.*",
                "com.ancientlightstudios.quarkus.kotlin.openapi.*"
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

        val method = KotlinMethod(methodName, true, returnType, emptyList(), KotlinCode("// TODO"))
        method.annotations.add(Name.ClassName(request.method.name))
        method.annotations.add(
            Name.ClassName("Path"),
            Name.VariableName("value") to request.path
        )

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
