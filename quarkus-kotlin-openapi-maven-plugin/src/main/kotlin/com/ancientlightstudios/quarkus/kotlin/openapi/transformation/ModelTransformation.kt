package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.NameSuggestionHint.nameSuggestion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirectionHint.hasSchemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirectionHint.schemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaModeHint.hasSchemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaNameHint.schemaName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaTargetModelHint.schemaTargetModel
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SplitFlagHint.hasSplitFlag
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchemaProperty
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class ModelTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        val reachableSchemas = spec.schemas
            // the spec can contain schemas which are not required anymore (e.g. if a schema is used by another which
            // itself creates a model class instead of just overlaying the fist one). In this case the schema has no direction
            // because it is no used anywhere
            .filter { it.hasSchemaDirection() }
            // and only transform real models. overlay are handled later
            .filter { it.hasSchemaMode(SchemaMode.Model) }

        val modelHulls = mutableListOf<ModelClass>()

        // the transformation works in two steps. first step is to create the empty hull for each final model class
        reachableSchemas.forEach {
            if (it.hasSplitFlag()) {
                // only bidirectional schemas are marked with the split flag, and we need to create two different model classes
                transformSchema(it, SchemaDirection.UnidirectionalUp, modelHulls, "Up")
                transformSchema(it, SchemaDirection.UnidirectionalDown, modelHulls, "Down")
            } else {
                // everything else can be converted into a single model class
                transformSchema(it, it.schemaDirection, modelHulls)
            }
        }

        // second step is to fill those empty hulls. This is necessary, because models can depend on each other, even
        // recursively, and it could be impossible to find the right order
        modelHulls.forEach {
            when (it) {
                is EnumModelClass -> finalizeEnumModel(it)
                is ObjectModelClass -> finalizeObjectModel(it)
                is OneOfModelClass -> finalizeOneOfModel(it)
            }
        }
    }

    private fun TransformationContext.transformSchema(
        schema: OpenApiSchema, direction: SchemaDirection, list: MutableList<ModelClass>, nameSuffix: String? = null
    ) {
        // extend the model name, if requested by the developer
        val schemaName = schema.schemaName.extend(config.modelNamePrefix, config.modelNamePostfix)

        // check if there is an additional suffix due to a split
        val finalSchemaName = when (nameSuffix) {
            null -> schemaName
            else -> schemaName.postfix(nameSuffix)
        }

        // convert into the final model name
        val fileName = finalSchemaName.toContainerName(config.modelPackageName)

        val modelClass = when (val targetModel = schema.schemaTargetModel) {
            is SchemaTargetModel.EnumModel -> EnumModelClass(fileName, targetModel.base, direction, schema)
            SchemaTargetModel.ObjectModel -> ObjectModelClass(fileName, direction, schema)
            SchemaTargetModel.OneOfModel -> OneOfModelClass(fileName, direction, schema)
            else -> return // the remaining types don't generate model classes and can be skipped here
        }

        list.add(modelClass)
        registerModelClass(modelClass)
        spec.solution.files.add(modelClass)
    }

    private fun TransformationContext.finalizeEnumModel(model: EnumModelClass) {
        val schema = model.source
        val enumItems = schema.getComponent<EnumValidationComponent>()?.values ?: listOf()
        val enumNames = schema.getComponent<EnumItemNamesComponent>()?.values ?: mapOf()

        enumItems.forEach {
            // if a name is specified by the developer, use it as it is, otherwise try to create a name out of the item value
            val name = enumNames[it] ?: constantNameOf(it)
            model.items += EnumModelItem(name, it)
        }
    }

    private fun TransformationContext.finalizeObjectModel(model: ObjectModelClass) {
        val schema = model.source
        val properties = schema.getComponent<ObjectComponent>()?.properties
            ?: ProbableBug("Object component not found at object schema")
        val required = schema.getComponent<ObjectValidationComponent>()?.required ?: listOf()
        val itemsSchema = schema.getComponent<MapComponent>()?.schema

        // remove any property which has a modifier set and this modifier is not compatible with the models direction
        val validProperties = when (model.direction) {
            SchemaDirection.UnidirectionalUp -> properties.discardWhen(SchemaModifier.ReadOnly)
            SchemaDirection.UnidirectionalDown -> properties.discardWhen(SchemaModifier.WriteOnly)
            SchemaDirection.Bidirectional -> properties
        }

        validProperties.forEach {
            val modelInstance = modelInstanceFor(it.schema, model.direction, required.contains(it.name))
            model.properties += ObjectModelProperties(methodNameOf(it.name), it.name, modelInstance)
        }

        if (itemsSchema != null) {
            model.additionalProperties = modelInstanceFor(itemsSchema, model.direction, true)
        }
    }

    private fun List<OpenApiSchemaProperty>.discardWhen(modifier: SchemaModifier) = filterNot {
        it.schema.getComponent<SchemaModifierComponent>()?.modifier == modifier
    }

    private fun TransformationContext.finalizeOneOfModel(model: OneOfModelClass) {
        val schema = model.source
        val oneOf = schema.getComponent<OneOfComponent>()
            ?: ProbableBug("OneOf component not found at oneOf schema")

        oneOf.options.forEach {
            val requestedContainerName = it.schema.getComponent<ContainerModelNameComponent>()?.value
            val containerName = when (requestedContainerName) {
                null -> schema.schemaName.postfix(it.schema.schemaName.value).toContainerName(config.modelPackageName)
                else -> ComponentName(requestedContainerName, config.modelPackageName, ConflictResolution.Requested)
            }

            model.options += OneOfModelOption(
                containerName,
                modelInstanceFor(it.schema, model.direction, true),
                oneOf.discriminator?.getAliasesFor(it.schema) ?: listOf()
            )
        }
    }

    private fun OneOfDiscriminator.getAliasesFor(optionSchema: OpenApiSchema): List<String> {
        // one of with discriminator doesn't work with inline schemas, so there should always be a useful schema name.
        // We have to use the name suggestion hint instead of the schema name hint, because the second might contain
        // the name requested by the developer via x-model-name annotation, and we can't use this
        val defaultAlias = optionSchema.nameSuggestion
            ?: ProbableBug("OneOf with discriminator contains a schema without a name")
        val additionalAliases = additionalMappings.filter { it.value == optionSchema.originPath }.keys
        return listOf(defaultAlias, *additionalAliases.toTypedArray())
    }

    private fun SchemaName.toContainerName(targetPackage: String) = when (strategy) {
        TransformationStrategy.Requested -> ComponentName(value, targetPackage, ConflictResolution.Requested)
        TransformationStrategy.Generated -> ComponentName(classNameOf(value), targetPackage)
    }

    private val Config.modelPackageName
        get() = "$packageName.model"
}