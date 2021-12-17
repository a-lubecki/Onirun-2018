package com.onirun.test

import com.onirun.model.bundle.IParsableBundle
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

class FieldsSummary(bundleClass: Class<IParsableBundle>) {

    private val fields = bundleClass.declaredFields

    constructor(bundle: IParsableBundle) : this(bundle.javaClass)

    fun has(name: String): Boolean {
        return fields.any {
            it.name == name
        }
    }

    fun fieldNames(): List<String> {

        return fields.map {
            it.name
        }
    }

    fun getObjectArrayChildrenNames(): List<String> {

        return fields.filter {
            it.type.isArray ||
                    (List::class.java.isAssignableFrom(it.type) && IParsableBundle::class.java.isAssignableFrom((it.genericType as ParameterizedType).actualTypeArguments[0] as Class<*>))
        }.map {
            it.name
        }
    }

    fun getObjectChildrenNames(): List<String> {

        return fields.filter {
            IParsableBundle::class.java.isAssignableFrom(it.type)
        }.map {
            it.name
        }
    }

    private fun findChildField(name: String): Field? {

        return fields.firstOrNull {
            it.name == name
        }
    }

    fun isArray(name: String): Boolean {

        val f = findChildField(name)
        val fType = f?.type ?: return false

        return fType.isArray || List::class.java.isAssignableFrom(fType)
    }

    fun getChildSummary(name: String): FieldsSummary? {

        val f = findChildField(name)
        val fType = f?.type ?: return null

        //object
        if (IParsableBundle::class.java.isAssignableFrom(fType)) {
            return FieldsSummary(fType as Class<IParsableBundle>)
        }

        if (fType.isArray) {
            return FieldsSummary(fType.componentType as Class<IParsableBundle>)
        }

        if (List::class.java.isAssignableFrom(fType)) {

            val subType = (f.genericType as ParameterizedType).actualTypeArguments[0] as Class<*>
            if (IParsableBundle::class.java.isAssignableFrom(subType)) {
                return FieldsSummary(subType as Class<IParsableBundle>)
            }
        }

        return null
    }

}