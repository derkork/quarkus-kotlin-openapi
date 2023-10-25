package com.ancientlightstudios.quarkus.kotlin.openapi

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true)
class GenerateMojo : AbstractMojo() {
    /**
     * The current Maven project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    lateinit var project: MavenProject

    /**
     * The list of the source files.
     */
    @Parameter(required = true)
    lateinit var sources: List<String>

    /**
     * The list of JSON-Patch files to apply to the OpenAPI specification.
     */
    @Parameter
    var patches: List<String> = listOf()

    /**
     * Path where the debug output should be written.
     */
    @Parameter
    var debugOutputFile:String? = null

    /**
     * The package name of the generated classes.
     */
    @Parameter(defaultValue = "com.example.openapi")
    lateinit var packageName: String

    /**
     * The name of the interface to generate.
     */
    @Parameter(defaultValue = "ExampleInterface")
    lateinit var interfaceName: String

    /**
     * Path prefix to be prepended to generated endpoints.
     */
    @Parameter(defaultValue = "")
    var pathPrefix:String = ""

    /**
     * The directory where the generated sources should be put
     */
    @Parameter(defaultValue = "\${project.build.directory}/generated-sources/quarkus-kotlin-openapi")
    lateinit var outputDirectory: File

    @Parameter
    var endpoints: List<String> = listOf()

    @Parameter
    lateinit var interfaceType: InterfaceType

    override fun execute() {
        val config = Config(
            sources,
            patches,
            debugOutputFile,
            interfaceName,
            packageName,
            outputDirectory.path,
            pathPrefix,
            endpoints,
            interfaceType
        )

        val generator = Generator(config)
        generator.generate()

        project.addCompileSourceRoot(outputDirectory.path)
    }

}