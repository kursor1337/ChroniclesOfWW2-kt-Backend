package com.kursor.chroniclesofww2.features

import com.kursor.chroniclesofww2.features.BattleFeaturesMessages.SUCCESS
import kotlinx.serialization.Serializable

@Serializable
data class SaveBattleReceiveDTO(
    val loginOfCreator: String,
    val name: String,
    val description: String,
    val dataJson: String
)

@Serializable
data class SaveBattleResponseDTO(
    val id: Int?,
    val message: String
)

@Serializable
data class EditBattleReceiveDTO(
    val id: Int,
    val name: String,
    val description: String,
    val dataJson: String
)

@Serializable
data class EditBattleResponseDTO(
    val message: String
)

@Serializable
data class DeleteBattleReceiveDTO(
    val id: Int
)

@Serializable
data class DeleteBattleResponseDTO(
    val message: String
)

object BattleFeaturesMessages {
    const val SUCCESS = "Success"
    const val NO_SPACE_LEFT = "No space left"
    const val NO_BATTLE_WITH_SUCH_ID = "No battle with such id"
}