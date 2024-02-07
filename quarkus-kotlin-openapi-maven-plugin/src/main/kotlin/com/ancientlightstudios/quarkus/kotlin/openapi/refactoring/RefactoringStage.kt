package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

class RefactoringStage(private val config: Config) : GeneratorStage {

    override fun process(spec: TransformableSpec) {
        val context = RefactoringContext(spec, config)

        context.performRefactoring(SplitByTagsRefactoring(config.splitByTags))
        context.performRefactoring(LinkSchemasAndSchemaDefinitionsRefactoring())

        // context.performRefactoring(AssignContentTypesToSchemaDefinitionsRefactoring())
        context.performRefactoring(CalculateNullableForUsageRefactoring())

        context.performRefactoring(OptimizeSchemeDefinitionRefactoring())
        context.performRefactoring(SchemaDefinitionNameRefactoring())
        context.performRefactoring(SplitSchemaDefinitionsRefactoring())
        context.performRefactoring(AssignTypesRefactoring(TypeMapper(config)))
        context.performRefactoring(PrepareBundleIdentifierRefactoring(config.interfaceName))
        context.performRefactoring(PrepareRequestIdentifierRefactoring())
        context.performRefactoring(EnsureUniqueNamesRefactoring())
    }

}