package com.aliucord.coreplugins.plugindownloader

import java.util.WeakHashMap

class ExtField(val c: Class<*>) {
    var map = WeakHashMap<Any, Any?>()
    fun set(instance: Any, value: Any?){
        if (c.isInstance(instance)) {
            map[instance] = value
        }
    }
    fun get(instance: Any): Any?{
       return if (c.isInstance(instance)) {
            map[instance]
        } else {
            null
        }
    }
}

fun Any.setExt(field: ExtField, value: Any?){
    field.set(this, value);
}
fun Any.getExt(field: ExtField): Any?{
    return field.get(this);
}