package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.AllOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.replaceWith
import org.slf4j.LoggerFactory

class ReplaceAllOfComponentsRefactoring : SpecRefactoring {

    private val log = LoggerFactory.getLogger(ReplaceAllOfComponentsRefactoring::class.java)

    override fun RefactoringContext.perform() {
        // only schemas with an allOf component
        var remainingSchemas = spec.schemas.filter { it.hasComponent<AllOfComponent>() }

        do {
            val (ready, notReady) = remainingSchemas.partition(::isSchemaReady)
            remainingSchemas = notReady

            if (ready.isEmpty()) {
                // there are no more schemas to process
                break
            }

            ready.forEach(::processSchema)
        } while (true)

        if (remainingSchemas.isNotEmpty()) {
            log.warn("AllOf component for the following schemas can't be inlined")
            remainingSchemas.forEach {
                log.warn("- ${it.originPath}")
            }

            ProbableBug("Can't inline allOf component for some schemas")
        }
    }

    private fun isSchemaReady(schema: OpenApiSchema): Boolean {
        // schema is ready if schemas inside the allOf component don't have nested allOf components
        return schema.components
            .filterIsInstance<AllOfComponent>()
            .flatMap { it.options }
            .none { it.schema.hasComponent<AllOfComponent>() }
    }

    private fun processSchema(schema: OpenApiSchema) {
        schema.components
            .filterIsInstance<AllOfComponent>()
            .forEach { component ->
                val allNestedComponents = component.options.flatMap { it.schema.components }

                // replace every allOfComponent with  the components of its nested schemas
                schema.components = schema.components.replaceWith(component, *allNestedComponents.toTypedArray())
            }
    }

}

