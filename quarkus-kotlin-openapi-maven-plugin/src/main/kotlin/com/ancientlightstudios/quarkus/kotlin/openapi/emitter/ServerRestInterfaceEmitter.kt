package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.RequestBuilderTransformStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry

class ServerRestInterfaceEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        kotlinFile(serverPackage(), suite.name.extend(postfix = "Server")) {
            registerImport("jakarta.ws.rs", wildcardImport = true)
            registerImport("com.fasterxml.jackson.databind.ObjectMapper")
            registerImport(modelPackage(), wildcardImport = true)
            registerImport(apiPackage(), wildcardImport = true)
            registerImport("org.jboss.resteasy.reactive.RestResponse")

            kotlinClass(fileName, false) {
                addPathAnnotation("/")

                kotlinMember("delegate".variableName(), suite.name.extend(postfix = "Delegate").typeName())
                kotlinMember("objectMapper".variableName(), "ObjectMapper".rawTypeName())

                suite.requests.forEach {
                    generateRequest(it)
                }
            }
        }.also { generateFile(it) }
    }

    private fun KotlinClass.generateRequest(request: Request) {
        kotlinMethod(request.name, true, "RestResponse<*>".rawTypeName()) {
            kotlinAnnotation(request.method.name.uppercase().rawClassName())
            addPathAnnotation(request.path)

            val statement = RequestBuilderTransformStatement(
                request.name, request.name.extend(postfix = "Request").className()
            )

            request.parameters.forEach {
                kotlinParameter(it.name, it.type) {
                    kotlinAnnotation(
                        it.source.value.rawClassName(),
                        "value".variableName() to it.name.render().stringExpression()
                    )
//                    addTransformStatement(it.name, it.,)
                }
            }
            addStatement(statement)
        }
        /*
          private fun generateRequest(serverInterface: KotlinClass, request: Request) {


                request.parameters.forEach {
                    method.addTransformStatement(
                        it.name, it.type, it.validationInfo,
                        "request.${it.kind.name.lowercase()}.${it.name}".stringExpression(), context, false
                    ).also(builderTransform::addParameter)
                }

                request.body?.let {
                    method.addParameter("body".variableName(), "String".rawTypeName(true))
                    method.addTransformStatement(
                        "body", it.type, it.validationInfo,
                        "request.body".stringExpression(), context, true
                    ).also(builderTransform::addParameter)
                }

                builderTransform.addTo(method)
            }

         */
    }
}
