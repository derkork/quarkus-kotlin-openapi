package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.nestedTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TryCatchExpression.Companion.tryExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.WhenExpression.Companion.whenExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ClientResponse
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ClientResponseImplementation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.TestClientResponseValidator
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.methodNameOf

class TestClientResponseValidatorEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<TestClientResponseValidator>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(validator: TestClientResponseValidator) {
        kotlinFile(validator.name.asTypeName()) {
            kotlinClass(name) {
                kotlinMember("response", validator.response.name.asTypeReference())
                kotlinMember("requestResponseLog", Kotlin.ByteArrayOutputStream.asTypeReference())

                val response = validator.response
                emitVerifyResponseMethod(response)
                emitGenericValidationMethod(response)

                validator.response.httpResponse.implementations.forEach { implementation ->
                    emitResponseValidationMethod(response, implementation)
                }
            }
        }
    }

    private fun KotlinClass.emitVerifyResponseMethod(response: ClientResponse) {
        val returnType = KotlinSimpleTypeReference("T", "")
        kotlinMethod(
            "verifyResponse",
            accessModifier = KotlinAccessModifier.Private,
            bodyAsAssignment = true,
            returnType = returnType,
            genericParameter = listOf(returnType)
        ) {
            kotlinParameter("block", KotlinDelegateTypeReference(response.name.asTypeReference(), returnType))

            // produces
            // try {
            //     response.block()
            // } catch(e: Throwable) {
            //     println(requestResponseLog)
            //     throw e
            // }
            tryExpression {
                "response".identifier().invoke("block").statement()
                catchBlock(Kotlin.Throwable) {
                    InvocationExpression.invoke("println", "requestResponseLog".identifier()).statement()
                    "e".identifier().throwStatement()
                }
            }.statement()
        }
    }

    private fun KotlinClass.emitGenericValidationMethod(response: ClientResponse) {
        kotlinMethod("responseSatisfies", bodyAsAssignment = true) {
            kotlinParameter(
                "block",
                KotlinDelegateTypeReference(response.name.asTypeReference(), Kotlin.Unit.asTypeReference())
            )

            // produces
            // verifyResponse(block)
            invoke("verifyResponse", "block".identifier()).statement()
        }
    }

    private fun KotlinClass.emitResponseValidationMethod(
        response: ClientResponse,
        implementation: ClientResponseImplementation
    ) {
        val methodName = methodNameOf("is", implementation.name, "Response")
        val implementationClass = response.httpResponse.name.asTypeName()
            .nestedTypeName(implementation.name)

        kotlinMethod(methodName, bodyAsAssignment = true) {
            kotlinParameter(
                "block",
                KotlinDelegateTypeReference(implementationClass.asTypeReference(), Kotlin.Unit.asTypeReference())
            )

            // produces
            // verifyResponse {
            //     when(response) {
            //         ...
            //     }
            // }
            invoke("verifyResponse") {
                whenExpression("response".identifier()) {
                    // produces
                    // is <responseClass> -> response.block()
                    optionBlock(AssignableExpression.assignable(implementationClass.asTypeReference())) {
                        "response".identifier().invoke("apply", "block".identifier()).statement()
                    }

                    // produces
                    // else -> throw AssertionFailedError("Assertion failed.", <responseClass>::class.java.name, response.javaClass.name)
                    optionBlock("else".identifier()) {
                        InvocationExpression.invoke(
                            Misc.AssertionFailedError.identifier(),
                            "Assertion failed.".literal(),
                            implementationClass.identifier().functionReference("class.java").property("name"),
                            "response".identifier().property("javaClass").property("name")
                        ).throwStatement()
                    }
                }.statement()
            }.statement()
        }
    }

}