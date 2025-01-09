package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Library
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.asTypeReference
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinMember
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRestController

class ServerRestControllerEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ServerRestController>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(restController: ServerRestController) {
        kotlinFile(restController.name.asTypeName()) {
            registerImports(Library.All)

            kotlinClass(name) {
                kotlinMember("delegate", restController.delegate.name.asTypeReference())
                kotlinMember("dependencyVogel", restController.dependencyVogel.name.asTypeReference())
            }
        }
    }
}

//class ServerRestInterfaceEmitter(private val pathPrefix: String) : CodeEmitter {
//
//    private lateinit var emitterContext: EmitterContext
//
//    override fun EmitterContext.emit() {
//        emitterContext = this
//        spec.inspect {
//            bundles {
//                emitRestInterfaceFile()
//                    .writeFile()
//            }
//        }
//    }
//
//    private fun RequestBundleInspection.emitRestInterfaceFile() = kotlinFile(bundle.serverRestInterfaceClassName) {
//        registerImports(Library.AllClasses)
//        registerImports(emitterContext.getAdditionalImports())
//
//        kotlinClass(fileName) {
//            if (emitterContext.config.onlyProfile.isNotBlank()) {
//                kotlinAnnotation(Quarkus.IfBuildProfileAnnotationClass, emitterContext.config.onlyProfile.literal())
//            }
//
//            if (emitterContext.config.exceptProfile.isNotBlank()) {
//                kotlinAnnotation(Quarkus.UnlessBuildProfileAnnotationClass, emitterContext.config.exceptProfile.literal())
//            }
//
//            addPathAnnotation(pathPrefix)
//
//            kotlinMember("delegate".variableName(), bundle.serverDelegateClassName.typeName())
//            kotlinMember("objectMapper".variableName(), Misc.ObjectMapperClass.typeName())
//
//            // produces:
//            //
//            // private val log = LoggerFactory.getLogger(FeaturesDefaultValueServer::class.java)
//            val loggerExpression = Misc.LoggerFactoryClass.companionObject().invoke("getLogger".methodName(), fileName.javaClass())
//            kotlinMember("log".variableName(), Misc.LoggerClass.typeName(), default = loggerExpression, initializedInConstructor = false)
//
//            requests {
//                emitRequest(this@kotlinClass)
//            }
//        }
//    }
//
//    private fun RequestInspection.emitRequest(containerClass: KotlinClass) = with(containerClass) {
//        kotlinMethod(request.requestMethodName, true, Misc.RestResponseClass.typeName().of(Kotlin.Star.typeName())) {
//            addRequestMethodAnnotation(request.method)
//            addPathAnnotation(request.path)
//
//            // produces:
//            //
//            // MDC.put("request-method", "<method>")
//            // MDC.put("request-path", "<path>")
//            // return withContext(MDCContext()) {
//            //    ...
//            // }
//            Misc.MDCClass.companionObject()
//                .invoke("put".methodName(), "request-method".literal(), request.method.value.literal())
//                .statement()
//            Misc.MDCClass.companionObject()
//                .invoke("put".methodName(), "request-path".literal(), request.path.literal())
//                .statement()
//            invoke("withContext".methodName("kotlinx.coroutines"), invoke(Misc.MDCContextClass.constructorName)) {
//                // produces:
//                //
//                // log.info("[<method>] <path> processing request")
//                "log".variableName()
//                    .invoke("info".methodName(), "[${request.method.value}] ${request.path} - processing request.".literal())
//                    .statement()
//
//                val requestContainerParts = mutableListOf<VariableName>()
//                parameters { requestContainerParts.add(emitParameter(parameter)) }
//                body { requestContainerParts.add(emitBody(body)) }
//                kotlinParameter("headers".variableName(), Jakarta.HttpHeadersClass.typeName()) {
//                    kotlinAnnotation(Jakarta.ContextAnnotationClass)
//                }
//
//                val requestContainerName = emitterContext.runEmitter(
//                    CombineIntoObjectStatementEmitter(
//                        "request".literal(), request.requestContainerClassName, listOf(), requestContainerParts
//                    )
//                ).resultStatement?.declaration("request".variableName())
//                emitDelegateInvocation(request, requestContainerName, request.requestContextClassName)
//            }.returnStatement()
//        }
//    }
//
//    // generates parameters and conversion code for path, query, header and cookie parameters
//    private fun KotlinMethod.emitParameter(parameter: OpenApiParameter): VariableName {
//        val parameterKind = parameter.kind
//
//        return emitMethodParameter(
//            parameter.parameterVariableName,
//            parameter.content.typeUsage.getDeserializationSourceType(),
//            getSourceAnnotation(parameterKind, parameter.name),
//            "request.${parameterKind.value}.${parameter.name}".literal(),
//            parameter.content.typeUsage,
//            parameter.content.mappedContentType
//        )
//    }
//
//    // generates parameters and conversion for the request body depending on the media type
//    private fun KotlinMethod.emitBody(body: OpenApiBody): VariableName {
//        addConsumesAnnotation(body.content.rawContentType)
//        return when (body.content.mappedContentType) {
//            ContentType.ApplicationJson -> emitJsonBody(body)
//            ContentType.TextPlain -> emitPlainBody(body)
////            ContentType.MultipartFormData -> emitMultipartBody(body)
//            ContentType.ApplicationFormUrlencoded -> emitFormBody(body)
//            ContentType.ApplicationOctetStream -> emitOctetBody(body)
//        }
//    }
//
//    private fun KotlinMethod.emitJsonBody(body: OpenApiBody): VariableName {
//        return emitMethodParameter(
//            body.parameterVariableName,
//            Kotlin.StringClass.typeName(true),
//            null,
//            "request.body".literal(),
//            body.content.typeUsage,
//            ContentType.ApplicationJson
//        )
//    }
//
//    private fun KotlinMethod.emitPlainBody(body: OpenApiBody): VariableName {
//        return emitMethodParameter(
//            body.parameterVariableName,
//            body.content.typeUsage.getDeserializationSourceType(),
//            null,
//            "request.body".literal(),
//            body.content.typeUsage,
//            ContentType.TextPlain
//        )
//    }
//
//    private fun KotlinMethod.emitMultipartBody(body: OpenApiBody): VariableName {
//        return "multi".variableName()
//    }
//
//    private fun KotlinMethod.emitFormBody(body: OpenApiBody): VariableName {
//        val typeUsage = body.content.typeUsage
//        val safeType = typeUsage.type
//
//        if (safeType is ObjectTypeDefinition) {
//            // create a parameter for each object property
//            val parts = safeType.properties.map {
//                val parameter = body.parameterVariableName.extend(prefix = it.sourceName)
//                val propertyType = it.typeUsage
//                val contentType = getContentTypeForFormPart(propertyType.type)
//                val sourceType = typeUsage.getDeserializationSourceType()
//
//                emitMethodParameter(
//                    parameter,
//                    sourceType,
//                    KotlinAnnotation(Jakarta.FormParamAnnotationClass, null to it.sourceName.literal()),
//                    "request.body.${it.sourceName}".literal(),
//                    propertyType,
//                    contentType
//                )
//            }
//
//            return emitterContext.runEmitter(
//                CombineIntoObjectStatementEmitter("request.body".literal(), safeType.modelName, listOf(), parts)
//            ).resultStatement?.declaration(body.parameterVariableName)
//                ?: ProbableBug("don't know how to deserialize form object")
//        } else {
//            // it's a simple type, just create a single parameter
//            return emitMethodParameter(
//                body.parameterVariableName,
//                body.content.typeUsage.getDeserializationSourceType(),
//                KotlinAnnotation(Jakarta.FormParamAnnotationClass, null to body.parameterVariableName.value.literal()),
//                "request.body".literal(),
//                body.content.typeUsage,
//                ContentType.TextPlain
//            )
//        }
//    }
//
//    private fun KotlinMethod.emitOctetBody(body: OpenApiBody): VariableName {
//        return emitMethodParameter(
//            body.parameterVariableName,
//            Kotlin.ByteArrayClass.typeName(true),
//            null,
//            "request.body".literal(),
//            body.content.typeUsage,
//            ContentType.ApplicationOctetStream
//        )
//    }
//
//    // generates the call to the delegate
//    private fun StatementAware.emitDelegateInvocation(
//        request: OpenApiRequest,
//        requestContainerName: VariableName?,
//        requestContextClassName: ClassName
//    ) {
//        val arguments = when (requestContainerName) {
//            null -> listOf("objectMapper".variableName(), "headers".variableName())
//            else -> listOf(requestContainerName, "objectMapper".variableName(), "headers".variableName())
//        }
//
//        val requestContextName = invoke(requestContextClassName.constructorName, arguments)
//            .declaration("context".variableName())
//
//        tryExpression {
//            "delegate".variableName().invoke("run".methodName()) {
//                requestContextName.invoke(request.requestMethodName).statement()
//            }.statement()
//
//            val signalName = "requestHandledSignal".rawVariableName()
//            catchBlock(Library.RequestHandledSignalClass, signalName) {
//                // produces:
//                //
//                // log.info("[<method>] <path> responding with status code ${requestHandledSignal.response.status}")
//                "log".variableName()
//                    .invoke("info".methodName(), "[${request.method.value}] ${request.path} - responding with status code \${requestHandledSignal.response.status}.".literal())
//                    .statement()
//                signalName.property("response".variableName()).statement()
//            }
//            catchBlock(Kotlin.ExceptionClass, "e".variableName()) {
//                // produces:
//                //
//                // log.error("[<method>] <path> unexpected exception occurred. Please check the delegate implementation to make sure it never throws an exception. Response is now undefined.")
//                // throw e
//                "log".variableName()
//                    .invoke("error".methodName(), "[${request.method.value}] ${request.path} - unexpected exception occurred. Please check the delegate implementation to make sure it never throws an exception. Response is now undefined.".literal())
//                    .statement()
//                "e".variableName().throwStatement()
//            }
//        }.statement()
//    }
//
//    // adds a new parameter to the current method and generates some deserialization statements for it
//    private fun KotlinMethod.emitMethodParameter(
//        parameterName: VariableName, parameterType: TypeName, parameterAnnotation: KotlinAnnotation?,
//        context: KotlinExpression, typeUsage: TypeUsage, contentType: ContentType
//    ): VariableName {
//        kotlinParameter(parameterName, parameterType) { parameterAnnotation?.let { addAnnotation(it) } }
//
//        // produces
//        //
//        // Maybe.Success(<context>, <parameterName>)
//        val statement = invoke(Library.MaybeSuccessClass.constructorName, context, parameterName).wrap()
//
//        // produces
//        //
//        // val <parameterName>Maybe = <statement>
//        //     .<deserializationStatement>
//        return emitterContext.runEmitter(
//            DeserializationStatementEmitter(typeUsage, statement, contentType, true)
//        ).resultStatement.declaration(parameterName.extend(postfix = "maybe"))
//    }
//
//}