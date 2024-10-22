package com.ancientlightstudios.quarkus.kotlin.openapi.validation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.BaseSchemaComponent.Companion.baseSchemaComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.OneOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class OneOfDiscriminatorCheck : Check {

    override fun verify(spec: TransformableSpec) {
        val oneOfsWithDiscriminator = spec.schemas.filter {
            it.components.any {
                it is OneOfComponent && it.discriminator != null
            }
        }

        oneOfsWithDiscriminator.forEach {
            it.inspect {
                components<OneOfComponent> {
                    component.schemas.forEach {
                        it.schema.baseSchemaComponent()
                            ?: SpecIssue("Inline schemas not supported if a oneOf schema has a discriminator. Found in ${schema.originPath}")
                    }
                }
            }
        }
    }

}