package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.NullableComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.OneOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaModifierComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.ValidationComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.OneOfOption
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.RealOneOfTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeUsage

class CreateSimpleOneOfTypeRefactoring(
    private val schema: TransformableSchema,
    private val oneOf: OneOfComponent,
    private val lazyTypeUsage: (TypeUsage, () -> TypeDefinition) -> Unit
) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        val nullable = schema.getComponent<NullableComponent>()?.nullable
        val modifier = schema.getComponent<SchemaModifierComponent>()?.modifier
        val validations = schema.getComponents<ValidationComponent>().map { it.validation }

        val typeName = schema.name.className(modelPackage)
        val optionTypes = oneOf.schemas.map { schemaUsage ->
            // by default options are always required. But it's still possible to define the schema as nullable
            val usage = TypeUsage(true)
            // lazy lookup in case the item schema is not yet converted
            lazyTypeUsage(usage) { schemaUsage.schema.typeDefinition }
            OneOfOption(typeName.extend(postfix = schemaUsage.schema.name), usage, emptyList())
        }

        schema.typeDefinition = RealOneOfTypeDefinition(
            typeName,
            nullable ?: false,
            modifier,
            optionTypes,
            validations
        )
    }

}
