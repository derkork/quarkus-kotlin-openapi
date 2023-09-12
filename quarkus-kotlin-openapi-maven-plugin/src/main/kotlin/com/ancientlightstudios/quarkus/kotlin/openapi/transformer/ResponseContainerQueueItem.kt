package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import TransformerContext
import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class ResponseContainerQueueItem(private val request: Request, private val context: TransformerContext) : QueueItem {

    fun className() = "${request.operationId}Response".className()

    override fun generate(): KotlinFile {

        val content = KotlinClass(className(), privateConstructor = true).apply {
            addMember("response".variableName(), "RestResponse<*>".rawTypeName(), private = false)
            withCompanion {
                addMethod("status".methodName(), false, className().typeName()).apply {
                    addParameter("status".variableName(), "Int".rawTypeName())
                    addParameter("body".variableName(), "Any".rawTypeName(true))

                    ResponseBuilderStatement(
                        className(),
                        "status".variableName(),
                        "body".variableName()
                    ).addTo(this)
                }
            }
        }

        /* // TODO: implement proper companion methods for responses indicated in OpenAPI spec
           val returnType = request.returnType?.let {
                    val inner = SafeModelQueueItem(config, it).enqueue(queue).className()
                    it.containerAsList(inner, innerNullable = false, outerNullable = false)
                }

         */


        return KotlinFile(content, "${context.config.packageName}.model").apply {
            imports.add("org.jboss.resteasy.reactive.RestResponse.ResponseBuilder")
            imports.add("org.jboss.resteasy.reactive.RestResponse")
        }

    }

}