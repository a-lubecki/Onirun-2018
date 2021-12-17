package com.onirun.model

/**
 * Created by Aurelien Lubecki
 * on 24/03/2018.
 */
enum class RaceTypeGroup(val tag: String) {

    ROAD("road"),
    NATURE("nature"),
    MULTI_SPORT("multi");

    companion object {

        fun findGroup(groupTag: String): RaceTypeGroup? {
            return values().find { it.tag == groupTag }
        }
    }

}