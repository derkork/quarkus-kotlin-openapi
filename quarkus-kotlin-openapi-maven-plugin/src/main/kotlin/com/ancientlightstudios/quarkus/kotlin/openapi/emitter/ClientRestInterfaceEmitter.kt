package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.CombineIntoObjectStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.DeserializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.SerializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestBundleInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientDelegateClassNameHint.clientDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientErrorResponseClassNameHint.clientErrorResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientHttpResponseClassNameHint.clientHttpResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientRestInterfaceClassNameHint.clientRestInterfaceClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestMethodNameHint.requestMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerClassNameHint.responseContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TryCatchExpression.Companion.tryExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.rawVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.WhenExpression.Companion.whenExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.refactoring.AssignContentTypesRefactoring.Companion.getContentTypeForFormPart

class ClientRestInterfaceEmitter : CodeEmitter {

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

    private fun RequestBundleInspection.emitRestInterfaceFile() = kotlinFile(bundle.clientRestInterfaceClassName) {
        registerImports(Library.AllClasses)
        registerImports(emitterContext.getAdditionalImports())

        kotlinClass(fileName) {
            kotlinAnnotation(Jakarta.ApplicationScopedClass)

            kotlinMember("delegate".variableName(), bundle.clientDelegateClassName.typeName()) {
                kotlinAnnotation(Misc.RestClientClass)
            }
            kotlinMember("objectMapper".variableName(), Misc.ObjectMapperClass.typeName())

            requests {
                emitRequest(this@kotlinClass)
            }
        }
    }

    private fun RequestInspection.emitRequest(containerClass: KotlinClass) = with(containerClass) {
        val successClass = request.clientHttpResponseClassName
        val errorClass = request.clientErrorResponseClassName

        kotlinMethod(request.requestMethodName, true, request.responseContainerClassName.typeName()) {

            val requestContainerParts = mutableListOf<VariableName>()
            tryExpression {
                parameters { requestContainerParts.add(emitParameter(this@kotlinMethod, parameter)) }
                body { requestContainerParts.addAll(emitBody(this@kotlinMethod, body)) }

                tryExpression {
                    // produces
                    // delegate.<methodName>(<serializedParameters ...>).toResponse()
                    "delegate".rawVariableName().invoke(request.requestMethodName, requestContainerParts)
                        .invoke("toResponse".rawMethodName()).statement()

                    // produces
                    // catch (e: WebApplicationException) {
                    //     e.response
                    // }
                    catchBlock(Jakarta.WebApplicationExceptionClass) {
                        "e".variableName().property("response".variableName()).statement()
                    }
                }.declaration("response".rawVariableName())

                // produces
                // val statusCode = response.status
                val statusCode = "response".variableName()
                    .property("status".variableName())
                    .declaration("statusCode".variableName())

                // produces
                // val responseMaybe: Maybe<[ResponseContainerClass]> = when (statusCode) {
                //     ...
                // }
                val responseMaybe = "responseMaybe".rawVariableName()
                whenExpression(statusCode) {
                    // generate options for all known status codes
                    request.responses.filter { it.responseCode is ResponseCode.HttpStatusCode }.forEach {
                        generateKnownResponseOption(
                            successClass,
                            it.responseCode as ResponseCode.HttpStatusCode,
                            it.body,
                            it.headers
                        )
                    }

                    // generate option for the default status or fallback otherwise
                    val defaultResponse = request.responses.firstOrNull { it.responseCode == ResponseCode.Default }
                    when (defaultResponse) {
                        null -> generateFallbackResponseOption(errorClass)
                        else -> generateDefaultResponseOption(
                            successClass, defaultResponse.body, defaultResponse.headers
                        )
                    }
                }.declaration(
                    responseMaybe,
                    typeName = Library.MaybeClass.typeName().of(request.responseContainerClassName.typeName())
                )


                // produces
                // when(responseMaybe) {
                //    ...
                // }
                whenExpression(responseMaybe) {
                    // produces
                    // is Maybe.Success -> responseMaybe.value
                    optionBlock(AssignableExpression.assignable(Library.MaybeSuccessClass)) {
                        responseMaybe.property("value".rawVariableName()).statement()
                    }

                    // produces
                    // is Maybe.Failure -> {
                    //     val errors = responseMaybe.errors.joinToString { "${it.path}: ${it.message}" }
                    //     <ResponseObject>(errors, response)
                    // }
                    optionBlock(AssignableExpression.assignable(Library.MaybeFailureClass)) {
                        responseMaybe.property("errors".variableName())
                            .invoke("joinToString".rawMethodName()) {
                                "\${it.path}: \${it.message}".literal().statement()
                            }.declaration("errors".variableName())

                        InvocationExpression.invoke(
                            errorClass.rawNested("ResponseError").constructorName,
                            "errors".variableName(),
                            "response".rawVariableName()
                        ).statement()
                    }
                }.statement()

                // produces
                // catch (_: TimeoutException) {
                //     AddMovieRatingError.RequestErrorTimeout()
                // }
                catchBlock(Misc.TimeoutExceptionClass, ignoreVariable = true) {
                    // otherwise the tryExpression will be picked as the receiver which will produce a compiler error
                    // due to the dsl annotation or an endless recursion without the annotation
                    InvocationExpression.invoke(errorClass.rawNested("RequestErrorTimeout").constructorName).statement()
                }

                // produces
                // catch (e: Exception) {
                //     AddMovieRatingError.RequestErrorUnknown(e)
                // }
                catchBlock(Kotlin.ExceptionClass) {
                    InvocationExpression.invoke(
                        errorClass.rawNested("RequestErrorUnknown").constructorName, "e".variableName()
                    ).statement()
                }
            }.returnStatement()
        }
    }

