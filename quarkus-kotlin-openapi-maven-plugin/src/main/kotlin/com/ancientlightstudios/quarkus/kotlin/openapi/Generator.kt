package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterStage
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.form.FormDependencyHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.form.FormModelTransformationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.form.FormUpstreamHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.json.JsonDependencyHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.json.JsonDownstreamHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.json.JsonModelTransformationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.json.JsonUpstreamHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet.OctetDependencyHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet.OctetDownstreamHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet.OctetModelTransformationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet.OctetUpstreamHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.plain.PlainDependencyHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.plain.PlainDownstreamHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.plain.PlainModelTransformationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.plain.PlainUpstreamHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.ParserStage
import com.ancientlightstudios.quarkus.kotlin.openapi.patching.PatchingStage
import com.ancientlightstudios.quarkus.kotlin.openapi.refactoring.RefactoringStage
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.TransformationStage
import com.ancientlightstudios.quarkus.kotlin.openapi.validation.ValidationStage
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

class Generator(private val config: Config) {

    private val log = LoggerFactory.getLogger(Generator::class.java)

    fun generate() {
        val start = Instant.now()

        val handlerRegistry = HandlerRegistry(
            // TODO: get list via ServiceLoader to support plugins
            listOf(
                FormDependencyHandler(),
                // there is no FormDownstreamHandler, because this content type only makes sense in requests
                FormModelTransformationHandler(),
                FormUpstreamHandler(),

                JsonDependencyHandler(),
                JsonDownstreamHandler(),
                JsonModelTransformationHandler(),
                JsonUpstreamHandler(),

                OctetDependencyHandler(),
                OctetDownstreamHandler(),
                OctetModelTransformationHandler(),
                OctetUpstreamHandler(),

                PlainDependencyHandler(),
                PlainDownstreamHandler(),
                PlainModelTransformationHandler(),
                PlainUpstreamHandler(),
            )
        )

        val json = PatchingStage(config).process()
        val spec = OpenApiSpec()
        ParserStage(config, json).process(spec)
        ValidationStage().process(spec)
        RefactoringStage(config).process(spec)
        TransformationStage(config, handlerRegistry).process(spec)
        EmitterStage(config, handlerRegistry).process(spec)

        val duration = Duration.between(start, Instant.now())

        log.info("Plugin took ${duration.prettyPrint()}")
    }

    private fun Duration.prettyPrint(): String {
        val hours = toHoursPart()
        val minutes = toMinutesPart()
        val seconds = toSecondsPart()
        val milliseconds = toMillisPart()

        val result = StringBuilder()
        if (hours > 0) {
            result.append("${hours}h")
        }
        if (minutes > 0) {
            result.append("${minutes}m")
        }
        if (seconds > 0) {
            result.append("${seconds}s")
        }
        // ignore milliseconds if there are minutes or hours
        if (hours == 0 && minutes == 0 && milliseconds > 0) {
            result.append("${milliseconds}ms")
        }

        return result.toString()
    }
}
