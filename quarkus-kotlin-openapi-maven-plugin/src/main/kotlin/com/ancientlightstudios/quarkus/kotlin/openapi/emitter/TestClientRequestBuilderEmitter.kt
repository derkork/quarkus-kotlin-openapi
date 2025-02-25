package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IfElseExpression.Companion.ifElseExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.RequestBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.RequestParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.TestClientRequestBuilder
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class TestClientRequestBuilderEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<TestClientRequestBuilder>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(builder: TestClientRequestBuilder) {
        kotlinFile(builder.name.asTypeName()) {

            kotlinClass(name) {
                registerImports(Library.All)
                registerImports(config.additionalImports())

                kotlinMember(
                    "requestSpecification",
                    type = RestAssured.RequestSpecification.asTypeReference(),
                    mutable = true,
                    accessModifier = null
                )
                kotlinMember("dependencyVogel", builder.dependencyVogel.name.asTypeReference())

                val context = object : TestClientRequestBuilderHandlerContext {
                    override fun addMethod(method: KotlinMethod) = this@kotlinClass.addMethod(method)
                }

                builder.parameters.filterNot { it.kind == ParameterKind.Path }.forEach { parameter ->
                    getHandler<TestClientRequestBuilderHandler, Unit> {
                        context.emitParameter(parameter)
                    }
                }

                builder.body?.let { body ->
                    getHandler<TestClientRequestBuilderHandler, Unit> {
                        context.emitBody(body)
                    }
                }
            }
        }
    }

}

interface TestClientRequestBuilderHandlerContext : MethodAware {

    /**
     * generates a default method for the parameter. if the type is nullable, a null check will be added, so the given
     * serialization should ignore null values
     */
    fun emitDefaultParameter(parameter: RequestParameter, type: KotlinTypeReference, serialization: KotlinExpression) {
        emitCustom(parameter.name, type) {
            val methodName = when (parameter.kind) {
                ParameterKind.Query -> "queryParams"
                ParameterKind.Header -> "headers"
                ParameterKind.Cookie -> "cookies"
                ParameterKind.Path -> ProbableBug("path params are not supported by the test client builder")
            }

            // produces
            // requestSpecification.<methodName>(mapOf(Pair(<parameter.sourceName>, <serialization>)))
            val pair = invoke(Kotlin.Pair.identifier(), parameter.sourceName.literal(), serialization)
            "requestSpecification".identifier().invoke(methodName, invoke("mapOf", pair))
                .assignment("requestSpecification")
        }
    }

    /**
     * generates a default method for the body. if the type is nullable, a null check will be added, so the given
     * serialization should ignore null values
     */
    fun emitDefaultBody(body: RequestBody, type: KotlinTypeReference, serialization: KotlinExpression) {
        emitCustom(body.name, type) {
            // produces
            // requestSpecification = requestSpecification.body(<serialization>)
            "requestSpecification".identifier().invoke("body", serialization)
                .assignment("requestSpecification")
        }
    }

    fun emitCustom(methodName: String, type: KotlinTypeReference, block: StatementAware.() -> Unit) {
        kotlinMethod(methodName) {
            kotlinParameter("value", type)

            // produces
            // if (value != null) {
            //     <statements>
            // }
            // or just
            // <statements>
            // if the type is not nullable
            if (type.nullable) {
                ifElseExpression("value".identifier().compareWith(nullLiteral(), "!=")) {
                    block()
                }.statement()
            } else {
                block()
            }
        }
    }

}

interface TestClientRequestBuilderHandler : Handler {

    fun TestClientRequestBuilderHandlerContext.emitParameter(parameter: RequestParameter): HandlerResult<Unit>

    fun TestClientRequestBuilderHandlerContext.emitBody(body: RequestBody): HandlerResult<Unit>

}