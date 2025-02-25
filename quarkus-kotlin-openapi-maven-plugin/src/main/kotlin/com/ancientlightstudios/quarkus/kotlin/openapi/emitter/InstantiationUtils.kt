package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property

/**
 * creates an instantiation expression for the given container class.
 * If the list of parameters is empty or only contains [PlainParameter], generates this
 * `Maybe.Success(<context>, <ContainerClassName>([parameters, ...]))`
 *
 * If there is at least one [MaybeParameter] generates this
 * ```
 * maybeAllOf(<context>, <maybeParameters ...>) {
 *     <ContainerClassName>(<parameters, ...>)
 * }
 * ```
 *
 * The list of parameters in the ctor contains this expression for every [MaybeParameter]
 * `(<maybeParameter> as Maybe.Success).value`
 */
fun allToObject(
    context: KotlinExpression, containerClassName: KotlinTypeName, parameter: List<InstantiationParameter>
): KotlinExpression {
    val constructorValues = parameter.map {
        when (it) {
            is MaybeParameter -> it.name.identifier()
                .cast(Library.MaybeSuccess.asTypeReference())
                .property("value")

            is PlainParameter -> it.name.identifier()
        }
    }

    val maybeParameters = parameter.filterIsInstance<MaybeParameter>()
        .map { it.name.identifier() }

    val resultStatement = invoke(containerClassName.identifier(), *constructorValues.toTypedArray())
    return if (maybeParameters.isNotEmpty()) {
        invoke("maybeAllOf", context, *maybeParameters.toTypedArray()) {
            resultStatement.statement()
        }
    } else {
        invoke(Library.MaybeSuccess.identifier(), context, resultStatement)
    }
}

sealed interface InstantiationParameter
data class MaybeParameter(val name: String) : InstantiationParameter
data class PlainParameter(val name: String) : InstantiationParameter