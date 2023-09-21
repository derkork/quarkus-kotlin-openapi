package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import TransformerContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaRef

class SafeModelQueueItem(schemaRef: SchemaRef, private val context: TransformerContext) : QueueItem {

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
            is Schema.PrimitiveTypeSchema -> {
                return if (schema.shared) {
                    schema.typeName.substringAfterLast("/").className()
                } else {
                    schema.primitiveType.primitiveTypeClass()
                }
            }
            else -> schema.typeName.substringAfterLast("/").className()
        }
    }

    override fun generate(): KotlinFile? {
        // ignore primitive types
        if (schema is Schema.PrimitiveTypeSchema) {
            return if (schema.shared) {
                val content = KotlinValueClass(className(), schema.primitiveType.primitiveTypeClass().typeName())
                KotlinFile(content, "${context.config.packageName}.model").apply {
                    imports.addAll(libraryImports())
                }
            } else {
                // inline schema, nothing to do here
                null
            }
        }

        if (schema is Schema.EnumSchema) {
            val content = KotlinEnum(className(), schema.values.map { it to it.className() })
            return KotlinFile(content, "${context.config.packageName}.model").apply {
                imports.add("com.fasterxml.jackson.annotation.JsonProperty")
                imports.addAll(libraryImports())
            }
        }

        val content = KotlinClass(className()).apply {
            addAnnotation("RegisterForReflection".rawClassName())

            val properties = innerSchemaRef.getAllProperties()
            properties.forEach {
                val safeType = context.safeModelFor(it.type).className()
                addMember(
                    it.name.variableName(),
                    it.type.containerAsList(safeType, innerNullable = false, outerNullable = !it.validationInfo.required),
                    private = false
                ).apply {
                    annotations.add("field:JsonProperty".rawClassName(), "value".variableName() to it.name)
                }
            }
        }

        return KotlinFile(content, "${context.config.packageName}.model").apply {
            imports.add("com.fasterxml.jackson.annotation.JsonProperty")
            imports.add("io.quarkus.runtime.annotations.RegisterForReflection")
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