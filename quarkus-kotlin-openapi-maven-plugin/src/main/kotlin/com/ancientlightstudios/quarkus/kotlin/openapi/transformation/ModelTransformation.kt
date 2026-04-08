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
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

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

        modelHulls.filterIsInstance<OneOfModelClass>().forEach {
            finalizeOneOfAliases(it)
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
        val properties = schema.getComponent<ObjectComponent>()?.properties ?: listOf()
        val required = schema.getComponent<ObjectValidationComponent>()?.required ?: listOf()
        val itemsSchema = schema.getComponent<MapComponent>()?.schema
        val hasPropertyValidation =
            schema.getComponent<ValidationComponent>()?.validations?.any { it is PropertiesValidation } ?: false

        // remove any property which has a modifier set and this modifier is not compatible with the models direction
        val validProperties = when (model.direction) {
            SchemaDirection.UnidirectionalUp -> properties.discardWhen(SchemaModifier.ReadOnly)
            SchemaDirection.UnidirectionalDown -> properties.discardWhen(SchemaModifier.WriteOnly)
            SchemaDirection.Bidirectional -> properties
        }

        validProperties.forEach {
            val modelInstance = modelInstanceFor(it.schema, model.direction, required.contains(it.name))
            model.properties += ObjectModelProperties(variableNameOf(it.name), it.name, ModelUsage(modelInstance))
        }

        if (itemsSchema != null) {
            model.additionalProperties = ModelUsage(modelInstanceFor(itemsSchema, model.direction, true))
        }

        model.needsPropertiesCount = hasPropertyValidation
    }

    private fun List<OpenApiSchemaProperty>.discardWhen(modifier: SchemaModifier) = filterNot {
        it.schema.getComponent<SchemaModifierComponent>()?.modifier == modifier
    }

    private fun TransformationContext.finalizeOneOfModel(model: OneOfModelClass) {
        val schema = model.source
        val oneOf = schema.getComponent<OneOfComponent>()
            ?: ProbableBug("OneOf component not found at oneOf schema")

        oneOf.options.forEach { option ->
            val requestedContainerName = option.schema.getComponent<ContainerModelNameComponent>()?.value
            val containerName = when (requestedContainerName) {
                null -> schema.schemaName.postfix(option.schema.schemaName.value).toContainerName(config.modelPackageName)
                else -> ComponentName(requestedContainerName, config.modelPackageName, ConflictResolution.Requested)
            }

            oneOf.discriminator?.let {
                model.discriminator = OneOfModelDiscriminator(variableNameOf(it.property), it.property)
            }

            model.options += OneOfModelOption(
                containerName,
                ModelUsage(modelInstanceFor(option.schema, model.direction, true)),
                option.schema,
                // these two values will be calculated in a later step
                listOf(),
                false
            )
        }
    }

    private fun TransformationContext.finalizeOneOfAliases(model: OneOfModelClass) {
        val oneOfComponent = model.source.getComponent<OneOfComponent>()
            ?: ProbableBug("OneOf component not found at oneOf schema")

        // the following steps are only necessary for oneOf models with a discriminator property
        if (oneOfComponent.discriminator == null) {
            return
        }

        val propertyName = oneOfComponent.discriminator.property
        val additionalMappings = oneOfComponent.discriminator.additionalMappings

        model.options.forEach { option ->
            val optionSchema = option.source
            val optionModel = option.model

            val objectModel = optionModel.instance as? ObjectModelInstance ?:
                SpecIssue("OneOf with discriminator requires objects as options. Found in ${model.source.originPath}.")

            // oneOf with discriminator doesn't work with inline schemas, so there should always be a useful schema name.
            // We have to use the name suggestion hint instead of the schema name hint, because the second might contain
            // the name requested by the developer via x-model-name annotation for the model, and this is not useful here
            val implicitAlias = optionSchema.nameSuggestion
                ?: ProbableBug("OneOf with discriminator contains a schema without a name. Found in ${model.source.originPath}.")

            val explicitAliases = additionalMappings.filter { it.value == optionSchema.originPath }.keys

            val property = objectModel.ref.properties.firstOrNull { it.sourceName == propertyName }
                ?: SpecIssue("OneOf with discriminator requires discriminator property in ${optionSchema.originPath}. Found in ${model.source.originPath}")

            // by default, the implicit and all explicit aliases are valid
            val allAliases = mutableListOf(implicitAlias, *explicitAliases.toTypedArray())
            var enforceAliasValue = true

            val propertyInstance = property.model.instance
            if (propertyInstance is EnumModelInstance) {
                // in the case of an enum, the generated code doesn't need to set the discriminator value, because it is always a valid value
                enforceAliasValue = false

                val enumItems = propertyInstance.ref.items.map { it.value }.toMutableSet()

                // the implicit alias can be omitted if there is no matching item in the enum.
                // it is also removed from the remaining enum items to check explicit aliases in the next step
                if (!enumItems.remove(implicitAlias)) {
                    allAliases.removeAt(0)
                }

                if (!enumItems.containsAll(explicitAliases) || enumItems.size != explicitAliases.size) {
                    SpecIssue("Enumeration for property ${property.name} in ${optionSchema.originPath} does not contain all aliases.")
                }

                if (allAliases.isEmpty()) {
                    SpecIssue("Enumeration for property ${property.name} in ${optionSchema.originPath} does not contain any aliases.")
                }
            }

            option.aliases = allAliases.distinct()
            option.enforceAliasValue = enforceAliasValue
        }

    }

    private fun SchemaName.toContainerName(targetPackage: String) = when (strategy) {
        TransformationStrategy.Requested -> ComponentName(value, targetPackage, ConflictResolution.Requested)
        TransformationStrategy.Generated -> ComponentName(classNameOf(value), targetPackage)
    }

    private val Config.modelPackageName
        get() = "$packageName.model"
}