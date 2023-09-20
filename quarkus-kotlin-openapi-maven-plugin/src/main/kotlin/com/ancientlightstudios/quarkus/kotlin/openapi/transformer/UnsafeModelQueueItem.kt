package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import TransformerContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NestedPathExpression.Companion.nested
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaRef

class UnsafeModelQueueItem(schemaRef: SchemaRef, private val context:TransformerContext) : QueueItem {

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
            is Schema.PrimitiveTypeSchema -> "String".rawClassName()
            is Schema.EnumSchema -> "String".rawClassName()
            else -> (schema.typeName.substringAfterLast("/") + "Unsafe").className()
        }
    }

    override fun generate(): KotlinFile? {
        // ignore primitive types and enums
        if (schema is Schema.PrimitiveTypeSchema || schema is Schema.EnumSchema) {
            return null
        }

        val properties = innerSchemaRef.getAllProperties()
        val additionalImports = mutableSetOf<String>()

        val content = KotlinClass(className()).apply {
            addAnnotation("RegisterForReflection".rawClassName())

            properties.forEach {

                val unsafeType = context.unsafeModelFor(it.type).className()
                addMember(
                    it.name.variableName(),
                    it.type.containerAsList(unsafeType, innerNullable = true, outerNullable = true),
                    mutable = false,
                    private = false
                ).apply {
                    annotations.add("field:JsonProperty".rawClassName(), "value".variableName() to it.name)
                }
            }

            val safeType =context.safeModelFor(innerSchemaRef).className()

            withCompanion("Validator".className()) {

                val builderTransform =  SafeObjectBuilderTransformStatement(safeType)

                addMethod(
                    "validated".methodName(), false,
                    "Maybe".typeName().of(safeType.typeName(true)),
                    "Maybe".typeName().of(className().typeName(true))
                ).apply {
                    ValidationBoilerplateStatement().addTo(this).apply {
                        properties.forEach {
                            val (maybeVariable, statement) = convertToMaybe(
                                context,
                                it.type,
                                it.validationInfo,
                                "context".variableName(),
                                "value".variableName().nested(it.name.variableName()),
                                additionalImports::add,
                                false
                            )
                            statement.addTo(this)
                            builderTransform.addParameter(it.name, maybeVariable)
                        }

                        builderTransform.addTo(this)
                    }
                }
            }
        }

        return KotlinFile(content, "${context.config.packageName}.model").apply {
            imports.add("io.quarkus.runtime.annotations.RegisterForReflection")
            imports.addAll(libraryImports())
            imports.addAll(jacksonImports())
            imports.addAll(additionalImports)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnsafeModelQueueItem

        return innerSchemaRef.id == other.innerSchemaRef.id
    }

    override fun hashCode(): Int {
        return innerSchemaRef.id.hashCode()
    }

}