package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.BaseSchemaComponent.Companion.baseSchemaComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.ContainerModelNameComponent.Companion.containerModelNameComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.NullableComponent.Companion.nullableComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaModifierComponent.Companion.schemaModifierComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.ValidationComponent.Companion.validationComponents
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

class CreateSimpleOneOfTypeRefactoring(
    private val schema: TransformableSchema,
    private val oneOf: OneOfComponent,
    private val typeResolver: TypeResolver
) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        val nullable = schema.nullableComponent()?.nullable
        val modifier = schema.schemaModifierComponent()?.modifier
        val validations = schema.validationComponents().map { it.validation }

        val discriminator = oneOf.discriminator

        val typeName = schema.name.className(modelPackage)
        val optionTypes = oneOf.schemas.map { schemaUsage ->
            val optionSchema = schemaUsage.schema
            // by default options are always required. But it's still possible to define the schema as nullable
            val usage = TypeUsage(true)
            // lazy lookup in case the item schema is not yet converted
            typeResolver.schedule(usage) { optionSchema.typeDefinition }

            // generate a generic container name unless one was specified
            val containerName = when(val name = schemaUsage.schema.containerModelNameComponent()) {
                null ->typeName.extend(postfix = schemaUsage.schema.name)
                else -> name.value.className(modelPackage)
            }

            val aliases = getAliases(optionSchema, discriminator)
            OneOfOption(containerName, usage, aliases)
        }

        schema.typeDefinition = RealOneOfTypeDefinition(
            typeName,
            discriminator?.let { OneOfDiscriminatorProperty(it.property, it.property.variableName()) },
            nullable ?: false,
            modifier,
            optionTypes,
            validations
        )
    }

    private fun getAliases(optionSchema: TransformableSchema, discriminator: OneOfDiscriminator?) : List<String> {
        // TODO: little hack for now
        return if (discriminator != null) {
            val originPath = when(val baseSchema = optionSchema.baseSchemaComponent()) {
                null -> optionSchema.originPath
                else -> baseSchema.schema.originPath
            }
            val defaultAlias = originPath.substringAfterLast("/")
            val additionalAliases = discriminator.additionalMappings.filter { it.value == originPath }.keys
            listOf(defaultAlias, *additionalAliases.toTypedArray())
        } else {
            listOf()
        }
    }

}
