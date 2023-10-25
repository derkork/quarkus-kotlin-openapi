package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ArrayExpression.Companion.arrayExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.toKebabCase

class ClientDelegateEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        kotlinFile(clientPackage(), suite.name.extend(postfix = "Delegate")) {
            registerImport("com.ancientlightstudios.quarkus.kotlin.openapi.*")
            registerImport("org.eclipse.microprofile.rest.client.inject.RegisterRestClient")
            registerImport(modelPackage(), wildcardImport = true)
            registerImport("jakarta.ws.rs", wildcardImport = true)
            registerImport("org.jboss.resteasy.reactive.RestResponse")

            kotlinInterface(fileName) {
                kotlinAnnotation(
                    "RegisterRestClient".rawClassName(),
                    "configKey".variableName() to suite.name.extend(postfix = "Client").render().toKebabCase().stringExpression()
                )
                suite.requests.forEach { request ->
                    kotlinMethod(request.name, true, "RestResponse".rawTypeName().of("String".rawTypeName(true))) {
                        kotlinAnnotation(request.method.name.uppercase().rawClassName())
                        addPathAnnotation(request.path)
                        if (request.responses.any { it.second !=null }) {
                            kotlinAnnotation("Produces".rawClassName(), "value".variableName() to "application/json".stringExpression().arrayExpression())
                        }

                        request.parameters.forEach { parameter ->
                            kotlinParameter(parameter.name, parameter.type.safeType) {
                                kotlinAnnotation(
                                    parameter.source.value.rawClassName(),
                                    "value".variableName() to parameter.name.render().stringExpression()
                                )
                            }
                        }

                        request.body?.let {
                            kotlinParameter("body".variableName(), it.safeType)
                            kotlinAnnotation("Consumes".rawClassName(), "value".variableName() to "application/json".stringExpression().arrayExpression())
                        }
                    }
                }
            }
        }.also { generateFile(it) }
    }
}
