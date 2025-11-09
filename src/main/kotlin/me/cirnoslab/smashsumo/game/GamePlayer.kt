package me.cirnoslab.smashsumo.game

import fr.mrmicky.fastboard.adventure.FastBoard
import me.cirnoslab.smashsumo.SmashSumo
import me.cirnoslab.smashsumo.SmashSumo.Companion.P
import me.cirnoslab.smashsumo.SmashSumo.Companion.S
import me.cirnoslab.smashsumo.SmashSumo.Companion.SCOREBOARD_LINE
import me.cirnoslab.smashsumo.Utils
import me.cirnoslab.smashsumo.Utils.mm
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.sqrt

class GamePlayer(
    val game: Game,
    val player: Player,
    var playerNumber: Int? = null,
    var lives: Int,
) {
    var state: PlayerState = PlayerState.WAITING
    var damage: Double = 0.0
    var speedSquared: Double = 0.0 // measured from PlayerMoveEvent
    var board = FastBoard(player)
    var respawnPoint: Location? = null
    var waitRespawn = false
    var jumpPhase = 0 // jump 1, jump 2, wait for ground

    // last own attack on others
    var lastHitMomentum = 0.0
    var lastHitVerticalMultiplier = 0.0

    val speed: Double
        get() = sqrt(speedSquared)

    val displayHealth: Double
        get() = Utils.ntrc(damage, 0.0, 125.0, 20.0, 1.0)

    val lifeComponent: Component
        get() = text("⬤".repeat(lives), color).append(text("⬤".repeat(SmashSumo.MAX_LIVES - lives), GRAY))

    val damageColor: TextColor
        get() = "<transition:white:yellow:red:dark_red:${Utils.ntrc(damage, 0.0, 125.0, 0.0, 1.0)}>".mm().color()!!

    val damageComponent: Component
        get() = text("%.1f%%".format(damage), damageColor)

    // HUDs are here so they can be modified per player
    val actionBarDisplay: Component
        get() {
            return when (state) {
                PlayerState.SPECTATING -> "${P}Currently spectating".mm()
                PlayerState.WAITING -> "${P}Waiting for players... (${S}${game.gamePlayers.count()}$P)".mm()
                PlayerState.IN_GAME ->
                    text("P$playerNumber", color)
                        .append(" $P| ".mm())
                        .append(damageComponent)
                        .append(" $P| Lives: ${S}$lives".mm())
                PlayerState.ENDING -> "${P}Waiting to teleport back...".mm()
                else -> "You should not see this.".mm()
            }
        }

    val scoreboardDisplay: List<Component>
        get() {
            return when (state) {
                PlayerState.WAITING ->
                    listOf(
                        SCOREBOARD_LINE,
                        text("Waiting for players:"),
                        "${S}${game.gamePlayers.size} <white>currently waiting".mm(),
                        SCOREBOARD_LINE,
                    )
                PlayerState.ENDING ->
                    listOf(
                        SCOREBOARD_LINE,
                        text("Game ended."),
                        SCOREBOARD_LINE,
                    )

                PlayerState.GHOST ->
                    listOf()
                else -> {
                    val board =
                        mutableListOf(
                            SCOREBOARD_LINE,
                            text("Duration: ").append(game.formattedTime),
                        )
                    for (gp in game.startingPlayers!!) {
                        if (gp.playerNumber == null) continue // joined as spectator, should not be possible
                        if (board.size >= 14) break // 12 players max
                        if (gp.state != PlayerState.IN_GAME) {
                            board.add(text("  P${gp.playerNumber}", gp.color).append(text(" ELIMINATED", RED)))
                        } else {
                            board.add(
                                text("  P${gp.playerNumber} ", gp.color)
                                    .append(gp.damageComponent)
                                    .appendSpace()
                                    .append(gp.lifeComponent),
                            )
                        }
                    }
                    board.add(SCOREBOARD_LINE)
                    return board
                }
            }
        }

    val color: NamedTextColor
        get() = if (playerNumber != null) SmashSumo.playerColor[(playerNumber!! - 1) % 8] else GRAY

    enum class PlayerState {
        WAITING,
        IN_GAME,
        SPECTATING,
        GHOST, // for players who left midgame
        ENDING,
    }
}
