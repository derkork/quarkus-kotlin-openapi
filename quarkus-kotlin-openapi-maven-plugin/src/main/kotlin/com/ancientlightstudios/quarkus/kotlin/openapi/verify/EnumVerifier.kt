package com.ancientlightstudios.quarkus.kotlin.openapi.verify

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.EnumValidation
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class EnumVerifier : Verifier {

    override fun verify(apiSpec: ApiSpec) {
        val enumSchemas = apiSpec.getAllSchemas()
            .filterIsInstance<Schema.PrimitiveSchema>()
            .filter { it.defaultValue != null }
            .filter { it.validations.any { it is EnumValidation } }
            .associateWith { it.validations.filterIsInstance<EnumValidation>().first() }

        enumSchemas.forEach { (schema, validation) ->
            if (!validation.values.contains(schema.defaultValue)) {
                SpecIssue("Default value for schema ${schema.originPath} is not a valid enum value.")
            }
        }
    }
}