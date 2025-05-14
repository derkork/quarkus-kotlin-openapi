package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Feature
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.DependencyContainer

class DependencyContainerEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<DependencyContainer>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(dependencyContainer: DependencyContainer) {
        kotlinFile(dependencyContainer.name.asTypeName()) {
            registerImports(Library.All)

            kotlinClass(name) {
                kotlinAnnotation(Jakarta.ApplicationScoped)

                val context = object : DependencyContainerFeatureHandlerContext {
                    override fun addMember(member: KotlinMember) = this@kotlinClass.addMember(member)
                }

                dependencyContainer.features.forEach { feature ->
                    getHandler<DependencyContainerFeatureHandler, Unit> { context.installDependency(feature) }
                }
            }
        }
    }

}

interface DependencyContainerFeatureHandlerContext : MemberAware {

    fun installDefaultDependency(name: String, type: KotlinTypeReference) =
        kotlinMember(name, type, accessModifier = null)

}

interface DependencyContainerFeatureHandler : Handler {

    fun DependencyContainerFeatureHandlerContext.installDependency(feature: Feature): HandlerResult<Unit>

}