    private fun TryCatchExpression.emitParameter(
        method: KotlinMethod,
        parameter: TransformableParameter
    ): VariableName {
        val parameterName = parameter.parameterVariableName
        val typeUsage = parameter.content.typeUsage
        val default = defaultParameterExpression(typeUsage)
        method.kotlinParameter(parameterName, typeUsage.buildValidType(), default)

        val statement = emitterContext.runEmitter(
            SerializationStatementEmitter(typeUsage, parameterName, parameter.content.mappedContentType)
        ).resultStatement

        // TODO: it's now almost the same as for the body. re-use stuff
        return when (parameter.content.mappedContentType) {
            ContentType.ApplicationJson -> statement.invoke("asString".methodName(), "objectMapper".variableName())

            else -> statement
        }.declaration(parameterName.extend(postfix = "Payload"))
    }

    // generates parameters and conversion for the request body depending on the media type
    private fun TryCatchExpression.emitBody(method: KotlinMethod, body: TransformableBody): List<VariableName> {
        return when (body.content.mappedContentType) {
            ContentType.ApplicationJson -> listOf(emitJsonBody(method, body))
            ContentType.TextPlain -> listOf(emitPlainBody(method, body))
//            ContentType.MultipartFormData -> emitMultipartBody(method, body)
            ContentType.ApplicationFormUrlencoded -> emitFormBody(method, body)
            ContentType.ApplicationOctetStream -> listOf(emitOctetBody(method, body))
        }
    }

    private fun TryCatchExpression.emitJsonBody(method: KotlinMethod, body: TransformableBody): VariableName {
        val parameterName = body.parameterVariableName
        val typeUsage = body.content.typeUsage
        val default = defaultParameterExpression(typeUsage)

        method.kotlinParameter(parameterName, typeUsage.buildValidType(), default)

        val jsonNode = emitterContext.runEmitter(
            SerializationStatementEmitter(typeUsage, parameterName, body.content.mappedContentType)
        ).resultStatement

        return jsonNode.invoke("asString".methodName(), "objectMapper".variableName())
            .declaration(parameterName.extend(postfix = "Payload"))
    }

