package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.RequestMethod
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

fun getSourceAnnotation(source: ParameterKind, name: String): KotlinAnnotation {
    val annotationClass = when (source) {
        ParameterKind.Path -> Jakarta.PathParamAnnotationClass
        ParameterKind.Query -> Jakarta.QueryParamAnnotationClass
        ParameterKind.Header -> Jakarta.HeaderParamAnnotationClass
        ParameterKind.Cookie -> Jakarta.CookieParamAnnotationClass
    }
    return KotlinAnnotation(annotationClass, null to name.literal())
}

fun TypeUsage.buildValidType(): TypeName {
    return when (val safeType = this.type) {
        is PrimitiveTypeDefinition -> safeType.baseType.typeName(nullable)
        is EnumTypeDefinition -> safeType.modelName.typeName(nullable)
        is ObjectTypeDefinition -> safeType.modelName.typeName(nullable)
        is CollectionTypeDefinition -> Kotlin.ListClass.typeName(nullable)
            .of(safeType.items.buildValidType())
    }
}
