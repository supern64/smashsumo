package me.cirnoslab.smashsumo.game

import me.cirnoslab.smashsumo.Config
import me.cirnoslab.smashsumo.arena.Arena
import org.bukkit.entity.Player

/**
 * Game manager singleton
 */
object GameManager {
    /**
     * A list of [Game]s that are currently active
     */
    var games = mutableListOf<Game>()

    /**
     * Join a player into an arena.
     * If no game exists for that arena, one will be created.
     *
     * @param p the Player to join
     * @param arena the Arena to join the player into
     * @return the join result
     */
    fun join(
        p: Player,
        arena: Arena,
    ): GameJoinResult {
        if (isPlayerInGame(p)) return GameJoinResult.ALREADY_IN_GAME

        val game = getGame(arena) ?: initGame(arena)
        arena.state = Arena.ArenaState.WAITING
        return game.join(p)
    }

    /**
     * Makes a player leave an arena.
     * If no one is left, the Game will be destroyed and removed.
     *
     * @param p the Player to leave
     * @return the leave result
     */
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

    /**
     * Initializes a game for [arena] with the settings from the configuration and adds it to the list.
     *
     * @param arena the Arena to create a Game for
     * @return the game
     */
    fun initGame(arena: Arena): Game {
        val game =
            Game(
                arena,
                GameSettings(
                    Config.Game.lives,
                    Config.Game.allowBlock,
                    Config.Game.respawnTime,
                    Config.Game.platformDespawnTime,
                    Config.Game.itemDespawnTime,
                    Config.Game.knockback,
                    Config.Game.defaultKit,
                ),
            )
        games.add(game)
        return game
    }

    /**
     * Removes a game. Only called by Game itself.
     *
     * @param game the Game to remove
     */
    fun removeGame(game: Game) {
        games.remove(game)
    }

    /**
     * Gets a game if [player] is in it.
     *
     * @param player the player to search
     * @return game of player if exists, otherwise null
     */
    fun getGame(player: Player): Game? {
        for (game in games) {
            if (game.gamePlayers.containsKey(player.uniqueId)) return game
        }
        return null
    }

    /**
     * Gets the game if [arena] is hosting one.
     *
     * @param arena the arena to search
     * @return game of arena if exists, otherwise null
     */
    fun getGame(arena: Arena): Game? {
        for (game in games) {
            if (game.arena.name == arena.name) return game
        }
        return null
    }

    /**
     * Gets if [player] is in a game.
     *
     * @param player the player to search
     * @return whether the player is in a game
     */
    fun isPlayerInGame(player: Player): Boolean = getGame(player) != null

    /**
     * Gets a [GamePlayer] of a [Player] if they are in a game.
     *
     * @param player the player to search
     * @return the GamePlayer if in game, otherwise null
     */
    fun getGamePlayer(player: Player): GamePlayer? {
        val game = getGame(player) ?: return null
        return game.gamePlayers[player.uniqueId]
    }

    /**
     * Represents the result of joining a game
     */
    enum class GameJoinResult {
        SUCCESS,
        ALREADY_IN_GAME,

        /**
         * The game the player is trying to join has already started (joined as spectator)
         */
        GAME_STARTED,
        GAME_ENDING,
    }

    enum class GameLeaveResult {
        SUCCESS,
        NOT_IN_GAME,
    }
}
