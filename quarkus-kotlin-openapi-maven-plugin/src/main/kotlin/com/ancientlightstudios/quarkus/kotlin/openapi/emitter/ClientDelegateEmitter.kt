package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.Serialization.getSerializationTargetType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestBundleInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientDelegateClassNameHint.clientDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestMethodNameHint.requestMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class ClientDelegateEmitter(private val interfaceName: String) : CodeEmitter {

    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this
        spec.inspect {
            bundles {
                emitDelegateFile()
                    .writeFile()
            }
        }
    }

    private fun RequestBundleInspection.emitDelegateFile() = kotlinFile(bundle.clientDelegateClassName) {
        registerImports(Library.AllClasses)
        registerImports(emitterContext.getAdditionalImports())

        kotlinInterface(fileName) {
            val configKeyName = "$interfaceName client".toKebabCase().literal()
            kotlinAnnotation(Misc.RegisterRestClientClass, "configKey".variableName() to configKeyName)

            requests {
                emitRequest(this@kotlinInterface)
            }
        }
    }

    private fun RequestInspection.emitRequest(containerInterface: KotlinInterface) = with(containerInterface) {
        kotlinMethod(request.requestMethodName, true, Misc.RestResponseClass.typeName().of(Kotlin.StringClass, true)) {
            addRequestMethodAnnotation(request.method)
            addPathAnnotation(request.path)

            parameters { emitParameter(parameter) }
            body { emitBody(body) }
        }
    }

    private fun KotlinMethod.emitParameter(parameter: TransformableParameter) {
        val parameterKind = parameter.kind
        val parameterName = parameter.parameterVariableName

        kotlinParameter(parameterName, parameter.typeDefinition.getSerializationTargetType(!parameter.required)) {
            addAnnotation(getSourceAnnotation(parameterKind, parameter.name))
        }
    }

    private fun KotlinMethod.emitBody(body: TransformableBody) {
        val content = body.content
        val typeDefinition = content.typeDefinition
        addConsumesAnnotation(content.rawContentType)

        return when (body.content.mappedContentType) {
            ContentType.ApplicationJson,
            ContentType.TextPlain ->
                kotlinParameter(body.parameterVariableName, typeDefinition.getSerializationTargetType(!body.required))

            ContentType.ApplicationFormUrlencoded -> {

                if (typeDefinition is ObjectTypeDefinition) {
                    typeDefinition.properties.forEach {
                        val parameter = body.parameterVariableName.extend(prefix = it.sourceName)
                        kotlinParameter(
                            parameter, typeDefinition.getSerializationTargetType(!body.required)
                        ) {
                            kotlinAnnotation(Jakarta.FormParamAnnotationClass, it.sourceName.literal())
                        }
                    }
                } else {
                    kotlinParameter(
                        body.parameterVariableName, typeDefinition.getSerializationTargetType(!body.required)
                    )
                }
            }

            ContentType.MultipartFormData,
            ContentType.ApplicationOctetStream -> ProbableBug("not yet implemented")
        }
    }

}