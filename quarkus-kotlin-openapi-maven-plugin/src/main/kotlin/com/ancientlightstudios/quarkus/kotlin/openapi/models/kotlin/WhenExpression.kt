package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class WhenExpression(private val expression: KotlinExpression?) : KotlinExpression, WhenOptionAware {

    private val options = KotlinRenderableBlockContainer<WhenOption>(false)

    override fun addOption(option: WhenOption) {
        options.addItem(option)
    }

    override fun ImportCollector.registerImports() {
        expression?.let { registerFrom(it) }
        registerFrom(options)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("when ")
        if (expression != null) {
            write("(")
            expression.render(this)
            write(") ")
        }

        block {
            options.render(this)
        }
    }

    companion object {

        fun `when`(expression: KotlinExpression? = null, block: WhenExpression.() -> Unit) =
            WhenExpression(expression).apply(block)
    }

}
