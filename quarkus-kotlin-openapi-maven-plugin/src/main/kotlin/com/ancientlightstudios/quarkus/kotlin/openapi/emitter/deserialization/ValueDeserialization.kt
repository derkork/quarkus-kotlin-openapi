package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Kotlin
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.CollectionTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// returns the "unsafe" type for a schema used in a request parameter or body
fun TransformableSchemaUsage.getDeserializationSourceType(contentType: ContentType): TypeName {
    return when (val definition = typeDefinition) {
        is CollectionTypeDefinition -> Kotlin.ListClass.typeName(true)
            .of(definition.items.getDeserializationSourceType(contentType))

        else -> Kotlin.StringClass.typeName(true)
    }
}

fun emitDeserializationStatement(
    statement: KotlinExpression,
    schema: TransformableSchemaUsage,
    contentType: ContentType
): KotlinExpression {
    return when (contentType) {
        ContentType.ApplicationJson -> emitJsonDeserializationStatement(statement, schema)
        ContentType.TextPlain -> emitPlainDeserializationStatement(statement, schema)
        else -> ProbableBug("Content-Type ${contentType.value} not expected for a deserialization statement")
    }
}

fun emitValidationStatement(baseStatement: KotlinExpression): KotlinExpression {
    // TODO custom validation
    // TODO string validation
    // TODO number validation
    // TODO array validation
    return baseStatement
}