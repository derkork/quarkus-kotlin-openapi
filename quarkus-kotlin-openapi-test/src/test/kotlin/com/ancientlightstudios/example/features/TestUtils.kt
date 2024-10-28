package com.ancientlightstudios.example.features

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ObjectEnumerableAssert

fun <SELF : ObjectEnumerableAssert<SELF, String>> ObjectEnumerableAssert<SELF, String>.containsExactly(
    vararg itemParts: List<String>
): SELF {
    var result = this.hasSize(itemParts.size)
    itemParts.forEach { parts ->
        result = result.anySatisfy {
            assertThat(it).contains(parts)
        }
    }
    return result
}

fun Char.repeat(count: Int) = "".padStart(count, this)

fun <T> T.repeatAsItem(count: Int) = List(count) { this }

fun <T> Pair<String, T>.repeatAsMap(count: Int) = List(count) { "$first:$it" to second }.toMap()