package com.ancientlightstudios.quarkus.kotlin.openapi.validation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class ResponseBodyContentTypeCheck : Check {

    override fun verify(spec: TransformableSpec) {
        // fail, if there is more than one content-type for a response body,
        // or if there are different content-types within all the responses of a request
        spec.inspect {
            bundles {
                requests {
                    val contentTypes = mutableSetOf<ContentType>()

                    responses {
                        body {
                            if (body.contentTypes.size > 1) {
                                SpecIssue("More than one content type for the body of a response is not yet supported. Found in ${response.originPath}")
                            }

                            contentTypes.addAll(body.contentTypes)
                        }
                    }

                    if (contentTypes.size > 1) {
                        SpecIssue("More than one content type for the responses of a request is not yet supported. Found in ${request.originPath}")
                    }
                }
            }
        }
    }
}