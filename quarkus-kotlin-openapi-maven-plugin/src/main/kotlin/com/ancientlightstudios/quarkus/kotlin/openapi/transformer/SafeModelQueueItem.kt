package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Name
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaRef

class SafeModelQueueItem(val schemaRef: SchemaRef) : QueueItem() {
    fun className() = Name.ClassName(schemaRef.resolve().typeName)


    override fun generate(config: Config, queue: (QueueItem) -> Unit): KotlinFile? {
        return null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SafeModelQueueItem

        return schemaRef.id == other.schemaRef.id
    }

    override fun hashCode(): Int {
        return schemaRef.id.hashCode()
    }
}