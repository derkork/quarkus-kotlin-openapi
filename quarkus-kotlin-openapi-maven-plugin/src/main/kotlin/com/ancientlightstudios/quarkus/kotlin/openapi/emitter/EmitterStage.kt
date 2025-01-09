package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.io.path.Path

class EmitterStage(private val config: Config, private val handlerRegistry: HandlerRegistry) : GeneratorStage {

    private val log = LoggerFactory.getLogger(EmitterStage::class.java)

    override fun process(spec: OpenApiSpec) {
        val context = EmitterContext(spec, config, handlerRegistry)

        listOf(
            DependencyVogelEmitter(),
            ServerDelegateInterfaceEmitter(),
            ServerResponseInterfaceEmitter(),
            ServerRestControllerEmitter(),
            ServerRequestContextEmitter(),
            ServerRequestContainerEmitter(),
            EnumModelClassEmitter(),
            ObjectModelClassEmitter(),
            OneOfModelClassEmitter(),
        ).runEmitters(context)

        context.kotlinFiles.writeFiles()
    }

    private fun List<CodeEmitter>.runEmitters(context: EmitterContext) {
        forEach {
            it.apply { context.emit() }
        }
    }

    private fun List<KotlinFile>.writeFiles() {
        // get the last modified time of all input files
        val inputLastModifiedDate = config.sourceFiles
            .union(config.patchFiles)
            .maxOfOrNull { File(it).lastModified() } ?: -1

        var filesWritten = 0
        var filesUpToDate = 0

        this.forEach {
            if (!it.writeFile(inputLastModifiedDate)) {
                filesUpToDate++
            } else {
                filesWritten++
            }
        }

        log.info("(New) Generated ${filesWritten + filesUpToDate} files ($filesWritten new, $filesUpToDate up-to-date).")

    }

    private fun KotlinFile.writeFile(inputLastModifiedDate: Long): Boolean {
        val outputDirectory = Path(config.outputDirectory)
        val packageName = name.packageName
        val targetPath = if (packageName.isNotBlank()) {
            val subPath = packageName.split(".")
            outputDirectory.resolve(Path(subPath.first(), *subPath.drop(1).toTypedArray()))
        } else {
            outputDirectory
        }

        targetPath.toFile().mkdirs()

        val outputFile = targetPath.resolve("${name.name}.kt").toFile()
        if (!config.forceOverwriteGeneratedFiles) {
            if (outputFile.exists() && outputFile.lastModified() > inputLastModifiedDate) {
                return false
            }
        }

        check(outputFile.exists() || outputFile.createNewFile()) { "Could not create file $outputFile" }
        outputFile.bufferedWriter().use {
            render(CodeWriter(it))
        }
        return true
    }

}