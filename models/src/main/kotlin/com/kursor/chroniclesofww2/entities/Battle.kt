package com.kursor.chroniclesofww2.entities

import kotlinx.serialization.Serializable


@Serializable
data class Battle(
    val id: Int,
    val loginOfCreator: String,
    val name: String,
    val description: String,
    val dataJson: String
)