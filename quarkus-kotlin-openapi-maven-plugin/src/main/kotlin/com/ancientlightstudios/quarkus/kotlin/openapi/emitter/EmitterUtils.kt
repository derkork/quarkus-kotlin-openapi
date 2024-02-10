package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.RequestMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

fun AnnotationAware.addPathAnnotation(path: String) {
    kotlinAnnotation(Jakarta.PathAnnotationClass, "value".variableName() to path.literal())
}

fun AnnotationAware.addRequestMethodAnnotation(method: RequestMethod) {
    val className = when (method) {
        RequestMethod.Get -> Jakarta.GetAnnotationClass
        RequestMethod.Put -> Jakarta.PutAnnotationClass
        RequestMethod.Post -> Jakarta.PostAnnotationClass
        RequestMethod.Delete -> Jakarta.DeleteAnnotationClass
        RequestMethod.Options -> Jakarta.OptionsAnnotationClass
        RequestMethod.Head -> Jakarta.HeadAnnotationClass
        RequestMethod.Patch -> Jakarta.PatchAnnotationClass
        RequestMethod.Trace -> Jakarta.TraceAnnotationClass
    }
    kotlinAnnotation(className)
}

fun AnnotationAware.addConsumesAnnotation(vararg contentTypes: String) {
    kotlinAnnotation(Jakarta.ConsumesAnnotationClass,
        "value".variableName() to contentTypes.toList().arrayLiteral { it.literal() })
}

fun AnnotationAware.addSourceAnnotation(source: ParameterKind, name: String) {
    val annotationClass = when (source) {
        ParameterKind.Path -> Jakarta.PathParamAnnotationClass
        ParameterKind.Query -> Jakarta.QueryParamAnnotationClass
        ParameterKind.Header -> Jakarta.HeaderParamAnnotationClass
        ParameterKind.Cookie -> Jakarta.CookieParamAnnotationClass
    }
    kotlinAnnotation(annotationClass, name.literal())
}

fun TypeDefinition.buildValidType(forceNullable: Boolean = false): TypeName {
    val isNullable = forceNullable || nullable

    return when (this) {
        is PrimitiveTypeDefinition -> baseType.typeName(isNullable)
        is EnumTypeDefinition -> modelName.typeName(isNullable)
        is ObjectTypeDefinition -> modelName.typeName(isNullable)
        is CollectionTypeDefinition -> Kotlin.ListClass.typeName(isNullable)
            .of(items.typeDefinition.buildValidType())
    }
}
