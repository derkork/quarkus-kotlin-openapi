package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.EnumValidation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.primitiveTypeFor

class TypeDefinitionRegistry(private val schemas: MutableMap<SchemaDefinition, SchemaInfo>, private val nameRegistry:NameRegistry, private val config: Config) {

    private val typeDefinitions = mutableMapOf<SchemaDefinition, MutableMap<FlowDirection, TypeDefinition>>()

    init {
        schemas.forEach { (definition, info) ->
            info.directions.forEach {
                buildTypeDefinition(definition, info, it)
            }
        }
    }

    private fun buildTypeDefinition(definition: SchemaDefinition, info: SchemaInfo, direction: FlowDirection): TypeDefinition {
        // do we already have a type definition?
        val existingTypeDefinition = typeDefinitions[definition]?.get(direction)
        if (existingTypeDefinition != null) {
            return existingTypeDefinition
        }

        val asSingleClass = info.style == ObjectStyle.Single ||
                (info.style == ObjectStyle.Multiple && info.directions.size == 1)

        // if we have a single class, try to get the already made type definition from the other direction
        val otherDirection = if (direction == FlowDirection.Up) FlowDirection.Down else FlowDirection.Up


        val otherDirectionTypeDefinition = if (asSingleClass) typeDefinitions[definition]?.get(otherDirection) else null

        val name = when {
            // re-use the same name if we have a single class for both directions
            otherDirectionTypeDefinition != null -> otherDirectionTypeDefinition.name
            asSingleClass -> nameRegistry.uniqueNameFor( info.name.className())
            else -> nameRegistry.uniqueNameFor( "${info.name} $direction".className())
        }

        val typeDefinition = when (definition) {
            is PrimitiveSchemaDefinition -> primitiveTypeDefinition(name, definition)
            is ArraySchemaDefinition -> collectionTypeDefinition(definition, direction)
            is ObjectSchemaDefinition -> objectTypeDefinition(name, definition, direction)

            is OneOfSchemaDefinition -> oneOfTypeDefinition(name, definition, direction)
            is AnyOfSchemaDefinition -> anyOfTypeDefinition(name, definition, direction)
            is AllOfSchemaDefinition -> allOfTypeDefinition(name, definition, direction)
        }

        return typeDefinition
    }

    fun getTypeDefinition(schema: Schema, direction: FlowDirection): TypeDefinition {
        // TODO: This treats references just as pointers. As soon as they can modify a scheme, we need to change this implementation
        val definition = getSchemaDefinition(schema)

        val info = schemas[definition] ?: ProbableBug("Schema $definition not found")

        return buildTypeDefinition(definition, info, direction)
    }

    private fun primitiveTypeDefinition(name: ClassName, definition: PrimitiveSchemaDefinition) : TypeDefinition {
        val primitiveTypeInfo = primitiveTypeFor(config, definition.type, definition.format)

        // for a plain primitive type there is only one enum validation, so we can search for the first one
        val enumValidation = definition.validations.filterIsInstance<EnumValidation>().firstOrNull()
        val result = if (enumValidation != null) {
            EnumTypeDefinition(
                name,
                primitiveTypeInfo.className,
                definition,
                enumValidation.values
            )
        } else {
            PrimitiveTypeDefinition(primitiveTypeInfo.className, primitiveTypeInfo.serializeMethodName,
                primitiveTypeInfo.deserializeMethodName, definition)
        }
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
        val result =
            ObjectTypeDefinition(name, definition.nullable, definition.validations, getProperties(definition, direction))
        typeDefinitions.getOrPut(definition) { mutableMapOf() }[direction] = result
        return result
    }

    private fun allOfTypeDefinition(
        name: ClassName,
        definition: AllOfSchemaDefinition,
        direction: FlowDirection
    ): TypeDefinition {
        val result =
            ObjectTypeDefinition(name, definition.nullable, definition.validations, getProperties(definition, direction))
        typeDefinitions.getOrPut(definition) { mutableMapOf() }[direction] = result
        return result
    }

    private fun anyOfTypeDefinition(
        name: ClassName,
        definition: AnyOfSchemaDefinition,
        direction: FlowDirection
    ): TypeDefinition {
        val schemas = definition.schemas.map { getTypeDefinition(it, direction).useAs(false) }
        val result = AnyOfTypeDefinition(name, definition.nullable, definition.validations, schemas)
        typeDefinitions.getOrPut(definition) { mutableMapOf() }[direction] = result
        return result
    }

    private fun oneOfTypeDefinition(
        name: ClassName,
        definition: OneOfSchemaDefinition,
        direction: FlowDirection
    ): TypeDefinition {
        val schemas = definition.schemas.mapKeysTo(linkedMapOf()) { getTypeDefinition(it.key, direction).useAs(!it.key.nullable) }
        val result = OneOfTypeDefinition(name, definition.nullable, definition.validations, schemas, definition.discriminator)
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
                    ObjectProperty(
                        name,
                        property,
                        getTypeDefinition(property.schema, direction).useAs(property.required)
                    )
                }
            )
        } else if (schema is AllOfSchemaDefinition) {
            result.addAll(schema.schemas.map { getSchemaDefinition(it) }.flatMap { getProperties(it, direction) })
        }

        return result
    }
}

