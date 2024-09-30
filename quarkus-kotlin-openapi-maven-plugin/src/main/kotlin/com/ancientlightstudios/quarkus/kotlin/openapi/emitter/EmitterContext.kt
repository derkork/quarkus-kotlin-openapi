package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import java.io.File
import kotlin.io.path.Path

class EmitterContext(val spec: TransformableSpec, val config: Config) {

    // get the last modified time of all input files
    private val inputLastModifiedDate: Long = config.sourceFiles
        .union(config.patchFiles).maxOfOrNull { File(it).lastModified() } ?: -1

    private val outputDirectory = Path(config.outputDirectory)

    fun getAdditionalImports() = config.additionalImports()

    fun <T : CodeEmitter> runEmitter(codeEmitter: T): T = codeEmitter.apply { emit() }

    var filesWritten: Long = 0
        private set

    var filesUpToDate: Long = 0
        private set

    fun KotlinFile.writeFile() {
        val packageName = fileName.packageName
        val targetPath = if (packageName.isNotBlank()) {
            val subPath = packageName.split(".")
            outputDirectory.resolve(Path(subPath.first(), *subPath.drop(1).toTypedArray()))
        } else {
            outputDirectory
        }

        targetPath.toFile().mkdirs()

        val outputFile = targetPath.resolve("${fileName.value}.kt").toFile()
        if (!config.forceOverwriteGeneratedFiles) {
            if (outputFile.exists() && outputFile.lastModified() > inputLastModifiedDate) {
                filesUpToDate++
                return
            }
        }

        check(outputFile.exists() || outputFile.createNewFile()) { "Could not create file $outputFile" }
        outputFile.bufferedWriter().use {
            render(CodeWriter(it))
        }
        filesWritten++
    }

}