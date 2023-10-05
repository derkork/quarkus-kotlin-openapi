package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import TransformerContext
import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.*
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

fun KotlinAnnotationContainer.addPath(path: String) = add("Path".className(), "value".variableName() to path)
fun KotlinAnnotationContainer.addParam(type: ParameterKind, name: String) =
    add("${type.name}_Param".className(), "value".variableName() to name)

fun jakartaRestImports() = listOf(
    "jakarta.ws.rs.GET",
    "jakarta.ws.rs.POST",
    "jakarta.ws.rs.PUT",
    "jakarta.ws.rs.DELETE",
    "jakarta.ws.rs.Path",
    "jakarta.ws.rs.PathParam",
    "jakarta.ws.rs.QueryParam",
    "jakarta.ws.rs.HeaderParam",
    "jakarta.ws.rs.CookieParam",
    "org.jboss.resteasy.reactive.RestResponse"
)

fun jacksonImports() = listOf(
    "com.fasterxml.jackson.databind.ObjectMapper",
    "com.fasterxml.jackson.annotation.JsonProperty"
)


fun modelImports(config: Config) = listOf("${config.packageName}.model.*")

fun libraryImports() = listOf("com.ancientlightstudios.quarkus.kotlin.openapi.*")

data class LoopStatus(val index: Int, val first: Boolean, val last: Boolean)

inline fun <T> Collection<T>.forEachWithStats(action: (status: LoopStatus, T) -> Unit) {
    val lastIndex = size - 1
    this.forEachIndexed { index, item -> action(LoopStatus(index, index == 0, index == lastIndex), item) }
}

inline fun <T> Array<T>.forEachWithStats(action: (status: LoopStatus, T) -> Unit) {
    val lastIndex = size - 1
    this.forEachIndexed { index, item -> action(LoopStatus(index, index == 0, index == lastIndex), item) }
}

fun <T> CodeWriter.renderParameterBlock(
    parameters: List<T>,
    maxSizeForSingleLine: Int = 1,
    block: CodeWriter.(T) -> Unit
) {
    // block to render the parameters. but will be called later
    val parameterBlock: CodeWriter.(Boolean) -> Unit = { newLine ->
        parameters.forEachWithStats { status, it ->
            block(it)
            if (!status.last) {
                write(", ", newLineAfter = newLine)
            }
        }
    }

    if (parameters.size > maxSizeForSingleLine) {
        indent(newLineBefore = true, newLineAfter = true) { parameterBlock(true) }
    } else {
        parameterBlock(false)
    }
}

fun SchemaRef.containerAsList(
    innerType: ClassName,
    innerNullable: Boolean = false,
    outerNullable: Boolean = false
): TypeName {
    val schema = this.resolve()
    if (schema !is Schema.ArraySchema) {
        // on top level, outerNullable decides whether the type is nullable
        return innerType.typeName(outerNullable)
    }

    // passing the innerNullable argument twice is important here, because nested Lists are treated as inner types as well
    return "List".rawTypeName(outerNullable).of(schema.items.containerAsList(innerType, innerNullable, innerNullable))
}

fun String.primitiveTypeClass() =
    when (this) {
        "string" -> "String"
        "password" -> "String"
        "integer" -> "Int"
        "int32" -> "Int"
        "int64" -> "Long"
        "float" -> "Float"
        "number" -> "Double"
        "boolean" -> "Boolean"
        else -> throw IllegalArgumentException("Unknown basic type: $this")
    }.rawClassName()


infix fun String.ifFalse(boolean: Boolean) = if (!boolean) this else ""


fun SchemaRef.getAllProperties(): List<SchemaProperty> {

    val result = mutableListOf<SchemaProperty>()

    val schema = this.resolve()
    if (schema is Schema.ObjectTypeSchema) {
        result.addAll(schema.properties)
    }

    if (schema is Schema.OneOfSchema) {
        schema.oneOf.forEach { result.addAll(it.getAllProperties()) }
    }

    if (schema is Schema.AnyOfSchema) {
        schema.anyOf.forEach { result.addAll(it.getAllProperties()) }
    }

    if (schema is Schema.AllOfSchema) {
        schema.allOf.forEach { result.addAll(it.getAllProperties()) }
    }

    return result
}

fun <T : KotlinStatement> T.addTo(method: KotlinMethod): T {
    method.addStatement(this)
    return this
}

fun KotlinMethod.addTransformStatement(
    parameterName: String,
    type: SchemaRef,
    validationInfo: ValidationInfo,
    parameterContext: Expression,
    context: TransformerContext,
    fromRequestBody: Boolean
): VariableName {
    val targetName = "maybe $parameterName".variableName()
    val sourceName = parameterName.variableName()

    val statement = when (val schema = type.resolve()) {
        is Schema.EnumSchema -> {
            val parameterType = context.safeModelFor(type).className()
            EnumToMaybeTransformStatement(
                targetName, sourceName, parameterContext, parameterType.typeName(), validationInfo
            )
        }

        is Schema.PrimitiveTypeSchema -> {
            val parameterType = context.safeModelFor(type).className()
            PrimitiveToMaybeTransformStatement(
                targetName, sourceName, parameterContext, parameterType.typeName(), schema.defaultValue, validationInfo
            )
        }

        is Schema.ArraySchema -> {
            if (fromRequestBody) {
                val parameterType = context.unsafeModelFor(type).className()
                val targetType = type.containerAsList(parameterType, true, false)
                CollectionBodyToMaybeTransformStatement(targetName, sourceName, targetType, parameterContext, validationInfo) {
                    nestedTransformStatement(it, schema.items, context)
                }
            } else {
                CollectionPropertyToMaybeTransformStatement(targetName, sourceName, parameterContext, validationInfo) {
                    nestedTransformStatement(it, schema.items, context)
                }
            }
        }

        else -> {
            val parameterType = context.unsafeModelFor(type).className()
            if (fromRequestBody) {
                ObjectBodyToMaybeTransformStatement(
                    targetName, sourceName, parameterContext, parameterType.typeName(), validationInfo
                )
            } else {
                ObjectPropertyToMaybeTransformStatement(
                    targetName, sourceName, parameterContext, parameterType.typeName(), validationInfo
                )
            }
        }
    }

    this.addStatement(statement)
    return targetName
}

private fun nestedTransformStatement(
    sourceName: VariableName,
    type: SchemaRef,
    context: TransformerContext
): KotlinStatement {
    // TODO: right now we don't have a way to detect list with nullable items
    val validationInfo = ValidationInfo(true)
    return when (val schema = type.resolve()) {
        is Schema.EnumSchema -> {
            val parameterType = context.safeModelFor(type).className()
            NestedEnumTransformStatement(sourceName, null, parameterType.typeName(), validationInfo)
        }

        is Schema.PrimitiveTypeSchema -> {
            val parameterType = context.safeModelFor(type).className()
            NestedPrimitiveTransformStatement(sourceName, null, parameterType.typeName(), null, validationInfo)
        }

        is Schema.ArraySchema -> {
            NestedCollectionTransformStatement(sourceName, validationInfo) {
                nestedTransformStatement(it, schema.items, context)
            }
        }

        else -> {
            val parameterType = context.unsafeModelFor(type).className()
            NestedObjectTransformStatement(sourceName, parameterType.typeName(), validationInfo)
        }
    }
}
