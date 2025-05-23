package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequestBundle

// creates new request bundles if requests should be separated by tags
class SplitRequestBundlesByTagsRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        if (!config.splitByTags) {
            return
        }

        val requests = mutableListOf<Pair<String?, OpenApiRequest>>()

        // get all requests associated with their main tag
        spec.inspect {
            bundles {
                requests.addAll(bundle.requests.map {
                    it.tags.getMainTag() to it
                })
            }
        }

        val tagGroups = requests.groupBy({ it.first }, { it.second })
        if (tagGroups.size < 2) {
            // no tag, or only one. nothing to do here
            return
        }

        spec.bundles = tagGroups.map { (tag, requests) ->
            OpenApiRequestBundle(tag, requests)
        }
    }

    // the main tag is just the first tag if any is specified.
    private fun List<String>.getMainTag() = firstOrNull()?.trim()

}