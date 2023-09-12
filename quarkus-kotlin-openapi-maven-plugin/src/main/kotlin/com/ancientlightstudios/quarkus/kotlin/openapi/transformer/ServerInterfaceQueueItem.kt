package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import TransformerContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NestedPathExpression.Companion.nested
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.*

class ServerInterfaceQueueItem(private val requests: Set<Request>, private val context: TransformerContext) : QueueItem {

    override fun generate(): KotlinFile {
        val delegateQueueItem = ServerDelegateQueueItem( requests, context).apply {
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
        val builderTransform =  RequestBuilderTransformStatement(methodName, requestContainer?.className())

        request.parameters.forEach {
            method.addParameter(it.name.variableName(), "String".rawTypeName(true)).apply {
                annotations.addParam(it.kind, it.name)
            }

            generateMaybeTransformStatement(it.type, it.name, it.validationInfo, it.kind, builderTransform)
                .addTo(method)
        }

        request.body?.let {
            method.addParameter("body".variableName(), "String".rawTypeName(true))

            generateMaybeTransformStatement(it.type, "body", it.validationInfo, null, builderTransform)
                .addTo(method)
        }

        builderTransform.addTo(method)
    }

    private fun generateMaybeTransformStatement(
        type: SchemaRef,
        parameterName: String,
        validationInfo: ValidationInfo,
        kind: ParameterKind?,
        builderTransformStatement: RequestBuilderTransformStatement
    ): KotlinStatement {
        val contextName = kind
            ?.let { "request.${it.name.lowercase()}.$parameterName".stringExpression() }
            ?: "request.$parameterName".stringExpression()
        val (maybeVariable, statement) = convertToMaybe(
            context,
            type,
            validationInfo,
            contextName,
            parameterName.variableName()
        )

        val inner = context.safeModelFor(type).className()
        val finalType = type.containerAsList(inner, innerNullable = false, outerNullable = !validationInfo.required)

        builderTransformStatement.registerParameter(parameterName, maybeVariable)
        return statement
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
