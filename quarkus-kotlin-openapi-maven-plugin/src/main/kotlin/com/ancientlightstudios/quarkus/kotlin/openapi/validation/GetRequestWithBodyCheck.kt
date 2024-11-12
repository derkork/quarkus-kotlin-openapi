package com.ancientlightstudios.quarkus.kotlin.openapi.validation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class GetRequestWithBodyCheck : Check {

    override fun verify(spec: OpenApiSpec) {
        spec.inspect {
            bundles {
                requests {
                    if (request.method == RequestMethod.Get && request.body != null) {
                        SpecIssue("Request body not allowed for GET requests. Found in ${request.originPath}")
                    }
                }
            }
        }
    }
}