package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
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
        // TODO: This treats references just as pointers. As soon as they can modify a scheme, we need to change this implementation
        val (name, definition) = getSchemaDefinition(schema, namingHint)
        // check if we already registered this schema. if yes, update the directions in case we haven't seen this yet
        val data = schemaData.computeIfPresent(definition) { _, info ->
            info.addDirection(direction)
        }

        if (data != null) {
            return data.style
        }

        return when (definition) {
            is PrimitiveSchemaDefinition -> registerSchema(definition, direction, name)
            is ArraySchemaDefinition -> registerArraySchema(definition, direction, name)
            is ObjectSchemaDefinition, is AllOfSchemaDefinition -> registerObjectSchema(definition, direction, name)
            is OneOfSchemaDefinition -> registerOneOfSchema(definition, direction, name)
            is AnyOfSchemaDefinition -> registerAnyOfSchema(definition, direction, name)
        }
    }

    private fun registerSchema(
        definition: SchemaDefinition, direction: FlowDirection,
        name: String, style: ObjectStyle = ObjectStyle.Single
    ): ObjectStyle {
        schemaData[definition] = SchemaInfo(style, direction, name)
        return style
    }

    private fun registerArraySchema(
        definition: ArraySchemaDefinition, direction: FlowDirection, name: String
    ): ObjectStyle {
        registerSchema(definition, direction, name)
        return registerSchema(definition.itemSchema, direction) { "$name item" }
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

        return if (propertyStyles.contains(ObjectStyle.Multiple)) {
            registerSchema(definition, direction, name, ObjectStyle.Multiple)
        } else {
            registerSchema(definition, direction, name, ObjectStyle.Single)
        }
    }

    fun registerAnyOfSchema(definition: AnyOfSchemaDefinition, direction: FlowDirection, name: String): ObjectStyle {
        val styles = definition.schemas.mapIndexed { index, schema ->
            registerSchema(schema, direction) { "$name option $index" }
        }

        return if (styles.contains(ObjectStyle.Multiple)) {
            registerSchema(definition, direction, name, ObjectStyle.Multiple)
        } else {
            registerSchema(definition, direction, name, ObjectStyle.Single)
        }
    }

    fun registerOneOfSchema(definition: OneOfSchemaDefinition, direction: FlowDirection, name: String): ObjectStyle {
        val styles = definition.schemas.keys.mapIndexed { index, schema ->
            registerSchema(schema, direction) { "$name option $index" }
        }

        return if (styles.contains(ObjectStyle.Multiple)) {
            registerSchema(definition, direction, name, ObjectStyle.Multiple)
        } else {
            registerSchema(definition, direction, name, ObjectStyle.Single)
        }
    }

    private fun getSchemaDefinition(schema: Schema, namingHint: () -> String): Pair<String, SchemaDefinition> =
        when (schema) {
            is SchemaDefinition -> namingHint() to schema
            is SchemaReference<*> -> getSchemaDefinition(schema.target) { schema.targetName }
        }

    fun getTypeDefinitionRegistry(config: Config) = TypeDefinitionRegistry(schemaData, config)

}
