package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class ResponseBuilderStatement(
    private val responseType: ClassName,
    private val statusVariableName: VariableName,
    private val bodyVariableName: VariableName
) : KotlinStatement {
    override fun render(writer: CodeWriter) = with(writer) {
        writeln("return ${responseType.name}(ResponseBuilder.create<Any?>(RestResponse.Status.fromStatusCode(${statusVariableName.name}), ${bodyVariableName.name}).build())")
    }
}
