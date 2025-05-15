package com.ancientlightstudios.quarkus.kotlin.openapi.handler.form

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.variableNameOf
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class FormUpstreamHandler : ServerRestControllerHandler, ServerRequestContainerHandler, ClientDelegateHandler,
    ClientRestControllerRequestHandler, TestClientRequestBuilderHandler, TestClientRestControllerHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun ServerRestControllerHandlerContext.emitParameter(parameter: RequestParameter):
            HandlerResult<InstantiationParameter> = parameter.content.matches(ContentType.ApplicationFormUrlencoded) {
        SpecIssue("Form encoding not supported for request parameter")
    }

    override fun ServerRestControllerHandlerContext.emitBody(body: RequestBody): HandlerResult<InstantiationParameter> =
        body.content.matches(ContentType.ApplicationFormUrlencoded) {

            val instance = body.content.model.instance
            if (instance is ObjectModelInstance) {
                // explode the form into several parts, which can require different content types
                val objectParts = instance.ref.properties.map { property ->
                    val nestedBody = body.forProperty(property)
                    registry.getHandler<ServerRestControllerHandler, InstantiationParameter> {
                        NestedServerHandlerContext(property.sourceName, this@emitBody).emitBody(nestedBody)
                    }
                }

                val maybe = allToObject(body.context.literal(), instance.ref.name.asTypeName(), objectParts)
                    .declaration("${body.name}Maybe")
                MaybeParameter(maybe)
            } else {
                // it's just a single item
                registry.getHandler<ServerRestControllerHandler, InstantiationParameter> {
                    NestedServerHandlerContext(body.sourceName, this@emitBody).emitBody(body)
                }
            }
        }

    // all properties need the FormParam annotation, so we create a new context to intercept the parameter
    private class NestedServerHandlerContext(
        private val sourceName: String, private val base: ServerRestControllerHandlerContext
    ) : ServerRestControllerHandlerContext {

        override fun addParameter(parameter: KotlinParameter) {
            parameter.addAnnotation(KotlinAnnotation(Jakarta.FormParamAnnotation, null to sourceName.literal()))
            base.addParameter(parameter)
        }

        override fun addStatement(statement: KotlinStatement) = base.addStatement(statement)

    }

    override fun ServerRequestContainerHandlerContext.emitParameter(parameter: RequestParameter): HandlerResult<Unit> =
        parameter.content.matches(ContentType.ApplicationFormUrlencoded) {
            SpecIssue("Form encoding not supported for request parameter")
        }

    override fun ServerRequestContainerHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.ApplicationFormUrlencoded) {
            emitProperty(body.name, body.content.model.asTypeReference())
        }

    override fun ClientDelegateHandlerContext.emitParameter(parameter: RequestParameter): HandlerResult<Unit> =
        parameter.content.matches(ContentType.ApplicationFormUrlencoded) {
            SpecIssue("Form encoding not supported for request parameter")
        }

    override fun ClientDelegateHandlerContext.emitBody(body: RequestBody): HandlerResult<Unit> =
        body.content.matches(ContentType.ApplicationFormUrlencoded) {
            val instance = body.content.model.instance
            if (instance is ObjectModelInstance) {
                // explode the form into several parts, which can require different content types
                instance.ref.properties.map { property ->
                    val nestedBody = body.forProperty(property)
                    registry.getHandler<ClientDelegateHandler, Unit> {
                        NestedClientHandlerContext(property.sourceName, this@emitBody).emitBody(nestedBody)
                    }
                }
            } else {
                // it's just a single item
                registry.getHandler<ClientDelegateHandler, Unit> {
                    NestedClientHandlerContext(body.sourceName, this@emitBody).emitBody(body)
                }
            }
        }

    // all properties need the FormParam annotation, so we create a new context to intercept the parameter
    private class NestedClientHandlerContext(
        private val sourceName: String, private val base: ClientDelegateHandlerContext
    ) : ClientDelegateHandlerContext {

        override fun addParameter(parameter: KotlinParameter) {
            parameter.addAnnotation(KotlinAnnotation(Jakarta.FormParamAnnotation, null to sourceName.literal()))
            base.addParameter(parameter)
        }

    }

    override fun ClientRestControllerRequestHandlerContext.emitParameter(parameter: RequestParameter):
            HandlerResult<List<KotlinExpression>> =
        parameter.content.matches(ContentType.ApplicationFormUrlencoded) {
            SpecIssue("Form encoding not supported for request parameter")
        }

    override fun ClientRestControllerRequestHandlerContext.emitBody(body: RequestBody):
            HandlerResult<List<KotlinExpression>> = body.content.matches(ContentType.ApplicationFormUrlencoded) {
        val model = body.content.model
        val typeInfo = body.content.forUserProvidedValue()
        emitProperty(body.name, typeInfo.type, typeInfo.defaultValue)

        val baseStatement = when (model.isNullable()) {
            true -> body.name.identifier().nullCheck()
            false -> body.name.identifier()
        }

        val instance = body.content.model.instance
        if (instance is ObjectModelInstance) {
            // explode the form into several parts, which can require different content types
            instance.ref.properties.map { property ->
                val nestedBody = body.forProperty(property)
                registry.getHandler<SerializationHandler, KotlinExpression> {
                    serializationExpression(
                        baseStatement.property(property.name),
                        nestedBody.content.model,
                        nestedBody.content.contentType
                    )
                }.declaration(variableNameOf(nestedBody.name, "Payload"))
            }
        } else {
            // it's just a single item
            listOf(registry.getHandler<SerializationHandler, KotlinExpression> {
                serializationExpression(
                    baseStatement.property(body.name),
                    body.content.model,
                    body.content.contentType
                )
            }.declaration(variableNameOf(body.name, "Payload")))
        }.map { it.identifier() }
    }

    override fun TestClientRequestBuilderHandlerContext.emitParameter(parameter: RequestParameter): HandlerResult<Unit> =
        parameter.content.matches(ContentType.ApplicationFormUrlencoded) {
            SpecIssue("Form encoding not supported for request parameter")
        }

    override fun TestClientRequestBuilderHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.ApplicationFormUrlencoded) {
            val model = body.content.model

            emitCustom(body.name, model.asTypeReference().acceptNull()) {
                val instance = body.content.model.instance
                if (instance is ObjectModelInstance) {
                    // explode the form into several parts, which can require different content types
                    instance.ref.properties.forEach { property ->
                        val nestedBody = body.forProperty(property)

                        val serialization = registry.getHandler<SerializationHandler, KotlinExpression> {
                            serializationExpression(
                                "value".identifier().property(property.name),
                                nestedBody.content.model,
                                nestedBody.content.contentType
                            )
                        }

                        // produces
                        // requestSpecification = requestSpecification.formParam("<paramName>", <serialization>)
                        "requestSpecification".identifier().invoke("formParam", property.name.literal(), serialization)
                            .assignment("requestSpecification")
                    }
                } else {
                    // it's just a single item
                    val serialization = registry.getHandler<SerializationHandler, KotlinExpression> {
                        serializationExpression(
                            "value".identifier(),
                            body.content.model,
                            body.content.contentType
                        )
                    }

                    // produces
                    // requestSpecification = requestSpecification.formParam("body", <serialization>)
                    "requestSpecification".identifier().invoke("formParam", body.name.literal(), serialization)
                        .assignment("requestSpecification")
                }
            }
        }

    override fun TestClientRestControllerHandlerContext.parameterType(parameter: RequestParameter):
            HandlerResult<TypeInfo> = parameter.content.matches(ContentType.ApplicationFormUrlencoded) {
        SpecIssue("Form encoding not supported for request parameter")
    }

    override fun TestClientRestControllerHandlerContext.bodyType(body: RequestBody) =
        body.content.matches(ContentType.ApplicationFormUrlencoded) { body.content.forUserProvidedValue() }

    override fun TestClientRestControllerHandlerContext.pathParameterSerialization(parameter: RequestParameter):
            HandlerResult<KotlinExpression> = parameter.content.matches(ContentType.ApplicationFormUrlencoded) {
        SpecIssue("Form encoding not supported for request parameter")
    }

    private fun RequestBody.forProperty(property: ObjectModelProperties): RequestBody {
        val contentType = property.model.defaultContentType()
        return RequestBody(
            variableNameOf(property.name, name),
            // TODO: check what would be the best solution for an optional form with optional or required fields. are they
            // just nullable, or is there more to do (e.g. property.model.acceptNull())
            ContentInfo(property.model, contentType, contentType.value),
            source,
            "$context.${property.name}"
        )
    }

    // don't adjust the model to the default value, to keep nullability even if there is a default value available
    private fun ContentInfo.forUserProvidedValue() = TypeInfo(model.asTypeReference(), model.getDefinedDefaultValue())

}