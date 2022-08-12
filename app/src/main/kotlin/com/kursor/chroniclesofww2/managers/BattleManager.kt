package com.kursor.chroniclesofww2.managers

import com.kursor.chroniclesofww2.entities.Battle
import com.kursor.chroniclesofww2.features.*
import com.kursor.chroniclesofww2.features.BattleFeaturesMessages.NOT_CREATOR
import com.kursor.chroniclesofww2.features.BattleFeaturesMessages.NO_BATTLE_WITH_SUCH_ID
import com.kursor.chroniclesofww2.features.BattleFeaturesMessages.NO_SPACE_LEFT
import com.kursor.chroniclesofww2.features.BattleFeaturesMessages.SUCCESS
import com.kursor.chroniclesofww2.repositories.BattleRepository

class BattleManager(private val battleRepository: BattleRepository) {

    suspend fun getAllBattles(): List<Battle> {
        return battleRepository.getAllBattles()
    }

    suspend fun getBattlesOfUser(login: String): List<Battle> {
        return battleRepository.getBattlesOfUser(login)
    }

    suspend fun getBattleById(id: Int): Battle? = battleRepository.getBattleById(id)

    suspend fun saveBattle(login: String, battleReceiveDTO: SaveBattleReceiveDTO): SaveBattleResponseDTO {
        val id = findFreeId() ?: return SaveBattleResponseDTO(id = null, message = NO_SPACE_LEFT)
        battleRepository.saveBattle(
            Battle(
                id = id,
                loginOfCreator = battleReceiveDTO.loginOfCreator,
                name = battleReceiveDTO.name,
                description = battleReceiveDTO.description,
                dataJson = battleReceiveDTO.dataJson
            )
        )
        return SaveBattleResponseDTO(id = id, message = SUCCESS)
    }

    suspend fun editBattle(login: String, editBattleReceiveDTO: EditBattleReceiveDTO): EditBattleResponseDTO {
        if (!isCreator(login, editBattleReceiveDTO.id)) return EditBattleResponseDTO(message = NOT_CREATOR)
        val battle = battleRepository.getBattleById(editBattleReceiveDTO.id)
            ?: return EditBattleResponseDTO(message = NO_BATTLE_WITH_SUCH_ID)
        battleRepository.updateBattle(
            Battle(
                battle.id,
                battle.loginOfCreator,
                editBattleReceiveDTO.name,
                editBattleReceiveDTO.description,
                editBattleReceiveDTO.dataJson
            )
        )
        return EditBattleResponseDTO(message = SUCCESS)
    }


    suspend fun deleteBattle(login: String, deleteBattleReceiveDTO: DeleteBattleReceiveDTO): DeleteBattleResponseDTO {
        if (!isCreator(login, deleteBattleReceiveDTO.id)) return DeleteBattleResponseDTO(message = NOT_CREATOR)
        if (battleRepository.getBattleById(deleteBattleReceiveDTO.id) == null) {
            return DeleteBattleResponseDTO(message = NO_BATTLE_WITH_SUCH_ID)
        }
        battleRepository.deleteBattle(deleteBattleReceiveDTO.id)
        return DeleteBattleResponseDTO(message = SUCCESS)
    }

    private suspend fun isCreator(login: String, battleId: Int): Boolean {
        return getBattleById(battleId)?.loginOfCreator == login
    }

    private suspend fun findFreeId(): Int? {
        val start = battleRepository.getAllBattles().size + REMOTE_PREFIX
        var r = start
        var l = start
        while (l > REMOTE_PREFIX && r < 2 * REMOTE_PREFIX) {
            if (battleRepository.getBattleById(l) == null) return l
            if (battleRepository.getBattleById(r) == null) return r
            r++
            l--
        }
        while (l > REMOTE_PREFIX) {
            if (battleRepository.getBattleById(l) == null) return l
            l--
        }
        while (r < 2 * REMOTE_PREFIX) {
            if (battleRepository.getBattleById(r) == null) return r
            r++
        }
        return null
    }

    companion object {
        private const val REMOTE_PREFIX = 1_000_000_000
    }

}