    private fun TryCatchExpression.emitPlainBody(method: KotlinMethod, body: TransformableBody): VariableName {
        val parameterName = body.parameterVariableName
        val typeUsage = body.content.typeUsage
        val default = defaultParameterExpression(typeUsage)

        method.kotlinParameter(parameterName, typeUsage.buildValidType(), default)
        return emitterContext.runEmitter(
            SerializationStatementEmitter(typeUsage, parameterName, body.content.mappedContentType)
        ).resultStatement.declaration(parameterName.extend(postfix = "Payload"))
    }

    private fun TryCatchExpression.emitMultipartBody(
        method: KotlinMethod,
        body: TransformableBody
    ): List<VariableName> {
        val default = defaultParameterExpression(body.content.typeUsage)
        return listOf("multi".variableName())
    }

    private fun TryCatchExpression.emitFormBody(method: KotlinMethod, body: TransformableBody): List<VariableName> {
        val parameterName = body.parameterVariableName
        val typeUsage = body.content.typeUsage
        val safeType = typeUsage.type
        val default = defaultParameterExpression(typeUsage)

        method.kotlinParameter(parameterName, typeUsage.buildValidType(), default)

        if (safeType is ObjectTypeDefinition) {
            val statement = if (typeUsage.isNullable()) {
                parameterName.nullCheck()
            } else {
                parameterName
            }

            return safeType.properties.map {
                val propertyType = it.typeUsage
                val contentType = getContentTypeForFormPart(propertyType.type)
                val propertyStatement = statement.property(it.name)
                emitterContext.runEmitter(
                    SerializationStatementEmitter(
                        propertyType, propertyStatement, contentType
                    )
                ).resultStatement.declaration(parameterName.extend(postfix = "${it.sourceName} Payload"))
            }
        } else {
            return listOf(
                emitterContext.runEmitter(
                    SerializationStatementEmitter(typeUsage, parameterName, body.content.mappedContentType)
                ).resultStatement.declaration(parameterName.extend(postfix = "Payload"))
            )
        }
    }

    private fun TryCatchExpression.emitOctetBody(method: KotlinMethod, body: TransformableBody): VariableName {
        val parameterName = body.parameterVariableName
        val default = defaultParameterExpression(body.content.typeUsage)
        method.kotlinParameter(
            parameterName,
            Kotlin.ByteArrayClass.typeName(body.content.typeUsage.isNullable()),
            default
        )
        return parameterName
    }

    private fun WhenOptionAware.generateKnownResponseOption(
        responseClass: ClassName, statusCode: ResponseCode.HttpStatusCode, body: TransformableBody?,
        headers: List<TransformableParameter>
    ) {
        val optionValue = statusCode.value.literal()
        generateResponseOption(
            responseClass.nested(statusCode.statusCodeReason()), optionValue, false, body, headers
        )
    }

    private fun WhenOptionAware.generateDefaultResponseOption(
        responseClass: ClassName, body: TransformableBody?, headers: List<TransformableParameter>
    ) {
        generateResponseOption(
            responseClass.rawNested("Default"), "else".variableName(), true, body, headers
        )
    }

