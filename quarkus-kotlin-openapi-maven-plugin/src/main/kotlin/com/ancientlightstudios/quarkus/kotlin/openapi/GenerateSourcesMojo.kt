package com.ancientlightstudios.quarkus.kotlin.openapi

import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true)
class GenerateSourcesMojo : GenerateMojo() {

    /**
     * The directory where the generated sources should be put
     */
    @Parameter(defaultValue = "\${project.build.directory}/generated-sources/quarkus-kotlin-openapi")
    override lateinit var outputDirectory: File

    override fun registerSourceRoot() {
        project.addCompileSourceRoot(outputDirectory.path)
    }

}