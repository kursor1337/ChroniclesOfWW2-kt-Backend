package com.kursor.chroniclesofww2.entities

import com.kursor.chroniclesofww2.model.serializable.Battle
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


@Serializable
data class BattleDatabaseEntity(
    val id: Int,
    val loginOfCreator: String,
    val name: String,
    val description: String,
    val dataJson: String
) {
    fun toBattle(): Battle {
        return Battle(
            id = id,
            name = name,
            description = description,
            data = Json.decodeFromString(dataJson)
        )
    }
}