    // build something like
    //
    // with a body
    //
    // RestResponse.Status.<ResponseName> -> {
    //
    // }
    //
    // without a body
    //
    // RestResponse.Status.<ResponseName> -> Maybe.Success("response.body", <ResponseObject>)
    private fun WhenOptionAware.generateResponseOption(
        responseClass: ClassName, optionValue: KotlinExpression, withStatusCode: Boolean, body: TransformableBody?,
        headers: List<TransformableParameter>
    ) {
        val additionalParameter = when (withStatusCode) {
            true -> listOf("statusCode".variableName())
            else -> listOf()
        }

        optionBlock(optionValue) {
            val responseContainerParts = mutableListOf<VariableName>()

            headers.forEach {
                responseContainerParts.add(emitHeaderParameter(it))
            }

            if (body != null) {
                // produces
                // val entity = when(response.hasEntity()) {
                //     true -> response.readEntity(ByteArray::class.java)
                //     false-> null
                // }
                val entity = whenExpression("response".variableName().invoke("hasEntity".methodName())) {
                    optionBlock(true.literal()) {
                        var statement: KotlinExpression = "response".variableName()
                            .invoke("readEntity".rawMethodName(), Kotlin.ByteArrayClass.javaClass())

                        // TODO: we probably need different target types here (e.g. for binary)
                        if (body.content.mappedContentType != ContentType.ApplicationOctetStream) {
                            // TODO: different encodings
                            statement = statement.nullCheck().invoke("decodeToString".methodName())
                        }

                        statement.statement()
                    }
                    optionBlock(false.literal()) {
                        nullLiteral().statement()
                    }
                }.declaration("entity".variableName())

                val statement = invoke(Library.MaybeSuccessClass.constructorName, "response.body".literal(), entity)

                // adds content-type specific deserialization steps to the statement
                responseContainerParts.add(
                    emitterContext.runEmitter(
                        DeserializationStatementEmitter(
                            body.content.typeUsage, statement, body.content.mappedContentType, true
                        )
                    ).resultStatement.declaration(body.parameterVariableName.extend(postfix = "maybe"))
                )
            }

            if (responseContainerParts.isNotEmpty()) {
                emitterContext.runEmitter(
                    CombineIntoObjectStatementEmitter(
                        "response".literal(), responseClass, additionalParameter, responseContainerParts
                    )
                ).resultStatement?.statement()
            } else {
                invoke(
                    Library.MaybeSuccessClass.constructorName,
                    "response.body".literal(),
                    invoke(responseClass.constructorName, *additionalParameter.toTypedArray())
                ).statement()
            }
        }
    }

    private fun WhenOption.emitHeaderParameter(header: TransformableParameter): VariableName {
        // produces
        //
        // response.stringHeaders.get[First)("<headerName>")
        val methodName = when (header.content.typeUsage.type) {
            is CollectionTypeDefinition -> "get".rawMethodName()
            else -> "getFirst".rawMethodName()
        }
        val headerValueExpression = "response".variableName()
            .property("stringHeaders".rawVariableName())
            .invoke(methodName, header.name.literal())

        // produces
        //
        // Maybe.Success(<context>, <headerValueExpression>)
        val context = "response.${header.kind.value}.${header.name}".literal()
        val statement = invoke(Library.MaybeSuccessClass.constructorName, context, headerValueExpression).wrap()

        // produces
        //
        // val <parameterName>Maybe = <statement>
        //     .<deserializationStatement>
        return emitterContext.runEmitter(
            DeserializationStatementEmitter(header.content.typeUsage, statement, header.content.mappedContentType, true)
        ).resultStatement.declaration(header.parameterVariableName.extend(postfix = "maybe"))
    }

    // generates
    // else -> Maybe.Success("response.body", <ResponseObject>("unknown status code ${statusCode.name}", response))
    private fun WhenOptionAware.generateFallbackResponseOption(responseClass: ClassName) {
        optionBlock("else".variableName()) {
            // produces
            // <ResponseObject>("unknown status code ${statusCode.name}", response)
            val newInstance = invoke(
                responseClass.rawNested("ResponseError").constructorName,
                "unknown status code \${statusCode}".literal(),
                "response".rawVariableName()
            )
            // produces
            // Maybe.Success("response.body", <newInstance>)
            invoke(Library.MaybeSuccessClass.constructorName, "response.body".literal(), newInstance).statement()
        }
    }

    private fun defaultParameterExpression(typeUsage: TypeUsage): KotlinExpression? {
        val declaredDefaultValue = when (val safeType = typeUsage.type) {
            is PrimitiveTypeDefinition -> safeType.defaultExpression()
            is EnumTypeDefinition -> safeType.defaultExpression()
            is CollectionTypeDefinition,
            is ObjectTypeDefinition,
            is OneOfTypeDefinition -> null
        }

        // if there is a default expression defined, use it. Otherwise, use the null expression, if null is allowed
        return declaredDefaultValue ?: if (typeUsage.isNullable()) nullLiteral() else null
    }

}