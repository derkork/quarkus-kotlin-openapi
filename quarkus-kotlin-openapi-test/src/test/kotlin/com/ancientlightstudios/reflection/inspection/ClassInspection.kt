package com.ancientlightstudios.reflection.inspection

import kotlin.reflect.KClass

class ClassInspection<T : Any>(val clazz: KClass<T>) {

}

fun <T : Any> KClass<T>.inspect(block: ClassInspection<T>.() -> Unit) = ClassInspection(this).block()


