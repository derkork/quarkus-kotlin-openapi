package com.ancientlightstudios.quarkus.kotlin.openapi.validation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class ResponseBodyContentTypeCheck : Check {

    override fun verify(spec: TransformableSpec) {
        spec.inspect {
            bundles {
                requests {
                    responses {
                        body {
                            if (body.content.isEmpty()) {
                                SpecIssue("At least one content type for the body of a response is required. Found in ${response.originPath}")
                            }

                            if (body.content.size > 1) {
                                SpecIssue("More than one content type for the body of a response is not yet supported. Found in ${response.originPath}")
                            }
                        }
                    }
                }
            }
        }
    }
}