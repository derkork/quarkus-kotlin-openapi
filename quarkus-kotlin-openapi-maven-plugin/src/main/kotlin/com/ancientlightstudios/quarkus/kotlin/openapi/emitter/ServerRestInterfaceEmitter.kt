package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.emitDeserializationStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.getDeserializationSourceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestBundleInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerClassNameHint.requestContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestMethodNameHint.requestMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerDelegateClassNameHint.serverDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerRestInterfaceClassNameHint.serverRestInterfaceClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.rawVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest

class ServerRestInterfaceEmitter(private val pathPrefix: String) : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.inspect {
            bundles {
                emitRestInterfaceFile()
                    .apply { registerImports(getAdditionalImports()) }
                    .writeFile()
            }
        }
    }

    private fun RequestBundleInspection.emitRestInterfaceFile() = kotlinFile(bundle.serverRestInterfaceClassName) {

        registerImports(Library.AllClasses)

        kotlinClass(fileName) {
            addPathAnnotation(pathPrefix)

            kotlinMember("delegate".variableName(), bundle.serverDelegateClassName.typeName())
            kotlinMember("objectMapper".variableName(), Misc.ObjectMapperClass.typeName())

            requests {
                emitRequest(this@kotlinClass)
            }
        }
    }

    private fun RequestInspection.emitRequest(containerClass: KotlinClass) = with(containerClass) {
        kotlinMethod(request.requestMethodName, true, Misc.RestResponseClass.typeName().of(Kotlin.Star)) {
            addRequestMethodAnnotation(request.method)
            addPathAnnotation(request.path)

            val requestContainerParts = mutableListOf<VariableName>()
            parameters { requestContainerParts.add(emitParameter(parameter)) }
            body { requestContainerParts.add(emitBody(body)) }

            val requestContainerName = emitRequestContainerConversion(request, requestContainerParts)
            emitDelegateInvocation(request, requestContainerName)
        }
    }

    // generates parameters and conversion code for path, query, header and cookie parameters
    private fun KotlinMethod.emitParameter(parameter: TransformableParameter): VariableName {
        val parameterKind = parameter.kind
        val parameterName = parameter.parameterVariableName

        kotlinParameter(parameterName, parameter.schema.getDeserializationSourceType(ContentType.TextPlain)) {
            addSourceAnnotation(parameterKind, parameter.name)
        }

        val statement = invoke(
            Library.MaybeSuccessClass.constructorName,
            "request.${parameterKind.value}.${parameter.name}".literal(),
            parameterName
        ).wrap()

        return emitDeserializationStatement(statement, parameter.schema, ContentType.TextPlain)
            .assignment(parameterName.extend(postfix = "maybe"))
    }

    // generates parameters and conversion for the request body depending on the media type
    private fun KotlinMethod.emitBody(body: TransformableBody): VariableName {
        addConsumesAnnotation(body.content.rawContentType)
        return when (body.content.mappedContentType) {
            ContentType.ApplicationJson -> emitJsonBody(body)
            ContentType.TextPlain -> emitPlainBody(body)
            ContentType.MultipartFormData -> emitMultipartBody(body)
            ContentType.ApplicationFormUrlencoded -> emitFormBody(body)
            ContentType.ApplicationOctetStream -> emitOctetBody(body)
        }
    }

    private fun KotlinMethod.emitJsonBody(body: TransformableBody): VariableName {
        val parameterName = "body".variableName()

        kotlinParameter(parameterName, Kotlin.StringClass.typeName(true))

        val statement =
            invoke(Library.MaybeSuccessClass.constructorName, "request.body".literal(), parameterName).wrap()
                .invoke("asJson".rawMethodName(), "objectMapper".rawVariableName()).wrap()

        return emitDeserializationStatement(statement, body.content.schema, ContentType.ApplicationJson)
            .assignment(parameterName.extend(postfix = "maybe"))
    }

    private fun KotlinMethod.emitPlainBody(body: TransformableBody): VariableName {
        return "plain".variableName()
    }

    private fun KotlinMethod.emitMultipartBody(body: TransformableBody): VariableName {
        return "multi".variableName()
    }

    private fun KotlinMethod.emitFormBody(body: TransformableBody): VariableName {
        return "form".variableName()
    }

    private fun KotlinMethod.emitOctetBody(body: TransformableBody): VariableName {
        return "octed".variableName()
    }

    // generates the conversion of all the input parameter into the request container
    private fun KotlinMethod.emitRequestContainerConversion(
        request: TransformableRequest,
        parts: List<VariableName>
    ): VariableName? {
        if (parts.isEmpty()) {
            return null
        }

        val expressions = parts.map { it.cast(Library.MaybeSuccessClass.typeName()).property("value".variableName()) }

        val maybeParameters = listOf("request".literal()) + parts
        return invoke("maybeAllOf".rawMethodName(), maybeParameters) {
            invoke(request.requestContainerClassName.constructorName, expressions).statement()
        }.assignment("request".variableName())
    }

    // generates the call to the delegate
    private fun KotlinMethod.emitDelegateInvocation(
        request: TransformableRequest,
        requestContainerName: VariableName?
    ) {
        val arguments = when (requestContainerName) {
            null -> emptyList()
            else -> listOf(requestContainerName)
        }

        "delegate".variableName().invoke(request.requestMethodName, arguments)
            .property("response".variableName())
            .returnStatement()
    }

}