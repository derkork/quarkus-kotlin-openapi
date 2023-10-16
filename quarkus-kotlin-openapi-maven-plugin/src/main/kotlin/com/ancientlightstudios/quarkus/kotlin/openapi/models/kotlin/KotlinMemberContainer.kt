package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.renderWithWrap
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinMemberContainer {

    private val members = mutableListOf<KotlinMember>()

    fun addMember(member: KotlinMember) {
        members.add(member)
    }

    val isNotEmpty: Boolean
        get() = members.isNotEmpty()

    fun render(writer: CodeWriter) = with(writer) {
        renderWithWrap(members, 2) { it.render(this) }
    }
}