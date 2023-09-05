package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaRef

class UnsafeModelQueueItem(val schemaRef: SchemaRef) : QueueItem() {

    // TODO: check arrays, enums and other stuff
    fun className() = "${schemaRef.resolve().typeName}Unsafe".className()

    override fun generate(config: Config, queue: (QueueItem) -> Unit): KotlinFile? {
        return null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnsafeModelQueueItem

        return schemaRef.id == other.schemaRef.id
    }

    override fun hashCode(): Int {
        return schemaRef.id.hashCode()
    }
}