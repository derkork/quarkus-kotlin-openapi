package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinEnum
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaRef

class SafeModelQueueItem(private val config: Config, schemaRef: SchemaRef) : QueueItem {

    private val innerSchemaRef: SchemaRef
    private val schema: Schema

    init {
        var currentSchemaRef = schemaRef
        var currentSchema = currentSchemaRef.resolve()


        while (currentSchema is Schema.ArraySchema) {
            currentSchemaRef = currentSchema.items
            currentSchema = currentSchemaRef.resolve()
        }

        innerSchemaRef = currentSchemaRef
        schema = currentSchema
    }

    fun className(): ClassName {
        return when (schema) {
            is Schema.PrimitiveTypeSchema -> schema.typeName.primitiveTypeClass()
            else -> schema.typeName.substringAfterLast("/").className()
        }
    }

    override fun generate(queue: (QueueItem) -> Unit): KotlinFile? {
        // ignore primitive types
        if (schema is Schema.PrimitiveTypeSchema) {
            return null
        }

        if (schema is Schema.EnumSchema) {
            val content = KotlinEnum(className(), schema.values.map { it to it.className() })
            return KotlinFile(content, "${config.packageName}.model").apply {
                imports.add("com.fasterxml.jackson.annotation.JsonProperty")
            }
        }

        val content = KotlinClass(className())
        return KotlinFile(content, "${config.packageName}.model").apply {
            imports.add("com.fasterxml.jackson.annotation.JsonProperty")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SafeModelQueueItem

        return innerSchemaRef.id == other.innerSchemaRef.id
    }

    override fun hashCode(): Int {
        return innerSchemaRef.id.hashCode()
    }

}