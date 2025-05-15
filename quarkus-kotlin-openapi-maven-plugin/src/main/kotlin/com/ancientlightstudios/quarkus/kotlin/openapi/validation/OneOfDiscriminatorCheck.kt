package com.ancientlightstudios.quarkus.kotlin.openapi.validation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.BaseSchemaComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.OneOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class OneOfDiscriminatorCheck : Check {

    override fun verify(spec: OpenApiSpec) {
        val oneOfsWithDiscriminator = spec.schemas.filter {
            it.components.any {
                it is OneOfComponent && it.discriminator != null
            }
        }

        oneOfsWithDiscriminator.forEach {
            it.inspect {
                components<OneOfComponent> {
                    component.options.forEach {
                        if (!it.schema.hasComponent<BaseSchemaComponent>()) {
                            SpecIssue("Inline schemas not supported if a oneOf schema has a discriminator. Found in ${schema.originPath}")
                        }
                    }
                }
            }
        }
    }

}