package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.CombineIntoObjectStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.Deserialization.getDeserializationSourceType
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.DeserializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestBundleInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerClassNameHint.requestContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestMethodNameHint.requestMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContextClassNameHint.requestContextClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerDelegateClassNameHint.serverDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerRestInterfaceClassNameHint.serverRestInterfaceClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TryCatchExpression.Companion.tryExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.rawVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.refactoring.AssignContentTypesRefactoring.Companion.getContentTypeForFormPart
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

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
        kotlinMethod(request.requestMethodName, true, Misc.RestResponseClass.typeName().of(Kotlin.Star.typeName())) {
            addRequestMethodAnnotation(request.method)
            addPathAnnotation(request.path)

            val requestContainerParts = mutableListOf<VariableName>()
            parameters { requestContainerParts.add(emitParameter(parameter)) }
            body { requestContainerParts.add(emitBody(body)) }

            val requestContainerName = emitterContext.runEmitter(
                CombineIntoObjectStatementEmitter(
                    "request".literal(), request.requestContainerClassName, requestContainerParts
                )
            ).resultStatement?.declaration("request".variableName())
            emitDelegateInvocation(request, requestContainerName, request.requestContextClassName)
        }
    }

    // generates parameters and conversion code for path, query, header and cookie parameters
    private fun KotlinMethod.emitParameter(parameter: TransformableParameter): VariableName {
        val parameterKind = parameter.kind

        return emitMethodParameter(
            parameter.parameterVariableName,
            parameter.content.typeUsage.getDeserializationSourceType(),
            getSourceAnnotation(parameterKind, parameter.name),
            "request.${parameterKind.value}.${parameter.name}".literal(),
            parameter.content.typeUsage,
            parameter.content.mappedContentType
        )
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
        return emitMethodParameter(
            body.parameterVariableName,
            Kotlin.StringClass.typeName(true),
            null,
            "request.body".literal(),
            body.content.typeUsage,
            ContentType.ApplicationJson
        )
    }

    private fun KotlinMethod.emitPlainBody(body: TransformableBody): VariableName {
        return emitMethodParameter(
            body.parameterVariableName,
            body.content.typeUsage.getDeserializationSourceType(),
            null,
            "request.body".literal(),
            body.content.typeUsage,
            ContentType.TextPlain
        )
    }

    private fun KotlinMethod.emitMultipartBody(body: TransformableBody): VariableName {
        return "multi".variableName()
    }

    private fun KotlinMethod.emitFormBody(body: TransformableBody): VariableName {
        val typeUsage = body.content.typeUsage
        val safeType = typeUsage.type

        if (safeType is ObjectTypeDefinition) {
            // create a parameter for each object property
            val parts = safeType.properties.map {
                val parameter = body.parameterVariableName.extend(prefix = it.sourceName)
                val propertyType = it.typeUsage
                val contentType = getContentTypeForFormPart(propertyType.type)
                val sourceType = typeUsage.getDeserializationSourceType()

                emitMethodParameter(
                    parameter,
                    sourceType,
                    KotlinAnnotation(Jakarta.FormParamAnnotationClass, null to it.sourceName.literal()),
                    "request.body.${it.sourceName}".literal(),
                    propertyType,
                    contentType
                )
            }

            return emitterContext.runEmitter(
                CombineIntoObjectStatementEmitter("request.body".literal(), safeType.modelName, parts)
            ).resultStatement?.declaration(body.parameterVariableName)
                ?: ProbableBug("don't know how to deserialize form object")
        } else {
            // it's a simple type, just create a single parameter
            return emitMethodParameter(
                body.parameterVariableName,
                body.content.typeUsage.getDeserializationSourceType(),
                KotlinAnnotation(Jakarta.FormParamAnnotationClass, null to body.parameterVariableName.value.literal()),
                "request.body".literal(),
                body.content.typeUsage,
                ContentType.TextPlain
            )
        }
    }

    private fun KotlinMethod.emitOctetBody(body: TransformableBody): VariableName {
        return emitMethodParameter(
            body.parameterVariableName,
            Kotlin.ByteArrayClass.typeName(true),
            null,
            "request.body".literal(),
            body.content.typeUsage,
            ContentType.ApplicationOctetStream
        )
    }

    // generates the call to the delegate
    private fun KotlinMethod.emitDelegateInvocation(
        request: TransformableRequest,
        requestContainerName: VariableName?,
        requestContextClassName: ClassName
    ) {
        val arguments = when (requestContainerName) {
            null -> listOf("objectMapper".variableName())
            else -> listOf(requestContainerName, "objectMapper".variableName())
        }

        val requestContextName = invoke(requestContextClassName.constructorName, arguments)
            .declaration("response".variableName())

        tryExpression {
            "delegate".variableName().invoke("apply".methodName()) {
                requestContextName.invoke(request.requestMethodName).statement()
            }.statement()

            val signalName = "requestHandledSignal".rawVariableName()
            catchBlock(Library.RequestHandledSignalClass, signalName) {
                signalName.property("response".variableName()).returnStatement()
            }
        }.statement()
    }

    // adds a new parameter to the current method and generates some deserialization statements for it
    private fun KotlinMethod.emitMethodParameter(
        parameterName: VariableName, parameterType: TypeName, parameterAnnotation: KotlinAnnotation?,
        context: KotlinExpression, typeUsage: TypeUsage, contentType: ContentType
    ): VariableName {
        kotlinParameter(parameterName, parameterType) { parameterAnnotation?.let { addAnnotation(it) } }

        // produces
        //
        // Maybe.Success(<context>, <parameterName>)
        val statement = invoke(Library.MaybeSuccessClass.constructorName, context, parameterName).wrap()

        // produces
        //
        // val <parameterName>Maybe = <statement>
        //     .<deserializationStatement>
        return emitterContext.runEmitter(
            DeserializationStatementEmitter(typeUsage, statement, contentType, true)
        ).resultStatement.declaration(parameterName.extend(postfix = "maybe"))
    }

}