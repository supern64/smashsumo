package me.cirnoslab.smashsumo.game

import fr.mrmicky.fastboard.FastBoard
import me.cirnoslab.smashsumo.Config.Style.P
import me.cirnoslab.smashsumo.Config.Style.S
import me.cirnoslab.smashsumo.Config.Style.teamColors
import me.cirnoslab.smashsumo.SmashSumo.Companion.SCOREBOARD_LINE
import me.cirnoslab.smashsumo.Utils
import org.bukkit.ChatColor.DARK_RED
import org.bukkit.ChatColor.GRAY
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.WHITE
import org.bukkit.ChatColor.YELLOW
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.sqrt

/**
 * Represents the game state of a player in a [Game]
 *
 * @property game the Game this GamePlayer is for
 * @property player the Player this GamePlayer is for
 * @property playerNumber the player order (see [Game.initStart])
 * @property lives the number of lives that this player has
 */
class GamePlayer(
    val game: Game,
    val player: Player,
    var playerNumber: Int? = null,
    var lives: Int,
) {
    /**
     * The current state of this player
     *
     * @see PlayerState
     */
    var state: PlayerState = PlayerState.WAITING

    /**
     * The current damage percentage of this player
     */
    var damage: Double = 0.0

    /**
     * The current speed squared of this player, measured from [org.bukkit.event.player.PlayerMoveEvent]
     *
     * @see me.cirnoslab.smashsumo.listeners.PlayerMechanicListener.onPlayerHit
     */
    var speedSquared: Double = 0.0

    /**
     * The [FastBoard] instance used by this player
     */
    var board = FastBoard(player)

    /**
     * The [Location] this player will respawn after death
     *
     * @see Game.kill
     */
    var respawnPoint: Location? = null

    /**
     * Whether this player is still waiting to be respawned
     *
     * @see Game.kill
     */
    var waitRespawn = false

    /**
     * The current double jump phase
     *
     * @see me.cirnoslab.smashsumo.listeners.PlayerMechanicListener.onPlayerFly
     */
    var jumpPhase = 0

    /**
     * The current speed of this player, measured from [org.bukkit.event.player.PlayerMoveEvent]
     *
     * @see me.cirnoslab.smashsumo.listeners.PlayerMechanicListener.onPlayerHit
     */
    val speed: Double
        get() = sqrt(speedSquared)

    /**
     * The current health mapped from [damage]
     */
    val displayHealth: Double
        get() = Utils.ntrc(damage, 0.0, 125.0, 20.0, 1.0)

    /**
     * The display string for life count
     */
    val lifeString: String
        get() = "${color}${"⬤".repeat(lives)}${GRAY}${"⬤".repeat(game.settings.lives - lives)}"

    // HUDs are here so they can be modified per player

    /**
     * The display string for the player's action bar
     *
     * @see HUDManager
     */
    val actionBarDisplay: String
        get() {
            return when (state) {
                PlayerState.SPECTATING -> "${P}Currently spectating"
                PlayerState.WAITING -> "${P}Waiting for players... (${S}${game.gamePlayers.count()}$P)"
                PlayerState.IN_GAME -> "${color}P$playerNumber $P| ${damageColor}${"%.1f".format(damage)}%$P | Lives: ${S}$lives"
                PlayerState.ENDING -> "${P}Waiting to teleport back..."
                else -> "You should not see this."
            }
        }

    /**
     * The display list for the player's scoreboard
     *
     * @see HUDManager
     */
    val scoreboardDisplay: List<String>
        get() {
            return when (state) {
                PlayerState.WAITING ->
                    listOf(
                        SCOREBOARD_LINE,
                        "Waiting for players:",
                        "${S}${game.gamePlayers.size} ${WHITE}currently waiting",
                        SCOREBOARD_LINE,
                    )
                PlayerState.ENDING ->
                    listOf(
                        SCOREBOARD_LINE,
                        "Game ended.",
                        SCOREBOARD_LINE,
                    )

                PlayerState.GHOST ->
                    listOf()
                else -> {
                    val board =
                        mutableListOf(
                            SCOREBOARD_LINE,
                            "Duration: ${game.formattedTime}",
                        )
                    for (gp in game.startingPlayers!!) {
                        if (gp.playerNumber == null) continue // joined as spectator, should not be possible
                        if (board.size >= 14) break // 12 players max
                        if (gp.state != PlayerState.IN_GAME) {
                            board.add("  ${gp.color}P${gp.playerNumber} ${RED}ELIMINATED")
                        } else {
                            board.add("  ${gp.color}P${gp.playerNumber} ${gp.damageColor}${"%.1f".format(gp.damage)}% ${gp.lifeString}")
                        }
                    }
                    board.add(SCOREBOARD_LINE)
                    return board
                }
            }
        }

    /**
     * The main color of the player (based on [playerNumber])
     */
    val color
        get() = if (playerNumber != null) teamColors[(playerNumber!! - 1) % teamColors.size] else GRAY

    /**
     * The damage color used
     */
    val damageColor
        get() =
            when (damage) {
                in 0.0..35.0 -> S
                in 35.001..65.0 -> YELLOW
                in 65.001..100.0 -> RED
                else -> DARK_RED
            }

    /**
     * Represents player state
     */
    enum class PlayerState {
        WAITING,
        IN_GAME,
        SPECTATING,

        /**
         * Does not exist (left midgame)
         */
        GHOST,
        ENDING,
    }
}
