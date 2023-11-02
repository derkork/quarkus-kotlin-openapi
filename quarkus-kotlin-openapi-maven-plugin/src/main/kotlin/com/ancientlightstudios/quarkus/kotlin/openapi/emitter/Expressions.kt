package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.InvocationExpression.Companion.invocationExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

fun VariableName.propertyToMaybeExpression(propertyName: String) = this.pathExpression()
    .then("asMaybe".methodName())
    .invocationExpression(propertyName.stringExpression(), "\${context}.$propertyName".stringExpression())

fun VariableName.parameterToMaybeExpression(context: Expression) = this.pathExpression()
    .then("asMaybe".methodName()).invocationExpression(context)
