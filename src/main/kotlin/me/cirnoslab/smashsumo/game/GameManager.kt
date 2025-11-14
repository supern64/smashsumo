package me.cirnoslab.smashsumo.game

import me.cirnoslab.smashsumo.Config
import me.cirnoslab.smashsumo.arena.Arena
import org.bukkit.entity.Player

object GameManager {
    var games = mutableListOf<Game>()

    fun join(
        p: Player,
        arena: Arena,
    ): GameJoinResult {
        if (isPlayerInGame(p)) return GameJoinResult.ALREADY_IN_GAME

        val game = getGame(arena) ?: initGame(arena)
        arena.state = Arena.ArenaState.WAITING
        return game.join(p)
    }

    fun leave(p: Player): GameLeaveResult {
        if (!isPlayerInGame(p)) return GameLeaveResult.NOT_IN_GAME
        val game = getGame(p)!!
        val result = game.leave(p)

        if (result == GameLeaveResult.SUCCESS && game.gamePlayers.isEmpty()) {
            games.remove(game)
            game.arena.state = Arena.ArenaState.AVAILABLE
        }
        return result
    }

    fun initGame(arena: Arena): Game {
        val game =
            Game(
                arena,
                GameSettings(
                    Config.Game.lives,
                    Config.Game.allowBlock,
                    Config.Game.respawnTime,
                    Config.Game.platformDespawnTime,
                    Config.Game.knockback,
                ),
            )
        games.add(game)
        return game
    }

    // only called by game itself
    fun removeGame(game: Game) {
        games.remove(game)
    }

    fun getGame(player: Player): Game? {
        for (game in games) {
            if (game.gamePlayers.containsKey(player.uniqueId)) return game
        }
        return null
    }

    fun getGame(arena: Arena): Game? {
        for (game in games) {
            if (game.arena.name == arena.name) return game
        }
        return null
    }

    fun isPlayerInGame(player: Player): Boolean = getGame(player) != null

    fun getGamePlayer(player: Player): GamePlayer? {
        val game = getGame(player) ?: return null
        return game.gamePlayers[player.uniqueId]
    }

    enum class GameJoinResult {
        SUCCESS,
        ALREADY_IN_GAME,
        GAME_STARTED,
        GAME_ENDING,
    }

    enum class GameLeaveResult {
        SUCCESS,
        NOT_IN_GAME,
    }
}
