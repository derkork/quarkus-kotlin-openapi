package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

// TODO: encoding in case of multipart/form-data or application/x-www-form-urlencoded
class TransformableContentMapping(
    var mappedContentType: ContentType,
    var rawContentType: String,
    override var schema: TransformableSchema
) : TransformableObject(), SchemaUsage

