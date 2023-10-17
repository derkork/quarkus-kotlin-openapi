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

        typeDefinitions.getOrPut(definition) { mutableMapOf() }[direction] = typeDefinition
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

    private fun sharedPrimitiveTypeDefinition(name: ClassName, definition: PrimitiveSchemaDefinition) =
        SharedPrimitiveTypeDefinition(
            nameRegistry.uniqueNameFor(name),
            primitiveTypeFor(definition.type, definition.format),
            definition
        )

    private fun enumTypeDefinition(name: ClassName, definition: EnumSchemaDefinition) =
        EnumTypeDefinition(
            nameRegistry.uniqueNameFor(name),
            primitiveTypeFor(definition.type, definition.format),
            definition
        )

    private fun collectionTypeDefinition(definition: ArraySchemaDefinition, direction: FlowDirection) =
        CollectionTypeDefinition(
            "List".rawClassName(),
            getTypeDefinition(definition.itemSchema, direction),
            definition
        )

    private fun objectTypeDefinition(
        name: ClassName,
        definition: ObjectSchemaDefinition,
        direction: FlowDirection
    ): TypeDefinition {
        val filter = when (direction) {
            FlowDirection.Up -> { property: SchemaProperty -> property.direction != Direction.ReadOnly }
            FlowDirection.Down -> { property: SchemaProperty -> property.direction != Direction.WriteOnly }
        }
        return ObjectTypeDefinition(name, definition, filter)
    }

    private fun allOfTypeDefinition(
        name: ClassName,
        definition: AllOfSchemaDefinition,
        direction: FlowDirection
    ): TypeDefinition {
        val filter = when (direction) {
            FlowDirection.Up -> { property: SchemaProperty -> property.direction != Direction.ReadOnly }
            FlowDirection.Down -> { property: SchemaProperty -> property.direction != Direction.WriteOnly }
        }
        return AllOfTypeDefinition(name, definition, filter)
    }


    fun getAllTypeDefinitions() = typeDefinitions.values.flatMap { it.values }

    private fun getSchemaDefinition(schema: Schema): SchemaDefinition =
        when (schema) {
            is SchemaDefinition -> schema
            is SchemaReference<*> -> getSchemaDefinition(schema.target)
        }

}

