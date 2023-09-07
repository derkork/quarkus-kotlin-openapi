package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class RequestContainerQueueItem(private val config: Config, private val request: Request) : QueueItem {

    fun className() = "${request.operationId}Request".className()

    override fun generate(queue: (QueueItem) -> Unit): KotlinFile {

        val content = KotlinClass(className())
        return KotlinFile(content, "${config.packageName}.model").apply {
        }

    }

    companion object {

        fun enqueueRequestContainer(config: Config, request: Request, queue: (QueueItem) -> Unit): RequestContainerQueueItem? {
            return if (request.parameters.isEmpty() && request.body == null) {
                null
            } else {
                RequestContainerQueueItem(config, request).enqueue(queue)
            }
        }

    }
}