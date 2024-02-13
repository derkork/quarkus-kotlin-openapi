package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

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
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ConstantName.Companion.rawConstantName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TryCatchExpression.Companion.tryExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.rawVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.WhenExpression.Companion.whenExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition

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
            parameters { requestContainerParts.add(emitParameter(parameter)) }
            body { requestContainerParts.addAll(emitBody(body)) }

            tryExpression {
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
                }.assignment("response".rawVariableName())

                // produces
                // val statusCode = RestResponse.Status.fromStatusCode(response.status)
                val statusCode = "statusCode".rawVariableName()
                Misc.RestResponseStatusClass.companionObject()
                    .invoke(
                        "fromStatusCode".rawMethodName(),
                        "response".variableName().property("status".variableName())
                    ).assignment(statusCode)

                // produces
                // val responseMaybe = when (statusCode) {
                //     ...
                // }
                val responseMaybe = "responseMaybe".rawVariableName()
                whenExpression(statusCode) {
                    // generate options for all known status codes
                    request.responses.filter { it.responseCode is ResponseCode.HttpStatusCode }.forEach {
                        generateKnownResponseOption(
                            successClass,
                            it.responseCode as ResponseCode.HttpStatusCode,
                            it.body
                        )
                    }

                    // generate option for the default status or fallback otherwise
                    val defaultResponse = request.responses.firstOrNull { it.responseCode == ResponseCode.Default }
                    when (defaultResponse) {
                        null -> generateFallbackResponseOption(errorClass)
                        else -> generateDefaultResponseOption(successClass, defaultResponse.body)
                    }
                }.assignment(responseMaybe)


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
                            }.assignment("errors".variableName())

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

    private fun KotlinMethod.emitParameter(parameter: TransformableParameter): VariableName {
        val parameterName = parameter.parameterVariableName
        val typeDefinition = parameter.typeDefinition
        val default = defaultParameterExpression(!parameter.required, typeDefinition)
        kotlinParameter(parameterName, typeDefinition.buildValidType(!parameter.required), default)

        return emitterContext.runEmitter(
            SerializationStatementEmitter(
                typeDefinition, !parameter.required, parameterName, ContentType.TextPlain
            )
        ).resultStatement.assignment(parameterName.extend(postfix = "Payload"))
    }

    // generates parameters and conversion for the request body depending on the media type
    private fun KotlinMethod.emitBody(body: TransformableBody): List<VariableName> {
        return when (body.content.mappedContentType) {
            ContentType.ApplicationJson -> listOf(emitJsonBody(body))
            ContentType.TextPlain -> listOf(emitPlainBody(body))
            ContentType.MultipartFormData -> emitMultipartBody(body)
            ContentType.ApplicationFormUrlencoded -> emitFormBody(body)
            ContentType.ApplicationOctetStream -> listOf(emitOctetBody(body))
        }
    }

    private fun KotlinMethod.emitJsonBody(body: TransformableBody): VariableName {
        val parameterName = "body".variableName()
        val typeDefinition = body.content.typeDefinition
        val default = defaultParameterExpression(!body.required, typeDefinition)

        kotlinParameter(parameterName, typeDefinition.buildValidType(!body.required), default)

        val jsonNode = emitterContext.runEmitter(
            SerializationStatementEmitter(
                typeDefinition, !body.required, parameterName, body.content.mappedContentType
            )
        ).resultStatement

        return "objectMapper".variableName()
            .invoke("writeValueAsString".rawMethodName(), jsonNode)
            .assignment("bodyPayload".variableName())
    }

    private fun KotlinMethod.emitPlainBody(body: TransformableBody): VariableName {
        val parameterName = "body".variableName()
        val typeDefinition = body.content.typeDefinition
        val default = defaultParameterExpression(!body.required, typeDefinition)

        kotlinParameter(parameterName, typeDefinition.buildValidType(!body.required), default)
        return emitterContext.runEmitter(
            SerializationStatementEmitter(
                typeDefinition, !body.required, parameterName, body.content.mappedContentType
            )
        ).resultStatement.assignment("bodyPayload".variableName())
    }

    private fun KotlinMethod.emitMultipartBody(body: TransformableBody): List<VariableName> {
        return listOf("multi".variableName())
        val typeDefinition = body.content.typeDefinition
        val default = defaultParameterExpression(!body.required, typeDefinition)

    }

    private fun KotlinMethod.emitFormBody(body: TransformableBody): List<VariableName> {
        return listOf("form".variableName())
        val typeDefinition = body.content.typeDefinition
        val default = defaultParameterExpression(!body.required, typeDefinition)
    }

    private fun KotlinMethod.emitOctetBody(body: TransformableBody): VariableName {
        return "octed".variableName()
        val typeDefinition = body.content.typeDefinition
        val default = defaultParameterExpression(!body.required, typeDefinition)

    }

    private fun WhenOptionAware.generateKnownResponseOption(
        responseClass: ClassName, statusCode: ResponseCode.HttpStatusCode, body: TransformableBody?
    ) {
        val optionValue = Misc.RestResponseStatusClass.companionObject()
            .property(statusCode.statusCodeName().rawConstantName())
        generateResponseOption(responseClass.nested(statusCode.statusCodeReason()), optionValue, false, body)
    }

    private fun WhenOptionAware.generateDefaultResponseOption(
        responseClass: ClassName, body: TransformableBody?
    ) {
        generateResponseOption(responseClass.rawNested("Default"), "else".variableName(), true, body)
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
        responseClass: ClassName, optionValue: KotlinExpression, withStatusCode: Boolean, body: TransformableBody?
    ) {
        val additionalParameter = when (withStatusCode) {
            true -> listOf("statusCode".variableName())
            else -> listOf()
        }

        optionBlock(optionValue) {
            if (body == null) {
                invoke(
                    Library.MaybeSuccessClass.constructorName,
                    "response.body".literal(),
                    invoke(responseClass.constructorName, *additionalParameter.toTypedArray())
                ).statement()
            } else {
                // TODO: we probably need different target types here (e.g. for binary)
                // produces
                // response.readEntity(String::class.java)
                val entity = "response".variableName()
                    .invoke("readEntity".rawMethodName(), Kotlin.StringClass.javaClass())
                    .assignment("entity".variableName())

                var statement = invoke(Library.MaybeSuccessClass.constructorName, "response.body".literal(), entity)

                // adds content-type specific deserialization steps to the statement
                statement = emitterContext.runEmitter(
                    DeserializationStatementEmitter(
                        body.content.typeDefinition, !body.required, statement, body.content.mappedContentType, true
                    )
                ).resultStatement

                // produces
                // <statement>
                //     .onSuccess { success(<ResponseObject>(value)) }
                statement.wrap().invoke("onSuccess".rawMethodName()) {
                    val newInstance = invoke(
                        responseClass.constructorName,
                        "value".rawVariableName(),
                        *additionalParameter.toTypedArray()
                    )
                    invoke("success".rawMethodName(), newInstance).statement()
                }.statement()
            }
        }
    }


    // generates
    // else -> Maybe.Success("response.body", <ResponseObject>("unknown status code ${statusCode.name}", response))
    private fun WhenOptionAware.generateFallbackResponseOption(responseClass: ClassName) {
        optionBlock("else".variableName()) {
            // produces
            // <ResponseObject>("unknown status code ${statusCode.name}", response)
            val newInstance = invoke(
                responseClass.rawNested("ResponseError").constructorName,
                "unknown status code \${statusCode.name}".literal(),
                "response".rawVariableName()
            )
            // produces
            // Maybe.Success("response.body", <newInstance>)
            invoke(Library.MaybeSuccessClass.constructorName, "response.body".literal(), newInstance).statement()
        }
    }

    private fun defaultParameterExpression(forceNullable: Boolean, typeDefinition: TypeDefinition): KotlinExpression? {
        val nullable = forceNullable || typeDefinition.nullable
        return when (nullable) {
            true -> nullLiteral()
            else -> null
        }
    }

}