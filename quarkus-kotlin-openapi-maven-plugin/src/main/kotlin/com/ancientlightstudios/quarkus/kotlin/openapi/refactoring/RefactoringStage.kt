package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

class RefactoringStage(private val config: Config) : GeneratorStage {

    override fun process(spec: TransformableSpec) {
        val context = RefactoringContext(spec, config)

        context.performRefactoring(SplitByTagsRefactoring(config.splitByTags))
        context.performRefactoring(LinkSchemasAndSchemaDefinitionsRefactoring())

        // first apply a few modifications
        context.performRefactoring(OptimizeSchemeDefinitionRefactoring())

        // apply names to schema definitions if they don't have one yet
        context.performRefactoring(SchemaDefinitionNameRefactoring())

        // adds type information to schema usages and schema definitions
        context.performRefactoring(AssignTypesRefactoring(TypeMapper(config)))

        // apply flow information (content-types, so we know which methods are required for each model)
        // we need the generated types for this to know what to assign to nested types within a multipart
        context.performRefactoring(AssignContentTypesRefactoring())

        // split schema definitions if necessary (we need the types for this,
        // because this is not important for primitive types)
        context.performRefactoring(SplitSchemaDefinitionsRefactoring())

        // TODO: mark definitions/types which are reachable and thus need to be generated

        context.performRefactoring(PrepareBundleIdentifierRefactoring(config.interfaceName))
        context.performRefactoring(PrepareRequestIdentifierRefactoring())

        context.performRefactoring(EnsureUniqueNamesRefactoring())
    }

}