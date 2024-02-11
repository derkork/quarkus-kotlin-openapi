package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// apply flow information (content-types)
class AssignContentTypesRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                requests {
                    parameters { propagate(parameter.typeDefinition, ContentType.TextPlain) }

                    body { propagate(body.content.typeDefinition, body.content.mappedContentType) }

                    responses {
                        headers { propagate(header.typeDefinition, ContentType.TextPlain) }

                        body { propagate(body.content.typeDefinition, body.content.mappedContentType) }
                    }
                }
            }
        }
    }

    private fun propagate(typeDefinition: TypeDefinition, contentType: ContentType) {
        if (typeDefinition.addContentType(contentType)) {
            // this content type was not set to the type yet. propagate it down
            when (typeDefinition) {
                // leaf types. nothing to do here
                is PrimitiveTypeDefinition,
                is EnumTypeDefinition -> return

                is CollectionTypeDefinition -> propagateToCollection(typeDefinition, contentType)
                is ObjectTypeDefinition -> propagateToObject(typeDefinition, contentType)
            }
        }
    }

    private fun propagateToObject(typeDefinition: ObjectTypeDefinition, contentType: ContentType) {
        typeDefinition.properties.forEach {
            val propertyType = it.schema.typeDefinition
            when (contentType) {
                ContentType.ApplicationJson -> propagate(propertyType, contentType)

                ContentType.ApplicationFormUrlencoded,
                ContentType.MultipartFormData -> {
                    // TODO: it's possible to override this fallback logic via encoding in the OpenApi spec
                    when (propertyType) {
                        is PrimitiveTypeDefinition,
                        is EnumTypeDefinition -> propagate(propertyType, ContentType.TextPlain)

                        is CollectionTypeDefinition,
                        is ObjectTypeDefinition -> propagate(propertyType, ContentType.ApplicationJson)
                    }
                }

                ContentType.TextPlain,
                ContentType.ApplicationOctetStream -> ProbableBug("don't know how to handle $contentType for objects")
            }
        }
    }

    private fun propagateToCollection(typeDefinition: CollectionTypeDefinition, contentType: ContentType) {
        val itemType = typeDefinition.items.typeDefinition
        when (contentType) {
            ContentType.TextPlain,
            ContentType.ApplicationJson -> propagate(itemType, contentType)

            ContentType.ApplicationFormUrlencoded,
            ContentType.MultipartFormData -> {
                // TODO: it's possible to override this fallback logic via encoding in the OpenApi spec
                when (itemType) {
                    is PrimitiveTypeDefinition,
                    is EnumTypeDefinition -> propagate(itemType, ContentType.TextPlain)

                    is CollectionTypeDefinition,
                    is ObjectTypeDefinition -> propagate(itemType, ContentType.ApplicationJson)
                }
            }

            ContentType.ApplicationOctetStream -> ProbableBug("don't know how to handle $contentType for collections")
        }
    }

}