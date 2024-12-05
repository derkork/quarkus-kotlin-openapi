package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientHttpResponseClassNameHint.clientHttpResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerClassNameHint.responseContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseValidatorClassNameHint.responseValidatorClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TryCatchExpression.Companion.tryExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.WhenExpression.Companion.whenExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest

//class TestClientResponseValidatorEmitter : CodeEmitter {
//
//    private lateinit var emitterContext: EmitterContext
//
//    override fun EmitterContext.emit() {
//        emitterContext = this
//        spec.inspect {
//            bundles {
//                requests {
//                    emitResponseValidatorFile()
//                        .writeFile()
//                }
//            }
//        }
//    }
//
//    private fun RequestInspection.emitResponseValidatorFile() = kotlinFile(request.responseValidatorClassName) {
//        kotlinClass(fileName) {
//            kotlinMember(
//                "response".variableName(),
//                type = request.responseContainerClassName.typeName(),
//            )
//            kotlinMember(
//                "requestResponseLog".variableName(),
//                type = Kotlin.ByteArrayOutputStreamClass.typeName(),
//            )
//
//            emitVerifyResponseMethod(request)
//            emitGenericValidationMethod(request)
//
//            responses {
//                val reason = when (val responseCode = response.responseCode) {
//                    is ResponseCode.Default -> "default"
//                    is ResponseCode.HttpStatusCode -> responseCode.statusCodeReason()
//                }
//
//                emitResponseValidationMethod(request, reason)
//            }
//        }
//    }
//
//    private fun KotlinClass.emitVerifyResponseMethod(request: OpenApiRequest) {
//        val tType = "T".rawClassName("", true).typeName()
//        kotlinMethod("verifyResponse".methodName(), accessModifier = KotlinAccessModifier.Private, bodyAsAssignment = true,
//                returnType = tType, genericParameter = listOf(tType)) {
//            kotlinParameter(
//                "block".variableName(),
//                TypeName.DelegateTypeName(request.responseContainerClassName.typeName(), listOf(), tType)
//            )
//
//            // produces
//            // try {
//            //     response.block()
//            // } catch(e: Throwable) {
//            //     println(requestResponseLog)
//            //     throw e
//            // }
//            tryExpression {
//                "response".variableName().invoke("block".methodName()).statement()
//                catchBlock(Kotlin.ThrowableClass) {
//                    InvocationExpression.invoke("println".methodName(), "requestResponseLog".variableName()).statement()
//                    "e".variableName().throwStatement()
//                }
//            }.statement()
//        }
//    }
//
//    private fun KotlinClass.emitGenericValidationMethod(request: OpenApiRequest) {
//        kotlinMethod("responseSatisfies".methodName(), bodyAsAssignment = true) {
//            kotlinParameter(
//                "block".variableName(),
//                TypeName.DelegateTypeName(request.responseContainerClassName.typeName(), listOf(), Kotlin.UnitType)
//            )
//
//            // produces
//            // verifyResponse(block)
//            invoke("verifyResponse".methodName(), "block".variableName())
//                .statement()
//        }
//    }
//
//    private fun KotlinClass.emitResponseValidationMethod(request: OpenApiRequest, reason: String) {
//        val responseClass = request.clientHttpResponseClassName.nested(reason)
//
//        kotlinMethod(reason.methodName(prefix = "is", postfix = "Response"), bodyAsAssignment = true) {
//            kotlinParameter(
//                "block".variableName(),
//                TypeName.DelegateTypeName(responseClass.typeName(), listOf(), Kotlin.UnitType)
//            )
//
//            // produces
//            // verifyResponse {
//            //     when(response) {
//            //         ...
//            //     }
//            // }
//            invoke("verifyResponse".methodName()) {
//                whenExpression("response".variableName()) {
//                    // produces
//                    // is <responseClass> -> response.block()
//                    optionBlock(AssignableExpression.assignable(responseClass)) {
//                        "response".variableName().invoke("apply".methodName(), "block".variableName()).statement()
//                    }
//
//                    // produces
//                    // else -> throw AssertionFailedError("Assertion failed.", <responseClass>::class.java.name, response.javaClass.name)
//                    optionBlock("else".variableName()) {
//                        InvocationExpression.invoke(
//                            Misc.AssertionFailedErrorClass.constructorName,
//                            "Assertion failed.".literal(),
//                            responseClass.javaClass().property("name".variableName()),
//                            "response".variableName().property("javaClass".variableName())
//                                .property("name".variableName())
//                        ).throwStatement()
//                    }
//                }.statement()
//            }.statement()
//        }
//    }
//
//}