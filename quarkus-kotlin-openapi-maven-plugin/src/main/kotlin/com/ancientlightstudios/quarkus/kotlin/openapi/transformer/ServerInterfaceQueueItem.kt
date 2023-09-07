package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.*

class ServerInterfaceQueueItem(private val config: Config, private val requests: Set<Request>) : QueueItem {

    override fun generate(queue: (QueueItem) -> Unit): KotlinFile {
        val delegateQueueItem = ServerDelegateQueueItem(config, requests)
        queue(delegateQueueItem)

        val serverInterface = KotlinClass("${config.interfaceName}Server".className()).apply {
            annotations.addPath("/")

            parameters.add(KotlinMember("delegate".variableName(), delegateQueueItem.className().typeName()))
            parameters.add(KotlinMember("objectMapper".variableName(), "ObjectMapper".rawTypeName()))
        }

        requests.forEach {
            generateRequest(serverInterface, it, queue)
        }

        return KotlinFile(serverInterface, "${config.packageName}.server").apply {
            imports.addAll(jakartaRestImports())
            imports.addAll(jacksonImports())
            imports.addAll(modelImports(config))
            imports.addAll(libraryImports())
        }
    }

    private fun generateRequest(serverInterface: KotlinClass, request: Request, queue: (QueueItem) -> Unit) {
        val methodName = request.operationId.methodName()
        val returnType = request.returnType?.let {
            val inner = SafeModelQueueItem(config, it).enqueue(queue).className()
            it.containerAsList(inner, false, false)
        }
        val methodBody = KotlinStatementList()

        val method = KotlinMethod(methodName, true, returnType, methodBody).apply {
            annotations.add(request.method.name.rawClassName()) // use name as it is
            annotations.addPath(request.path)
        }

        val parameterNames = mutableListOf<Pair<VariableName, VariableName>>()
        request.parameters.forEach {
            val parameter = KotlinParameter(it.name.variableName(), "String".rawTypeName(true))
            parameter.annotations.addParam(it.kind, it.name)
            method.parameters.add(parameter)

            methodBody.generateMaybeTransformStatement(it.type, it.name, it.validationInfo, it.kind, queue)
        }

        request.body?.let {
            val parameter = KotlinParameter("body".variableName(), "String".rawTypeName(true))
            method.parameters.add(parameter)

            methodBody.generateMaybeTransformStatement(it.type, "body", it.validationInfo, null, queue)
        }

        serverInterface.methods.add(method)
    }

    private fun KotlinStatementList.generateMaybeTransformStatement(
        type: SchemaRef, parameterName: String, validationInfo: ValidationInfo,
        kind: ParameterKind?, queue: (QueueItem) -> Unit
    ) {
        val maybeVariable = "maybe $parameterName".variableName()
        val parameterVariable = parameterName.variableName()
        val contextName = kind?.let { "request.${it.name.lowercase()}.$parameterName" } ?: "request.$parameterName"

        val statement = when (type.resolve()) {
            is Schema.EnumSchema -> {
                val parameterType = SafeModelQueueItem(config, type).enqueue(queue).className()
                MaybeEnumTransformStatement(
                    maybeVariable, parameterVariable, contextName, parameterType.typeName(), validationInfo
                )
            }

            is Schema.PrimitiveTypeSchema -> {
                val parameterType = SafeModelQueueItem(config, type).enqueue(queue).className()
                MaybePrimitiveTransformStatement(
                    maybeVariable, parameterVariable, contextName, parameterType.typeName(), validationInfo
                )
            }

            is Schema.ArraySchema -> {
                val parameterType = UnsafeModelQueueItem(config, type).enqueue(queue).className()
                MaybeArrayTransformStatement(
                    maybeVariable, parameterVariable, contextName,
                    type.containerAsArray(parameterType, true, false),
                    validationInfo
                )
            }

            else -> {
                val parameterType = UnsafeModelQueueItem(config, type).enqueue(queue).className()
                MaybeObjectTransformStatement(
                    maybeVariable, parameterVariable, contextName, parameterType.typeName(), validationInfo
                )
            }
        }

        this.statements.add(statement)
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
