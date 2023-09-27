package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import TransformerContext
import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class RequestContainerQueueItem(private val request: Request, private val context: TransformerContext) : QueueItem {

    fun className() = "${request.operationId}Request".className()

    override fun generate(): KotlinFile {

        val content = KotlinClass(className())

        request.parameters.forEach {
            val inner = context.safeModelFor(it.type).className()
            val finalType =
                // TODO: innerNullable depends in the spec
                it.type.containerAsList(inner, innerNullable = false, outerNullable = !it.validationInfo.required)
            content.addMember(it.name.variableName(), finalType, private = false)
        }

        request.body?.let {
            val inner = context.safeModelFor(it.type).className()
            val finalType =
                // TODO: innerNullable depends in the spec
                it.type.containerAsList(inner, innerNullable = false, outerNullable = !it.validationInfo.required)
            content.addMember("body".variableName(), finalType, private = false)
        }

        return KotlinFile(content, "${context.config.packageName}.model").apply {
        }

    }
}