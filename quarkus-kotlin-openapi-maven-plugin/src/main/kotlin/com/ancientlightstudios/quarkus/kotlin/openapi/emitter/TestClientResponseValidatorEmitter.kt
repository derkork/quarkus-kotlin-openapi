package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientHttpResponseClassNameHint.clientHttpResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerClassNameHint.responseContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseValidatorClassNameHint.responseValidatorClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.AssignableExpression.Companion.assignable
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TryCatchExpression.Companion.tryExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.WhenExpression.Companion.whenExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest

class TestClientResponseValidatorEmitter : CodeEmitter {

    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this
        spec.inspect {
            bundles {
                requests {
                    emitResponseValidatorFile()
                        .writeFile()
                }
            }
        }
    }

    private fun RequestInspection.emitResponseValidatorFile() = kotlinFile(request.responseValidatorClassName) {
        kotlinClass(fileName) {
            kotlinMember(
                "response".variableName(),
                type = request.responseContainerClassName.typeName(),
            )
            kotlinMember(
                "requestResponseLog".variableName(),
                type = Kotlin.ByteArrayOutputStreamClass.typeName(),
            )

            responses {
                val reason = when (val responseCode = response.responseCode) {
                    is ResponseCode.Default -> "default"
                    is ResponseCode.HttpStatusCode -> responseCode.statusCodeReason()
                }

                emitResponseValidationMethod(request, reason)
            }
        }
    }

    private fun KotlinClass.emitResponseValidationMethod(request: TransformableRequest, reason: String) {
        val responseClass = request.clientHttpResponseClassName.nested(reason)

        kotlinMethod(reason.methodName(prefix = "is", postfix = "Response")) {
            kotlinParameter(
                "block".variableName(),
                TypeName.DelegateTypeName(responseClass.typeName(), emptyList(), Kotlin.UnitType)
            )

            tryExpression {
                whenExpression("response".variableName()) {
                    optionBlock(AssignableExpression.assignable(responseClass)) {
                        "response".variableName().invoke("block".methodName()).statement()
                    }
                    optionBlock("else".variableName()) {
                        InvocationExpression.invoke(
                            Misc.AssertionFailedErrorClass.constructorName,
                            "Assertion failed.".literal(),
                             responseClass.javaClass().property("name".variableName()),
                            "response".variableName().property("javaClass".variableName()).property("name".variableName())
                        ).throwStatement()
                    }
                }.statement()
                catchBlock(Kotlin.ThrowableClass) {
                    InvocationExpression.invoke("println".methodName(), "requestResponseLog".variableName()).statement()
                    "e".variableName().throwStatement()
                }
            }.statement()
        }
    }

}