package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

class RefactoringStage(private val config: Config) : GeneratorStage {

    override fun process(spec: TransformableSpec) {
        val context = RefactoringContext(spec, config)

        if (config.splitByTags) {
            context.performRefactoring(SplitByTagsRefactoring())
        }

        context.performRefactoring(LinkSchemasAndSchemaDefinitionsRefactoring())

        context.performRefactoring(OptimizeSchemeDefinitionRefactoring())

        context.performRefactoring(SchemaDefinitionNameRefactoring())

        // mark up/down

        // mark readonly/writeonly
        // split schemadefintion/schema by direction

        // create types for schemadefinition
        // apply types to schemausage

        context.performRefactoring(PrepareBundleIdentifierRefactoring(config.interfaceName))
        context.performRefactoring(PrepareRequestIdentifierRefactoring())

        // verify unique names
    }

}