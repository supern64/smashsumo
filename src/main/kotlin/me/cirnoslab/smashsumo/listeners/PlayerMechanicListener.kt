package me.cirnoslab.smashsumo.listeners

import me.cirnoslab.smashsumo.game.Game
import me.cirnoslab.smashsumo.game.GameManager
import me.cirnoslab.smashsumo.game.GamePlayer
import me.cirnoslab.smashsumo.game.GameSettings
import me.cirnoslab.smashsumo.game.HitValue
import org.bukkit.GameMode
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleFlightEvent

/**
 * Primary listener to implement player mechanics
 */
class PlayerMechanicListener : Listener {
    /**
     * Calculates player speed.
     *
     * @see GamePlayer.speed
     * @see GamePlayer.speedSquared
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerMove(e: PlayerMoveEvent) {
        val gp = GameManager.getGamePlayer(e.player) ?: return
        gp.speedSquared = e.to.distanceSquared(e.from)

        // bonus double jump reset
        if ((e.player as Entity).isOnGround && gp.game.state != Game.GameState.COUNTDOWN) {
            gp.resetJump()
        }
    }

    /**
     * Applies knockback and damage accordingly and updates the displays.
     *
     * @see me.cirnoslab.smashsumo.game.KnockbackConfig
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerHit(e: EntityDamageByEntityEvent) {
        if (e.entity !is Player) return
        val d = e.entity as Player
        val dGP = GameManager.getGamePlayer(d) ?: return

        if (dGP.respawnPoint != null || dGP.state != GamePlayer.PlayerState.IN_GAME) { // defender respawning or not in game
            e.isCancelled = true
            return
        }

        var fhv: HitValue?

        when (e.damager) {
            is Player -> {
                val a = e.damager as Player
                val aGP = GameManager.getGamePlayer(a)

                if (aGP == null) { // attacker outside of game
                    e.isCancelled = true
                    return
                }

                val game = aGP.game

                if (game.arena.name != dGP.game.arena.name) return // defender and attacker not in same game
                if (aGP.state != GamePlayer.PlayerState.IN_GAME) { // attacker not in game
                    e.isCancelled = true
                    return
                }

                if (aGP.respawnPoint != null) { // attacker is respawning
                    e.isCancelled = true
                    return
                }

                if (d.noDamageTicks > 0) { // defender has NDT
                    e.isCancelled = true
                    return
                }

                fhv = dGP.hit(aGP, dEvent = e)
            }

            is Projectile -> {
                val p = e.damager as Projectile

                if (p.shooter !is Player) { // shot by something random (skeleton lol)
                    e.isCancelled = true
                    return
                }

                if (d == p.shooter) {
                    e.isCancelled = true
                    return
                }

                val a = p.shooter as Player
                val aGP = GameManager.getGamePlayer(a)

                if (aGP == null) { // attacker outside of game
                    e.isCancelled = true
                    return
                }

                val game = aGP.game

                if (game.arena.name != dGP.game.arena.name) return // defender and attacker not in same game
                if (aGP.state != GamePlayer.PlayerState.IN_GAME) { // attacker not in game
                    e.isCancelled = true
                    return
                }

                if (aGP.respawnPoint != null) { // attacker is respawning
                    e.isCancelled = true
                    return
                }

                fhv = dGP.hit(p, aGP, dEvent = e)

                if (p.type != EntityType.FISHING_HOOK) p.remove()
            }

            else -> {
                return
            }
        }

        e.isCancelled = true
        d.damage(0.0)
        d.noDamageTicks = fhv.noDamageTicks
    }

    /**
     * Prevents players from automatically healing
     * (disrupts damage display)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerHeal(e: EntityRegainHealthEvent) {
        if (e.entity !is Player) return
        val player = e.entity as Player
        if (!GameManager.isPlayerInGame(player)) return

        e.isCancelled = true
    }

    /**
     * Prevents players from losing hunger
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerLoseHunger(e: FoodLevelChangeEvent) {
        if (e.entity !is Player) return
        val player = e.entity as Player
        if (!GameManager.isPlayerInGame(player)) return

        e.foodLevel = 20
    }

    /**
     * Prevents players from fall damage
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerFall(e: EntityDamageEvent) {
        if (e.entity !is Player) return
        if (e.cause != EntityDamageEvent.DamageCause.FALL) return
        val player = e.entity as Player
        if (!GameManager.isPlayerInGame(player)) return

        e.isCancelled = true
    }

    /**
     * Prevents players from placing blocks
     *
     * @see GameSettings.allowBlock
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerPlaceBlock(e: BlockPlaceEvent) {
        val game = GameManager.getGame(e.player) ?: return
        if (game.settings.allowBlock) return
        e.isCancelled = true
    }

    /**
     * Prevents players from breaking blocks
     *
     * @see GameSettings.allowBlock
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerBreakBlock(e: BlockBreakEvent) {
        val game = GameManager.getGame(e.player) ?: return
        if (game.settings.allowBlock) return
        e.isCancelled = true
    }

    /**
     * Handles triple jumping
     *
     * @see GamePlayer.jumpPhase
     */
    @EventHandler
    fun onPlayerFly(e: PlayerToggleFlightEvent) {
        val gp = GameManager.getGamePlayer(e.player) ?: return
        if (gp.state == GamePlayer.PlayerState.SPECTATING || gp.game.state == Game.GameState.COUNTDOWN) return
        if (gp.player.gameMode == GameMode.CREATIVE || gp.player.gameMode == GameMode.SPECTATOR) return

        e.isCancelled = true
        gp.jump()
    }
}
