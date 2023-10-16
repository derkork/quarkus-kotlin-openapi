package com.ancientlightstudios.quarkus.kotlin.openapi.transformerold

import TransformerContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class ServerInterfaceQueueItem(private val requests: Set<Request>, private val context: TransformerContext) :
    QueueItem {

    override fun generate(): KotlinFile {
        val delegateQueueItem = ServerDelegateQueueItem(requests, context).apply {
            context.enqueue(this)
        }

        val serverInterface = KotlinClass("${context.config.interfaceName}Server".className()).apply {
            addPathAnnotation("/")

            addMember("delegate".variableName(), delegateQueueItem.className().typeName())
            addMember("objectMapper".variableName(), "ObjectMapper".rawTypeName())
        }

        requests.forEach {
            generateRequest(serverInterface, it)
        }

        return KotlinFile(serverInterface, "${context.config.packageName}.server").apply {
            imports.addAll(jakartaRestImports())
            imports.addAll(jacksonImports())
            imports.addAll(modelImports(context.config))
            imports.addAll(libraryImports())
        }
    }

    private fun generateRequest(serverInterface: KotlinClass, request: Request) {
        val methodName = request.operationId.methodName()

        val method = serverInterface.addMethod(methodName, true, "RestResponse<*>".rawTypeName()).apply {
            annotations.add(request.method.name.rawClassName()) // use name as it is
            annotations.addPath(request.path)
        }

        val requestContainer = context.requestContainerFor(request)
        val builderTransform = RequestBuilderTransformStatement(methodName, requestContainer?.className())

        request.parameters.forEach {
            val paramType = it.type.containerAsList("String".rawClassName(), true, false)
            method.addParameter(it.name.variableName(), paramType).apply {
                annotations.addParam(it.kind, it.name)
            }
            method.addTransformStatement(
                it.name, it.type, it.validationInfo,
                "request.${it.kind.name.lowercase()}.${it.name}".stringExpression(), context, false
            ).also(builderTransform::addParameter)
        }

        request.body?.let {
            method.addParameter("body".variableName(), "String".rawTypeName(true))
            method.addTransformStatement(
                "body", it.type, it.validationInfo,
                "request.body".stringExpression(), context, true
            ).also(builderTransform::addParameter)
        }

        builderTransform.addTo(method)
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
