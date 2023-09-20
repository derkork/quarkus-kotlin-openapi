package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import TransformerContext
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

                request.responses.forEach {
                    addMethod(it.code.statusCodeReason().methodName(), false, bodyAsAssignment = true).apply {
                        if (it.type != null) {
                            val responseType = context.safeModelFor(it.type).className()
                            val returnType =
                                it.type.containerAsList(responseType, innerNullable = false, outerNullable = false)

                            addParameter("body".variableName(), returnType)
                            addStatement(kotlinStatement {
                                write("status(${it.code}, body)")
                            })
                        } else {
                            addStatement(kotlinStatement {
                                write("status(${it.code}, null)")
                            })
                        }
                    }
                }
            }
        }

        return KotlinFile(content, "${context.config.packageName}.model").apply {
            imports.add("org.jboss.resteasy.reactive.RestResponse.ResponseBuilder")
            imports.add("org.jboss.resteasy.reactive.RestResponse")
        }

    }

}