package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

// TODO: encoding in case of multipart/form-data or application/x-www-form-urlencoded
class ContentMapping(
    val mappedContentType: ContentType,
    val rawContentType: String,
    val schema: TransformableSchemaUsage
)

