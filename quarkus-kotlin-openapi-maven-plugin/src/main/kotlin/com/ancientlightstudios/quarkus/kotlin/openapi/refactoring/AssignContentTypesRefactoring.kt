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
                    parameters { propagate(parameter.typeDefinition, Direction.Up, ContentType.TextPlain) }

                    body { propagate(body.content.typeDefinition, Direction.Up, body.content.mappedContentType) }

                    responses {
                        headers { propagate(header.typeDefinition, Direction.Down, ContentType.TextPlain) }

                        body { propagate(body.content.typeDefinition, Direction.Down, body.content.mappedContentType) }
                    }
                }
            }
        }
    }

    private fun propagate(typeDefinition: TypeDefinition, direction: Direction, contentType: ContentType) {
        if (typeDefinition.addContentType(direction, contentType)) {
            // this content type was not set to the type yet. propagate it down
            when (typeDefinition) {
                // leaf types. nothing to do here
                is PrimitiveTypeDefinition,
                is EnumTypeDefinition -> return

                is CollectionTypeDefinition -> propagateToCollection(typeDefinition, direction, contentType)
                is ObjectTypeDefinition -> propagateToObject(typeDefinition, direction, contentType)
            }
        }
    }

    private fun propagateToObject(
        typeDefinition: ObjectTypeDefinition, direction: Direction, contentType: ContentType
    ) {
        typeDefinition.properties.forEach {
            val propertyType = it.schema.typeDefinition
            when (contentType) {
                ContentType.ApplicationJson -> propagate(propertyType, direction, contentType)

                ContentType.ApplicationFormUrlencoded,
                ContentType.MultipartFormData -> propagate(
                    propertyType, direction, getContentTypeForFormPart(propertyType)
                )

                ContentType.TextPlain,
                ContentType.ApplicationOctetStream -> ProbableBug("don't know how to handle $contentType for objects")
            }
        }
    }

    private fun propagateToCollection(
        typeDefinition: CollectionTypeDefinition, direction: Direction, contentType: ContentType
    ) {
        val itemType = typeDefinition.items.typeDefinition
        when (contentType) {
            ContentType.TextPlain,
            ContentType.ApplicationJson -> propagate(itemType, direction, contentType)

            ContentType.ApplicationFormUrlencoded,
            ContentType.MultipartFormData -> propagate(itemType, direction, getContentTypeForFormPart(itemType))

            ContentType.ApplicationOctetStream -> ProbableBug("don't know how to handle $contentType for collections")
        }
    }


    companion object {

        // TODO: this is just a quick solution. We have to think about all the possible combinations
        // and how we can override this via the openapi's encoding property
        // should only be used for nested stuff inside a multipart or urlencoded type
        fun getContentTypeForFormPart(typeDefinition: TypeDefinition): ContentType {
            return when (typeDefinition) {
                is PrimitiveTypeDefinition,
                is EnumTypeDefinition -> ContentType.TextPlain

                is CollectionTypeDefinition,
                is ObjectTypeDefinition -> ContentType.ApplicationJson
            }
        }

    }

}