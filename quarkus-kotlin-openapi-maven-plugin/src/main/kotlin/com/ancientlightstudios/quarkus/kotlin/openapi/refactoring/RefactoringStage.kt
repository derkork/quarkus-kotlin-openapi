package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

class RefactoringStage(private val config: Config) : GeneratorStage {

    override fun process(spec: TransformableSpec) {
        val context = RefactoringContext(spec, config)

        // specify which is the direction for serialization and which for deserialization
        context.performRefactoring(PrepareSpecDirectionsRefactoring())

        context.performRefactoring(SplitByTagsRefactoring(config.splitByTags))

        // apply names to schemas if they don't have one yet
        context.performRefactoring(SchemaNameRefactoring())

        // first apply a few modifications
        context.performRefactoring(OptimizeSchemeRefactoring())

        // adds type information to schemas
        context.performRefactoring(AssignTypesToSchemasRefactoring(TypeMapper(config)))

        // apply flow information (content-types, so we know which methods are required for each model)
        // we need the generated types for this to know what to assign to nested types within a multipart
        context.performRefactoring(AssignContentTypesRefactoring())

        // split types if necessary, the direction for each type from the previous step is necessary for this
        context.performRefactoring(SplitTypeDefinitionRefactoring())

        // after types ready, we can assign them to the request and response objects
        context.performRefactoring(AssignTypesToRequestsRefactoring())

        // schemas have bidirectional or unidirectional type definitions. some of them are just overlays on top
        // of real types which must be exported. This refactoring adds these models as a list to the spec.
        context.performRefactoring(IdentifyRealTypeDefinitionsRefactoring())

        context.performRefactoring(PrepareBundleIdentifierRefactoring(config.interfaceName))
        context.performRefactoring(PrepareRequestIdentifierRefactoring())

        context.performRefactoring(ModelNameRefactoring())
        
        // type definitions which needs to be exported are identified, make sure their names don't collide with other
        // stuff
        context.performRefactoring(EnsureUniqueNamesRefactoring())
    }

}