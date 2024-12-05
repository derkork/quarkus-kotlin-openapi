package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec

class RefactoringStage(private val config: Config) : GeneratorStage {

    override fun process(spec: OpenApiSpec) {
        listOf(
            SplitRequestBundlesByTagsRefactoring(),
            SetRequestBundleIdentifierRefactoring(),
            SetRequestIdentifierRefactoring(),

            // TODO: old legacy

            // specify which is the direction for serialization and which for deserialization
            PrepareSpecDirectionsRefactoring(),

            // apply names to schemas if they don't have one yet
            SchemaNameRefactoring(),

            // first apply a few modifications
            OptimizeSchemeRefactoring(),

            // adds type information to schemas
            AssignTypesToSchemasRefactoring(TypeMapper(config)),

            // apply flow information (content-types, so we know which methods are required for each model)
            // we need the generated types for this to know what to assign to nested types within a multipart
            AssignContentTypesRefactoring(),

            // split types if necessary, the direction for each type from the previous step is necessary for this
            SplitTypeDefinitionRefactoring(),

            // after types ready, we can assign them to the request and response objects
            AssignTypesToRequestsRefactoring(),

            // schemas have bidirectional or unidirectional type definitions. some of them are just overlays on top
            // of real types which must be exported. This refactoring adds these models as a list to the spec.
            IdentifyRealTypeDefinitionsRefactoring(),

            PrepareBundleIdentifierRefactoring(config.interfaceName),
            PrepareRequestIdentifierRefactoring(),

            ModelNameRefactoring(),

            // type definitions which needs to be exported are identified, make sure their names don't collide with other
            // stuff
            EnsureUniqueNamesRefactoring()
        ).runRefactorings(RefactoringContext(spec, config))
    }

    private fun List<SpecRefactoring>.runRefactorings(context: RefactoringContext) {
        forEach {
            it.apply { context.perform() }
        }
    }

}