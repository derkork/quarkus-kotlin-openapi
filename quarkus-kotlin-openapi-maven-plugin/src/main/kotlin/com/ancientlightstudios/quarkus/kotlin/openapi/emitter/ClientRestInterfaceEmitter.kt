package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.RequestBuilderTransformStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.addTransformStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry

class ClientRestInterfaceEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        kotlinFile(clientPackage(), suite.name.extend(postfix = "Client")) {
            registerImport("jakarta.ws.rs.core", wildcardImport = true)
            registerImport("com.fasterxml.jackson.databind.ObjectMapper")
            registerImport(modelPackage(), wildcardImport = true)
            registerImport(apiPackage(), wildcardImport = true)
            registerImport("org.jboss.resteasy.reactive.RestResponse")
            registerImport("jakarta.enterprise.context.ApplicationScoped")

            kotlinClass(fileName, false) {
                kotlinAnnotation("ApplicationScoped".rawClassName())

                kotlinMember("delegate".variableName(), suite.name.extend(postfix = "Delegate").typeName())
                kotlinMember("objectMapper".variableName(), "ObjectMapper".rawTypeName())

                suite.requests.forEach {
                    generateRequest(it)
                }
            }
        }.also { generateFile(it) }
    }

    private fun KotlinClass.generateRequest(request: Request) {
        val returnType = request.name.className().extend(postfix = "Response")
        kotlinMethod(request.name, true, "RequestResult".rawTypeName().of(returnType)) {
            request.parameters.forEach {
                kotlinParameter(it.name, it.type.safeType) // TODO: default value for nullable properties
            }

            request.body?.let {
                kotlinParameter("body".variableName(), it.safeType) // TODO: default value for nullable properties
            }

            kotlinStatement {

            }
        }
    }
}
