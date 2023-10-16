package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.*
import kotlin.experimental.or

class SchemaCollector {

    private val schemaData = mutableMapOf<Schema, Pair<ObjectStyle, Byte>>()

    fun registerSchema(schema: Schema?, direction: FlowDirection) {
        schema?.let { registerSchema(it, direction) }
    }

    private fun registerSchema(schema: Schema, direction: FlowDirection): ObjectStyle {
        // they always have only one representation
        if (schema is Schema.PrimitiveSchema || schema is Schema.EnumSchema) {
            return ObjectStyle.Single
        }

        // check if we already registered this schema. if yes, update the directions in case we haven't seen this yet
        val data = schemaData.computeIfPresent(schema) { _, (style, knownDirections) ->
            style to (knownDirections or direction.value)
        }

        if (data != null) {
            return data.first
        }

        // TODO: This treats references just as pointers. As soon as they can modify a scheme, we need to change this implementation
        val result = when (val definition = getSchemaDefinition(schema)) {
            // compiler doesn't know, that these two cases are impossible, because we already checked them at the start of the function
            is PrimitiveSchemaDefinition, is EnumSchemaDefinition -> throw IllegalStateException("you found a bug.")
            is ArraySchemaDefinition -> registerSchema(definition.itemSchema, direction)
            is ObjectSchemaDefinition -> registerObjectSchema(definition, direction)
            is OneOfSchemaDefinition -> TODO("OneOf schemas not yet implemented")
            is AnyOfSchemaDefinition -> TODO("AnyOf schemas not yet implemented")
            is AllOfSchemaDefinition -> registerAllOfSchema(definition, direction)
        }

        return result
    }

    private fun registerObjectSchema(definition: ObjectSchemaDefinition, direction: FlowDirection): ObjectStyle {
        val propertyStyles = definition.properties.map { (_, property) ->
            val style = registerSchema(property.schema, direction)
            when {
                style == ObjectStyle.Multiple -> style
                property.direction != Direction.ReadAndWrite -> ObjectStyle.Multiple
                else -> ObjectStyle.Single
            }
        }

        val result = if (propertyStyles.contains(ObjectStyle.Multiple)) {
            ObjectStyle.Multiple
        } else {
            ObjectStyle.Single
        }

        schemaData[definition] = result to direction.value

        return result
    }

    private fun registerAllOfSchema(definition: AllOfSchemaDefinition, direction: FlowDirection): ObjectStyle {
        // TODO: this can produce class files even if nobody really uses them
        val nestedStyles = definition.schemas.map { registerSchema(it, direction) }

        val result = if (nestedStyles.contains(ObjectStyle.Multiple)) {
            ObjectStyle.Multiple
        } else {
            ObjectStyle.Single
        }

        schemaData[definition] = result to direction.value
        
        return result
    }

    private fun getSchemaDefinition(schema: Schema): SchemaDefinition =
        when (schema) {
            is SchemaDefinition -> schema
            is SchemaReference<*> -> getSchemaDefinition(schema.target)
        }

    fun getTypeDefinitionRegistry(): TypeDefinitionRegistry {
        TODO("Not yet implemented")
    }

    private enum class ObjectStyle {
        Single,
        Multiple
    }

}