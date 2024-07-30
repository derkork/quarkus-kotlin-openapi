package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientDelegateClassNameHint.clientDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientErrorResponseClassNameHint.clientErrorResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientHttpResponseClassNameHint.clientHttpResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientRestInterfaceClassNameHint.clientRestInterfaceClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ModelTypesHint.modelTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestBuilderClassNameHint.requestBuilderClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerClassNameHint.requestContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContextClassNameHint.requestContextClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerClassNameHint.responseContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseValidatorClassNameHint.responseValidatorClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerDelegateClassNameHint.serverDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerRestInterfaceClassNameHint.serverRestInterfaceClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

class ModelNameRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        val prefix = config.modelNamePrefix.trim()
        val postfix = config.modelNamePostfix.trim()
        if (prefix.isBlank() && postfix.isBlank()) {
            return
        }

        spec.modelTypes
            .filterNot { it is TypeDefinitionOverlay }
            .forEach {
                when (it) {
                    is PrimitiveTypeDefinition,
                    is CollectionTypeDefinition -> return@forEach

                    // this cast is safe, because overlays were filtered out before
                    is EnumTypeDefinition ->
                        (it as RealEnumTypeDefinition).modelName = it.modelName.extend(prefix, postfix)

                    is ObjectTypeDefinition ->
                        (it as RealObjectTypeDefinition).modelName = it.modelName.extend(prefix, postfix)

                    is OneOfTypeDefinition -> {
                        (it as RealOneOfTypeDefinition).modelName = it.modelName.extend(prefix, postfix)
                        it.options.forEach { 
                            it.modelName = it.modelName.extend(prefix, postfix)
                        }
                    }
                }
            }
    }

}
