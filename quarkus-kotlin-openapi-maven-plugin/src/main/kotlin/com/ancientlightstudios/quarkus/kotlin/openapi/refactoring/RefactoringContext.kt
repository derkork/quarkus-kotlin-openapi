package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

class RefactoringContext(val spec: TransformableSpec, private val config: Config) {

    val packageName = config.packageName

    val serverPackage = "$packageName.server"

    val clientPackage = "$packageName.client"

    val modelPackage = "$packageName.model"

    val apiPackage = "com.ancientlightstudios.quarkus.kotlin.openapi"

    fun performRefactoring(refactoring: SpecRefactoring) {
        refactoring.apply { perform() }
    }


}