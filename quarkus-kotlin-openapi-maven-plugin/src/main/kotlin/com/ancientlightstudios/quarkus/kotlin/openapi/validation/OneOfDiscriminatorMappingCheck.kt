package com.ancientlightstudios.quarkus.kotlin.openapi.validation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.BaseSchemaComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.OneOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class OneOfDiscriminatorMappingCheck : Check {

    override fun verify(spec: OpenApiSpec) {
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
                        val found = component.options.any {
                            when (val baseSchema = it.schema.getComponent<BaseSchemaComponent>()) {
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