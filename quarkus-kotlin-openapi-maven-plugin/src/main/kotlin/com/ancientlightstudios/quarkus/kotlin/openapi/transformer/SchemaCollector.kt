package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.*

class SchemaInfo(val style: ObjectStyle, direction: FlowDirection, val name: String) {

    private val _directions = mutableSetOf(direction)

    val directions: Set<FlowDirection>
        get(): Set<FlowDirection> = _directions

    fun addDirection(direction: FlowDirection): SchemaInfo {
        _directions.add(direction)
        return this
    }

}

class SchemaCollector {

    private val schemaData = mutableMapOf<SchemaDefinition, SchemaInfo>()

    fun registerSchema(schema: Schema?, direction: FlowDirection, namingHint: () -> String) {
        schema?.let { registerSchema(it, direction, namingHint) }
    }

    private fun registerSchema(schema: Schema, direction: FlowDirection, namingHint: () -> String): ObjectStyle {
        if (schema is PrimitiveSchemaDefinition) {
            // it's an inline primitive type
            return ObjectStyle.Single
        }

        val (name, definition) = getSchemaDefinition(schema, namingHint)
        // check if we already registered this schema. if yes, update the directions in case we haven't seen this yet
        val data = schemaData.computeIfPresent(definition) { _, info ->
            info.addDirection(direction)
        }

        if (data != null) {
            return data.style
        }

        // TODO: This treats references just as pointers. As soon as they can modify a scheme, we need to change this implementation
        return when (definition) {
            is PrimitiveSchemaDefinition, is EnumSchemaDefinition -> registerSimpleSchema(definition, direction, name)
            is ArraySchemaDefinition -> registerSchema(definition.itemSchema, direction) { "$name item" }
            is ObjectSchemaDefinition, is AllOfSchemaDefinition -> registerObjectSchema(definition, direction, name)
            is OneOfSchemaDefinition -> TODO("OneOf schemas not yet implemented")
            is AnyOfSchemaDefinition -> TODO("AnyOf schemas not yet implemented")
        }
    }

    private fun registerSimpleSchema(
        definition: SchemaDefinition,
        direction: FlowDirection,
        name: String
    ): ObjectStyle {
        val schemaInfo = SchemaInfo(ObjectStyle.Single, direction, name)
        schemaData[definition] = schemaInfo
        return schemaInfo.style
    }

    private fun registerObjectSchema(
        definition: SchemaDefinition,
        direction: FlowDirection,
        name: String
    ): ObjectStyle {
        val propertyStyles = definition.getAllProperties().map { (propertyName, property) ->
            val style = registerSchema(property.schema, direction) { "$name $propertyName" }
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

        schemaData[definition] = SchemaInfo(result, direction, name)
        return result
    }

    private fun getSchemaDefinition(schema: Schema, namingHint: () -> String): Pair<String, SchemaDefinition> =
        when (schema) {
            is SchemaDefinition -> namingHint() to schema
            is SchemaReference<*> -> getSchemaDefinition(schema.target) { schema.targetName }
        }

    fun getTypeDefinitionRegistry() = TypeDefinitionRegistry(schemaData)

}
