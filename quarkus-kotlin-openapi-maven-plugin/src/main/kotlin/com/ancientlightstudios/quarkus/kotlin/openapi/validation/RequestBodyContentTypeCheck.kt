package com.ancientlightstudios.quarkus.kotlin.openapi.validation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class RequestBodyContentTypeCheck : Check {

    override fun verify(spec: TransformableSpec) {
        spec.inspect {
            bundles {
                requests {
                    body {
                        if (body.content.isEmpty()) {
                            SpecIssue("At least one content type for the body of a request is required. Found in ${request.originPath}")
                        }

                        if (body.content.size > 1) {
                            SpecIssue("More than one content type for the body of a request is not yet supported. Found in ${request.originPath}")
                        }
                    }
                }
            }
        }
    }
}