package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.primitiveTypeFor

class TypeDefinitionRegistry(private val schemas: MutableMap<SchemaDefinition, SchemaInfo>) {

    private val nameRegistry = NameRegistry()

    private val typeDefinitions = mutableMapOf<SchemaDefinition, MutableMap<FlowDirection, TypeDefinition>>()

    init {
        schemas.forEach { (definition, info) ->
            info.directions.forEach {
                buildTypeDefinition(definition, it)
            }
        }
    }

    // TODO: can never be an inline primitive. see schema collector and build it in a cleaner way
    private fun buildTypeDefinition(definition: SchemaDefinition, direction: FlowDirection): TypeDefinition {
        // do we already have a type definition?
        val existingTypeDefinition = typeDefinitions[definition]?.get(direction)
        if (existingTypeDefinition != null) {
            return existingTypeDefinition
        }

        // we have to build this thing

        val info = schemas[definition] ?: throw IllegalArgumentException(definition.toString())

        val asSingleClass = info.style == ObjectStyle.Single ||
                (info.style == ObjectStyle.Multiple && info.directions.size == 1)

        val name = when (asSingleClass) {
            true -> info.name
            false -> "${info.name} $direction"
        }.className()

        val typeDefinition = when (definition) {
            is PrimitiveSchemaDefinition -> sharedPrimitiveTypeDefinition(name, definition)
            is EnumSchemaDefinition -> enumTypeDefinition(name, definition)
            is ArraySchemaDefinition -> collectionTypeDefinition(definition, direction)
            is ObjectSchemaDefinition -> objectTypeDefinition(name, definition, direction)

            is OneOfSchemaDefinition -> TODO("OneOf schemas not yet implemented")
            is AnyOfSchemaDefinition -> TODO("AnyOf schemas not yet implemented")
            is AllOfSchemaDefinition -> allOfTypeDefinition(name, definition, direction)
        }

        return typeDefinition
    }

    fun getTypeDefinition(schema: Schema, direction: FlowDirection): TypeDefinition {
        if (schema is PrimitiveSchemaDefinition) {
            // it's an inline primitive type
            return InlinePrimitiveTypeDefinition(primitiveTypeFor(schema.type, schema.format), schema)
        }
        if (schema is ArraySchemaDefinition) {
            // it's an inline primitive type
            return collectionTypeDefinition(schema, direction)
        }

        // TODO: This treats references just as pointers. As soon as they can modify a scheme, we need to change this implementation
        val definition = getSchemaDefinition(schema)
        return buildTypeDefinition(definition, direction)
    }

    private fun sharedPrimitiveTypeDefinition(name: ClassName, definition: PrimitiveSchemaDefinition): TypeDefinition {
        val result = SharedPrimitiveTypeDefinition(
            nameRegistry.uniqueNameFor(name),
            primitiveTypeFor(definition.type, definition.format),
            definition
        )
        typeDefinitions[definition] = mutableMapOf(FlowDirection.Up to result, FlowDirection.Down to result)
        return result
    }

    private fun enumTypeDefinition(name: ClassName, definition: EnumSchemaDefinition): TypeDefinition {
        val result = EnumTypeDefinition(
            nameRegistry.uniqueNameFor(name),
            primitiveTypeFor(definition.type, definition.format),
            definition
        )
        typeDefinitions[definition] = mutableMapOf(FlowDirection.Up to result, FlowDirection.Down to result)
        return result
    }

    private fun collectionTypeDefinition(definition: ArraySchemaDefinition, direction: FlowDirection): TypeDefinition {
        val result = CollectionTypeDefinition(
            "List".rawClassName(),
            getTypeDefinition(definition.itemSchema, direction),
            definition
        )
        typeDefinitions.getOrPut(definition) { mutableMapOf() }[direction] = result
        return result
    }

    private fun objectTypeDefinition(
        name: ClassName,
        definition: ObjectSchemaDefinition,
        direction: FlowDirection
    ): TypeDefinition {
        val result = ObjectTypeDefinition(name, definition.nullable, getProperties(definition, direction))
        typeDefinitions.getOrPut(definition) { mutableMapOf() }[direction] = result
        return result
    }

    private fun allOfTypeDefinition(
        name: ClassName,
        definition: AllOfSchemaDefinition,
        direction: FlowDirection
    ): TypeDefinition {
        val result = ObjectTypeDefinition(name, definition.nullable, getProperties(definition, direction))
        typeDefinitions.getOrPut(definition) { mutableMapOf() }[direction] = result
        return result
    }

    fun getAllTypeDefinitions(directionFilter: FlowDirection? = null): List<TypeDefinition> {
        var types = typeDefinitions.values.flatMap { it.map { it.key to it.value } }

        if (directionFilter != null) {
            types = types.filter { it.first == directionFilter }
        }

        return types.map { it.second }
    }

    private fun getSchemaDefinition(schema: Schema): SchemaDefinition =
        when (schema) {
            is SchemaDefinition -> schema
            is SchemaReference<*> -> getSchemaDefinition(schema.target)
        }

    private fun getProperties(schema: SchemaDefinition, direction: FlowDirection): List<ObjectProperty> {
        val result = mutableListOf<ObjectProperty>()

        val filter = when (direction) {
            FlowDirection.Up -> { property: SchemaProperty -> property.direction != Direction.ReadOnly }
            FlowDirection.Down -> { property: SchemaProperty -> property.direction != Direction.WriteOnly }
        }

        if (schema is ObjectSchemaDefinition) {
            result.addAll(schema.properties
                .filter { (_, property) -> filter(property) }
                .map { (name, property) ->
                    ObjectProperty(name, property, getTypeDefinition(property.schema, direction).useAs(property.required))
                }
            )
        } else if (schema is AllOfSchemaDefinition) {
            result.addAll(schema.schemas.map { getSchemaDefinition(it) }.flatMap { getProperties(it, direction) })
        }

        return result
    }
}

