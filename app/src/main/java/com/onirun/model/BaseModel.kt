package com.onirun.model

/**
 * Created by Aurelien Lubecki
 * on 18/03/2018.
 */
abstract class BaseModel<out T>(val id: T) {

    override fun equals(other: Any?): Boolean {

        if (this === other) {
            return true
        }

        if (javaClass != other?.javaClass) {
            return false
        }

        other as BaseModel<*>

        if (id != other.id) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

}

