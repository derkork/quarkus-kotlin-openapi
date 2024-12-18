package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.Serialization.getSerializationTargetType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestBundleInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientDelegateClassNameHint.clientDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestMethodNameHint.requestMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.ObjectTypeDefinition

class ClientDelegateEmitter(
    private val pathPrefix: String,
    private val interfaceName: String,
    private val additionalProviders: List<ClassName>
) : CodeEmitter {

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
            addPathAnnotation(pathPrefix)

            additionalProviders.forEach {
                kotlinAnnotation(Misc.RegisterProviderClass, it.classExpression())
            }

            requests {
                emitRequest(this@kotlinInterface)
            }
        }
    }

    private fun RequestInspection.emitRequest(containerInterface: KotlinInterface) = with(containerInterface) {
        kotlinMethod(
            request.requestMethodName,
            true,
            Misc.RestResponseClass.typeName().of(Kotlin.ByteArrayClass.typeName(true))
        ) {
            addRequestMethodAnnotation(request.method)
            addPathAnnotation(request.path)

            parameters { emitParameter(parameter) }
            body { emitBody(body) }
        }
    }

    private fun KotlinMethod.emitParameter(parameter: TransformableParameter) {
        val parameterKind = parameter.kind
        val parameterName = parameter.parameterVariableName

        kotlinParameter(parameterName, parameter.content.typeUsage.getSerializationTargetType()) {
            addAnnotation(getSourceAnnotation(parameterKind, parameter.name))
        }
    }

    private fun KotlinMethod.emitBody(body: TransformableBody) {
        val content = body.content
        val typeUsage = content.typeUsage
        addConsumesAnnotation(content.rawContentType)

        return when (body.content.mappedContentType) {
            ContentType.ApplicationJson ->
                kotlinParameter(body.parameterVariableName, typeUsage.getSerializationTargetType(true))
            ContentType.TextPlain ->
                kotlinParameter(body.parameterVariableName, typeUsage.getSerializationTargetType())

            ContentType.ApplicationFormUrlencoded -> {
                val type = typeUsage.type
                if (type is ObjectTypeDefinition) {
                    type.properties.forEach {
                        val parameter = body.parameterVariableName.extend(prefix = it.sourceName)
                        kotlinParameter(
                            parameter, typeUsage.getSerializationTargetType()
                        ) {
                            kotlinAnnotation(Jakarta.FormParamAnnotationClass, it.sourceName.literal())
                        }
                    }
                } else {
                    kotlinParameter(
                        body.parameterVariableName, typeUsage.getSerializationTargetType()
                    )
                }
            }

            ContentType.ApplicationOctetStream ->
                kotlinParameter(body.parameterVariableName, Kotlin.ByteArrayClass.typeName(typeUsage.isNullable()))

//            ContentType.MultipartFormData -> ProbableBug("Multipart-Form not yet implemented for client")
        }
    }

}