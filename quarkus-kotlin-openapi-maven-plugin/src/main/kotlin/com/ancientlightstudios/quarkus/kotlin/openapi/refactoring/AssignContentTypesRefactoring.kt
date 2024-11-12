package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// apply flow information (content-types)
class AssignContentTypesRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                requests {
                    parameters {
                        propagate(
                            parameter.content.schema.typeDefinition, Direction.Up, parameter.content.mappedContentType
                        )
                    }

                    body { propagate(body.content.schema.typeDefinition, Direction.Up, body.content.mappedContentType) }

                    responses {
                        headers {
                            propagate(
                                header.content.schema.typeDefinition, Direction.Down, header.content.mappedContentType
                            )
                        }

                        body {
                            propagate(
                                body.content.schema.typeDefinition, Direction.Down, body.content.mappedContentType
                            )
                        }
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
                is OneOfTypeDefinition -> propagateToOneOf(typeDefinition, direction, contentType)
            }
        }
    }

    private fun propagateToOneOf(typeDefinition: OneOfTypeDefinition, direction: Direction, contentType: ContentType) {
        typeDefinition.options.forEach {
            propagate(it.typeUsage.type, direction, contentType)
        }
    }

    private fun propagateToObject(
        typeDefinition: ObjectTypeDefinition, direction: Direction, contentType: ContentType
    ) {
        typeDefinition.additionalProperties?.let {
            val propertyType = it.type
            when (contentType) {
                ContentType.ApplicationJson -> propagate(propertyType, direction, contentType)
                else -> ProbableBug("don't know how to handle $contentType for maps")
            }
        }

        typeDefinition.properties.forEach {
            val propertyType = it.typeUsage.type
            when (contentType) {
                ContentType.ApplicationJson -> propagate(propertyType, direction, contentType)

//                ContentType.MultipartFormData,
                ContentType.ApplicationFormUrlencoded -> propagate(
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
        val itemType = typeDefinition.items.type
        when (contentType) {
            ContentType.TextPlain,
            ContentType.ApplicationJson -> propagate(itemType, direction, contentType)

//            ContentType.MultipartFormData ->
            ContentType.ApplicationFormUrlencoded -> propagate(itemType, direction, getContentTypeForFormPart(itemType))

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
                is ObjectTypeDefinition,
                is OneOfTypeDefinition -> ContentType.ApplicationJson
            }
        }

    }

}