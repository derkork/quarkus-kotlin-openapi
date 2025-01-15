package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec

class RefactoringStage(private val config: Config) : GeneratorStage {

    override fun process(spec: OpenApiSpec) {
        listOf(
            // split the requests of the main bundle (the only one right now) by tags into smaller bundles,
            // if required by the configuration
            SplitRequestBundlesByTagsRefactoring(),

            // sets the `requestBundleIdentifier` for each request bundle
            SetRequestBundleIdentifierRefactoring(),

            // sets the `requestIdentifier` for each request
            SetRequestIdentifierRefactoring(),

            // apply names to schemas if they don't have one yet, based on a specified model name, the name of a
            // reference, or the previously set `requestIdentifier`
            SchemaNameRefactoring(),

            // first apply a few modifications
            OptimizeSchemaRefactoringGroup(),
        ).runRefactorings(RefactoringContext(spec, config))
    }

    private fun List<SpecRefactoring>.runRefactorings(context: RefactoringContext) {
        forEach {
            it.apply { context.perform() }
        }
    }

}