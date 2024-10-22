package com.ancientlightstudios.quarkus.kotlin.openapi.validation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.BaseSchemaComponent.Companion.baseSchemaComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.OneOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class OneOfDiscriminatorMappingCheck : Check {

    override fun verify(spec: TransformableSpec) {
        val oneOfsWithDiscriminator = spec.schemas.filter {
            it.components.any {
                it is OneOfComponent && it.discriminator != null && it.discriminator.additionalMappings.isNotEmpty()
            }
        }

        oneOfsWithDiscriminator.forEach {
            it.inspect {
                components<OneOfComponent> {
                    // if the oneOf has a discriminator with additional mappings they must match to a $ref in one of the schemas

                    component.discriminator!!.additionalMappings.values.forEach { mapping ->
                        val found = component.schemas.any {
                            val baseSchema = it.schema.baseSchemaComponent()
                            when(baseSchema) {
                                null -> false
                                else -> baseSchema.schema.originPath == mapping
                            }
                        }

                        if (!found) {
                            SpecIssue("No schema found for discriminator mapping $mapping in oneOf schema ${schema.originPath}")
                        }
                    }
                }
            }
        }
    }

}