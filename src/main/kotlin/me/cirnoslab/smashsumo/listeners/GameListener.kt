package me.cirnoslab.smashsumo.listeners

import me.cirnoslab.smashsumo.game.Game
import me.cirnoslab.smashsumo.game.GameManager
import me.cirnoslab.smashsumo.game.GamePlayer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Primary listener for game based events
 */
class GameListener : Listener {
    /**
     * Detects if player has exited arena.
     * Kills if in-game, teleports back if waiting.
     */
    @EventHandler
    fun onPlayerExitArenaBound(e: PlayerMoveEvent) {
        val gp = GameManager.getGamePlayer(e.player) ?: return

        if (gp.game.arena.inArena(e.to) || gp.respawnPoint != null) return
        when (gp.state) {
            GamePlayer.PlayerState.IN_GAME -> {
                gp.game.kill(gp)
            }
            GamePlayer.PlayerState.WAITING -> {
                e.player.teleport(
                    Location(gp.game.arena.center.world, gp.game.arena.center.x, gp.game.arena.center.y + 2.0, gp.game.arena.center.z),
                )
            }
            else -> return
        }
    }

    /**
     * Detects if a player leaves the server, and make the player leave the game too.
     */
    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        GameManager.leave(e.player)
    }

    /**
     * Detects if a player jumps from the respawn platform, and despawns it instantly if so.
     */
    @EventHandler
    fun onPlayerJumpFromRespawnPlatform(e: PlayerMoveEvent) {
        val gp = GameManager.getGamePlayer(e.player) ?: return
        if (gp.respawnPoint == null || gp.waitRespawn) return

        if (e.to.y > e.from.y && !(e.player as Entity).isOnGround) {
            gp.respawnPoint!!
                .world
                .getBlockAt(gp.respawnPoint)
                .type = Material.AIR
            gp.respawnPoint = null
        }
    }

    /**
     * Prevents a player from moving during countdown.
     */
    @EventHandler
    fun onPlayerCountdownMove(e: PlayerMoveEvent) {
        val gp = GameManager.getGamePlayer(e.player) ?: return

        if (gp.game.state != Game.GameState.COUNTDOWN) return
        e.to.x = e.from.x
        e.to.z = e.from.z
    }
}
