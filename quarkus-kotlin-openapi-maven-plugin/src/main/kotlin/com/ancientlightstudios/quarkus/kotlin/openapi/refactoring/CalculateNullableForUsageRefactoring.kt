package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ForceNullableHint.forceNullable
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.ObjectComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.ObjectValidationComponent

class CalculateNullableForUsageRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                requests {
                    parameters {
                        if (!parameter.required) {
                            parameter.schema.forceNullable()
                        }
                    }

                    body {
                        if (!body.required) {
                            content {
                                content.schema.forceNullable()
                            }
                        }
                    }

                    responses {
                        headers {
                            if (!header.required) {
                                header.schema.forceNullable()
                            }
                        }

                        body {
                            if (!body.required) {
                                content {
                                    content.schema.forceNullable()
                                }
                            }
                        }
                    }
                }
            }

            // TODO: not sure if this will cover all cases, but it is a start for now
            schemaDefinitions {
                val requiredProperties = mutableListOf<String>()
                components<ObjectValidationComponent> { requiredProperties.addAll(component.required) }
                components<ObjectComponent> {
                    component.properties.forEach {
                        if (!requiredProperties.contains(it.name)) {
                            it.schema.forceNullable()
                        }
                    }
                }
            }
        }
    }

}
