package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Feature
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.DependencyVogel

class DependencyVogelEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<DependencyVogel>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(dependencyVogel: DependencyVogel) {
        kotlinFile(dependencyVogel.name.asTypeName()) {
            registerImports(Library.All)

            kotlinClass(name) {
                kotlinAnnotation(Jakarta.ApplicationScoped)

                val context = object : DependencyVogelFeatureHandlerContext {
                    override fun addMember(member: KotlinMember) = this@kotlinClass.addMember(member)
                }

                dependencyVogel.features.forEach { feature ->
                    getHandler<DependencyVogelFeatureHandler, Unit> { context.installDependency(feature) }
                }
            }
        }
    }

}

interface DependencyVogelFeatureHandlerContext : MemberAware {

    fun installDefaultDependency(name: String, type: KotlinTypeReference) =
        kotlinMember(name, type, accessModifier = null)

}

interface DependencyVogelFeatureHandler : Handler {

    fun DependencyVogelFeatureHandlerContext.installDependency(feature: Feature): HandlerResult<Unit>

}
