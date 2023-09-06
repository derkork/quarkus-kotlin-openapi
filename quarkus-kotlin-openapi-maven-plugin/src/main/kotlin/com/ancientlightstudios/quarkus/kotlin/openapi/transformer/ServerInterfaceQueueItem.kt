package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class ServerInterfaceQueueItem(private val requests: Set<Request>) : QueueItem() {

    override fun generate(config: Config, queue: (QueueItem) -> Unit): KotlinFile {
        val serverInterface = KotlinClass("${config.interfaceName}Server".className())
        serverInterface.annotations.addPath("/")

        requests.forEach {
            generateRequest(serverInterface, it, queue)
        }

        return KotlinFile(serverInterface, "${config.packageName}.server").apply {
            addJakartaRestImports()
            addJacksonImports()
            addModelImports(config)
            addLibraryImports()
        }
    }

    private fun generateRequest(serverInterface: KotlinClass, request: Request, queue: (QueueItem) -> Unit) {
        val methodName = request.operationId.methodName()
        val returnType = request.returnType?.let {
            val queueItem = SafeModelQueueItem(it)
            queue(queueItem)
            queueItem.className()
        }

        val method = KotlinMethod(methodName, true, returnType, KotlinCode("// TODO"))
        method.annotations.add(request.method.name.rawClassName()) // use name as it is
        method.annotations.addPath(request.path)


        request.parameters.forEach {
            val parameter = KotlinParameter(it.name.variableName(), "String".className(), true)
            parameter.annotations.addParam(it.kind, it.name)
            method.parameters.add(parameter)
        }

        request.bodyType?.let {
            val parameter = KotlinParameter("body".variableName(), "String".className(), true)
            method.parameters.add(parameter)
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
