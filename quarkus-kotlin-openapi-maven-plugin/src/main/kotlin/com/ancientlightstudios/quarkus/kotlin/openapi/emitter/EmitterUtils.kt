package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.RequestMethod

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

fun AnnotationAware.addProducesAnnotation(vararg contentTypes: ContentType) {
    kotlinAnnotation(Jakarta.ProducesAnnotationClass,
        "value".variableName() to contentTypes.toList().arrayLiteral { it.value.literal() })
}

fun AnnotationAware.addConsumesAnnotation(vararg contentTypes: ContentType) {
    kotlinAnnotation(Jakarta.ConsumesAnnotationClass,
        "value".variableName() to contentTypes.toList().arrayLiteral { it.value.literal() })
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