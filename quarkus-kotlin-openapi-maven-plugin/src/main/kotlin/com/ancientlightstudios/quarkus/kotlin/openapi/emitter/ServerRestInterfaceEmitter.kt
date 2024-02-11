package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.CombineIntoObjectStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.DeserializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.DeserializationStatementEmitter.Companion.getDeserializationSourceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestBundleInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerClassNameHint.requestContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestMethodNameHint.requestMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerDelegateClassNameHint.serverDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerRestInterfaceClassNameHint.serverRestInterfaceClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
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

    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this
        spec.inspect {
            bundles {
                emitRestInterfaceFile()
                    .writeFile()
            }
        }
    }

    private fun RequestBundleInspection.emitRestInterfaceFile() = kotlinFile(bundle.serverRestInterfaceClassName) {
        registerImports(Library.AllClasses)
        registerImports(emitterContext.getAdditionalImports())

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

            val requestContainerName = emitterContext.runEmitter(
                CombineIntoObjectStatementEmitter(
                    "request".literal(), request.requestContainerClassName, requestContainerParts
                )
            ).resultStatement?.assignment("request".variableName())
            emitDelegateInvocation(request, requestContainerName)
        }
    }

    // generates parameters and conversion code for path, query, header and cookie parameters
    private fun KotlinMethod.emitParameter(parameter: TransformableParameter): VariableName {
        val parameterKind = parameter.kind
        val parameterName = parameter.parameterVariableName

        kotlinParameter(parameterName, parameter.typeDefinition.getDeserializationSourceType()) {
            addSourceAnnotation(parameterKind, parameter.name)
        }

        val statement = invoke(
            Library.MaybeSuccessClass.constructorName,
            "request.${parameterKind.value}.${parameter.name}".literal(),
            parameterName
        ).wrap()

        return emitterContext.runEmitter(
            DeserializationStatementEmitter(parameter.typeDefinition, statement, ContentType.TextPlain)
        ).resultStatement.assignment(parameterName.extend(postfix = "maybe"))
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

        return emitterContext.runEmitter(
            DeserializationStatementEmitter(body.content.typeDefinition, statement, ContentType.ApplicationJson)
        ).resultStatement.assignment(parameterName.extend(postfix = "maybe"))
    }

    private fun KotlinMethod.emitPlainBody(body: TransformableBody): VariableName {
        val parameterName = "body".variableName()

        kotlinParameter(parameterName, body.content.typeDefinition.getDeserializationSourceType())

        val statement =
            invoke(Library.MaybeSuccessClass.constructorName, "request.body".literal(), parameterName).wrap()

        return emitterContext.runEmitter(
            DeserializationStatementEmitter(body.content.typeDefinition, statement, ContentType.TextPlain)
        ).resultStatement.assignment(parameterName.extend(postfix = "maybe"))
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