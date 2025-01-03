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


            // TODO: old legacy
            // adds type information to schemas
//            AssignTypesToSchemasRefactoring(TypeMapper(config)),

            // apply flow information (content-types, so we know which methods are required for each model)
            // we need the generated types for this to know what to assign to nested types within a multipart
//            AssignContentTypesRefactoring(),

            // split types if necessary, the direction for each type from the previous step is necessary for this
//            SplitTypeDefinitionRefactoring(),

            // after types ready, we can assign them to the request and response objects
//            AssignTypesToRequestsRefactoring(),

            // schemas have bidirectional or unidirectional type definitions. some of them are just overlays on top
            // of real types which must be exported. This refactoring adds these models as a list to the spec.
//            IdentifyRealTypeDefinitionsRefactoring(),

//            ModelNameRefactoring(),

            // type definitions which needs to be exported are identified, make sure their names don't collide with other
            // stuff
//            EnsureUniqueNamesRefactoring()
        ).runRefactorings(RefactoringContext(spec, config))
    }

    private fun List<SpecRefactoring>.runRefactorings(context: RefactoringContext) {
        forEach {
            it.apply { context.perform() }
        }
    }

}