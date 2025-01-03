package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object SchemaNameHint : Hint<SchemaName> {

    var OpenApiSchema.schemaName: SchemaName
        get() = get(SchemaNameHint) ?: ProbableBug("Name not set for schema")
        set(value) = set(SchemaNameHint, value)

    fun OpenApiSchema.hasSchemaName() = has(SchemaNameHint)

}

data class SchemaName(val value: String, val strategy: TransformationStrategy = TransformationStrategy.Generated) {

    fun prefix(value: String) = extend(value, "")

    fun postfix(value: String) = extend("", value)

    fun extend(prefix: String = "", postfix: String = "") = when (strategy == TransformationStrategy.Requested) {
        // the name was requested, so we just append the stuff
        true -> SchemaName("$prefix$value$postfix", strategy)
        // this name will be processed later, so we use spaces as a delimiter to give hints for the formatter
        false -> SchemaName("$prefix $value $postfix", strategy)
    }

}

enum class TransformationStrategy {

    // Name requested by the developer. Will be used as it is, even if it is an invalid value
    Requested,

    // Name selected by the generator. Will be changed if necessary
    Generated

}