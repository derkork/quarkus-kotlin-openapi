package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.*

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

fun AnnotationAware.addProducesAnnotation(contentTypes: Collection<ContentType>) {
    kotlinAnnotation(Jakarta.ProducesAnnotationClass,
        "value".variableName() to contentTypes.arrayLiteral { it.value.literal() })
}

fun AnnotationAware.addConsumesAnnotation(contentTypes: Collection<ContentType>) {
    kotlinAnnotation(Jakarta.ConsumesAnnotationClass,
        "value".variableName() to contentTypes.arrayLiteral { it.value.literal() })
}

fun AnnotationAware.addSourceAnnotation(parameter: TransformableParameter) {
    val source = when (parameter) {
        is TransformablePathParameter -> Jakarta.PathParamAnnotationClass
        is TransformableQueryParameter -> Jakarta.QueryParamAnnotationClass
        is TransformableHeaderParameter -> Jakarta.HeaderParamAnnotationClass
        is TransformableCookieParameter -> Jakarta.CookieParamAnnotationClass
    }
    kotlinAnnotation(source, parameter.name.literal())
}