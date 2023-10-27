package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import java.nio.file.Path
import kotlin.io.path.Path

class EmitterContext(private val packageName: String, private val outputDirectory: Path, val pathPrefix:String, val validatorPackage:String? = null) {

    fun serverPackage() = "$packageName.server"

    fun clientPackage() = "$packageName.client"
    
    fun modelPackage() = "$packageName.model"

    fun apiPackage() = "com.ancientlightstudios.quarkus.kotlin.openapi"

    fun generateFile(file: KotlinFile) {
        val targetPath = if (file.packageName.isNotBlank()) {
            val subPath = file.packageName.split(".")
            outputDirectory.resolve(Path(subPath.first(), *subPath.drop(1).toTypedArray()))
        } else {
            outputDirectory
        }

        targetPath.toFile().mkdirs()

        val outputFile = targetPath.resolve("${file.fileName.render()}.kt").toFile()
        check(outputFile.exists() || outputFile.createNewFile()) { "Could not create file $outputFile" }

        outputFile.bufferedWriter().use {
            file.render(CodeWriter(it))
        }
    }

}