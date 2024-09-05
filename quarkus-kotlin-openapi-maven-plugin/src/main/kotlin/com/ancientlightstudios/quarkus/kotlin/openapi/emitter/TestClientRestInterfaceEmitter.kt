package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.CombineIntoObjectStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.DeserializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.SerializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestBundleInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientErrorResponseClassNameHint.clientErrorResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientHttpResponseClassNameHint.clientHttpResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientRestInterfaceClassNameHint.clientRestInterfaceClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestBuilderClassNameHint.requestBuilderClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestMethodNameHint.requestMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerClassNameHint.responseContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseValidatorClassNameHint.responseValidatorClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ConstantName.Companion.rawConstantName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.rawVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.CollectionTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.PrimitiveTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition

class TestClientRestInterfaceEmitter(private val pathPrefix: String) : CodeEmitter {

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
            kotlinMember("objectMapper".variableName(), Misc.ObjectMapperClass.typeName())
            kotlinMember(
                "specBuilder".variableName(),
                TypeName.DelegateTypeName(returnType = RestAssured.RequestSpecificationClass.typeName())
            )

            requests {
                emitSafeMethod(this@kotlinClass)
                emitUnsafeMethod(this@kotlinClass)
                emitRawMethod(this@kotlinClass)
            }
        }
    }

    private fun RequestInspection.emitSafeMethod(file: KotlinClass) {
        file.kotlinMethod(request.requestMethodName.extend(postfix = "Safe"), bodyAsAssignment = true) {
            val statements = mutableListOf<KotlinExpression>()
            val pathParams = mutableListOf<KotlinExpression>()
            parameters {
                kotlinParameter(
                    parameter.parameterVariableName,
                    parameter.content.typeUsage.buildValidType(),
                    parameter.content.typeUsage.type.defaultExpression()
                )

                if (parameter.kind == ParameterKind.Path) {
                    pathParams.add(parameter.parameterVariableName)
                } else {
                    statements.add(invoke(parameter.name.methodName(), parameter.parameterVariableName))
                }
            }

            body {
                kotlinParameter(
                    body.parameterVariableName,
                    body.content.typeUsage.buildValidType(),
                    body.content.typeUsage.type.defaultExpression()
                )
                statements.add(invoke("body".methodName(), body.parameterVariableName))
            }

            invoke(request.requestMethodName.extend(postfix = "Unsafe"), *pathParams.toTypedArray()) {
                statements.forEach {
                    it.statement()
                }
            }.statement()
        }
    }

    private fun RequestInspection.emitUnsafeMethod(file: KotlinClass) {
        file.kotlinMethod(request.requestMethodName.extend(postfix = "Unsafe"), bodyAsAssignment = true) {
            val pathParams = mutableListOf<KotlinExpression>()
            parameters {
                if (parameter.kind == ParameterKind.Path) {
                    kotlinParameter(
                        parameter.parameterVariableName,
                        parameter.content.typeUsage.buildValidType(),
                        parameter.content.typeUsage.type.defaultExpression()
                    )

                    val parameterStatement = emitterContext.runEmitter(
                        SerializationStatementEmitter(
                            parameter.content.typeUsage,
                            parameter.parameterVariableName,
                            parameter.content.mappedContentType
                        )
                    ).resultStatement

                    pathParams.add(parameterStatement)
                }
            }

            kotlinParameter(
                "block".variableName(),
                TypeName.DelegateTypeName(request.requestBuilderClassName.typeName(), emptyList(), Kotlin.UnitType),
                expression = emptyLambda()
            )

            invoke(request.requestMethodName.extend(postfix = "Raw"), *pathParams.toTypedArray()) {
                invoke(
                    request.requestBuilderClassName.constructorName,
                    "this".variableName(),
                    "objectMapper".variableName()
                )
                    .invoke("apply".methodName(), "block".variableName())
                    .property("requestSpecification".variableName())
                    .statement()
            }.statement()
        }
    }

    private fun RequestInspection.emitRawMethod(file: KotlinClass) {
        file.kotlinMethod(
            request.requestMethodName.extend(postfix = "Raw"),
            returnType = request.responseValidatorClassName.typeName()
        ) {
            val pathParams = mutableListOf<KotlinExpression>()
            parameters {
                if (parameter.kind == ParameterKind.Path) {
                    kotlinParameter(
                        parameter.parameterVariableName,
                        Kotlin.AnyClass.typeName()
                    )

                    pathParams.add(
                        invoke(
                            Kotlin.PairClass.constructorName, parameter.name.literal(), parameter.parameterVariableName
                        )
                    )
                }
            }
            kotlinParameter(
                "block".variableName(), TypeName.DelegateTypeName(
                    RestAssured.RequestSpecificationClass.typeName(),
                    emptyList(),
                    RestAssured.RequestSpecificationClass.typeName()
                )
            )

            val outputStream = invoke(Kotlin.ByteArrayOutputStreamClass.constructorName)
                .declaration("outputStream".variableName())

            val printStream = invoke(Kotlin.PrintStreamClass.constructorName, outputStream)
                .declaration("printStream".variableName())

            val result = TryCatchExpression.tryExpression {
                val pathParamMap = InvocationExpression.invoke(
                    "mapOf".methodName(), pathParams,
                    listOf(Kotlin.StringClass.typeName(), Kotlin.AnyClass.typeName())
                )

                val validatableResponse = InvocationExpression.invoke("specBuilder".methodName()).wrap()
                    .invoke(
                        "filter".methodName(), InvocationExpression.invoke(
                            Library.RequestLoggingFilterClass.constructorName,
                            printStream
                        )
                    ).wrap()
                    .invoke(
                        "filter".methodName(), InvocationExpression.invoke(
                            Library.ResponseLoggingFilterClass.constructorName,
                            printStream
                        )
                    ).wrap()
                    .invoke("run".methodName(), "block".variableName()).wrap()
                    .invoke(
                        request.method.value.methodName(),
                        request.path.literal(),
                        pathParamMap
                    )  // TODO: prefix
                    .wrap()
                    .invoke("then".methodName()).wrap()
                    .invoke("extract".methodName())
                    .declaration("validatableResponse".variableName())

                emitResponseConversion(request, validatableResponse)

                val errorClass = request.clientErrorResponseClassName

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
            }.declaration("result")
            invoke(request.responseValidatorClassName.constructorName, result, outputStream).returnStatement()
        }
    }

    private fun StatementAware.emitResponseConversion(
        request: TransformableRequest,
        validatableResponse: VariableName
    ) {
        val successClass = request.clientHttpResponseClassName
        val errorClass = request.clientErrorResponseClassName

        // produces
        // val statusCode = <validatableResponse>.statusCode()
        val statusCode = validatableResponse.invoke("statusCode".methodName()).declaration("statusCode")

        // produces
        // val responseMaybe: Maybe<[ResponseContainerClass]> = when (statusCode) {
        //     ...
        // }
        val responseMaybe = "responseMaybe".rawVariableName()
        WhenExpression.whenExpression(statusCode) {
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
        WhenExpression.whenExpression(responseMaybe) {
            // produces
            // is Maybe.Success -> responseMaybe.value
            optionBlock(AssignableExpression.assignable(Library.MaybeSuccessClass)) {
                responseMaybe.property("value".rawVariableName()).statement()
            }

            // produces
            // is Maybe.Failure -> {
            //     val errors = responseMaybe.errors.joinToString { "${it.path}: ${it.message}" }
            //     <ResponseObject>(errors, validatableResponse.response())
            // }
            optionBlock(AssignableExpression.assignable(Library.MaybeFailureClass)) {
                responseMaybe.property("errors".variableName())
                    .invoke("joinToString".rawMethodName()) {
                        "\${it.path}: \${it.message}".literal().statement()
                    }.declaration("errors".variableName())

                InvocationExpression.invoke(
                    errorClass.rawNested("ResponseError").constructorName,
                    "errors".variableName(),
                    "validatableResponse".variableName().invoke("response".methodName())
                ).statement()
            }
        }.statement()

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
                // TODO: we probably need different target types here (e.g. for binary)
                val deserializationMethod = when (body.content.mappedContentType) {
                    ContentType.ApplicationOctetStream -> "asByteArray"
                    else -> "asString"
                }

                // produces
                // validatableResponse.body().<deserializationMethod>()
                val entity = "validatableResponse".variableName().invoke("body".methodName())
                    .invoke(deserializationMethod.methodName()).declaration("entity".variableName())

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
        // validatableResponse.headers().getValues("<headerName>"[firstOrNull()])
        var headerValueExpression: KotlinExpression =
            "validatableResponse".variableName().invoke("headers".methodName())
                .invoke("getValues".methodName(), header.name.literal())

        headerValueExpression = when (header.content.typeUsage.type) {
            is CollectionTypeDefinition -> headerValueExpression
            else -> headerValueExpression.invoke("firstOrNull".methodName())
        }

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
            // <ResponseObject>("unknown status code ${statusCode.name}", validatableResponse.response())
            val newInstance = invoke(
                responseClass.rawNested("ResponseError").constructorName,
                "unknown status code \${statusCode}".literal(),
                "validatableResponse".variableName().invoke("response".methodName())
            )
            // produces
            // Maybe.Success("response.body", <newInstance>)
            invoke(Library.MaybeSuccessClass.constructorName, "response.body".literal(), newInstance).statement()
        }
    }

    private fun TypeDefinition.defaultExpression() = when (this) {
        is PrimitiveTypeDefinition -> this.defaultExpression()
        is EnumTypeDefinition -> this.defaultExpression()
        else -> null
    }

}

