package me.cirnoslab.smashsumo.listeners

import io.github.theluca98.textapi.ActionBar
import me.cirnoslab.smashsumo.game.Game
import me.cirnoslab.smashsumo.game.GameManager
import me.cirnoslab.smashsumo.game.GamePlayer
import org.bukkit.GameMode
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.util.Vector
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.random.Random

class PlayerMechanicListener : Listener {
    // check player speed
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerMove(e: PlayerMoveEvent) {
        val gp = GameManager.getGamePlayer(e.player) ?: return
        gp.speedSquared = e.to.distanceSquared(e.from)

        // bonus double jump reset
        if ((e.player as Entity).isOnGround && gp.game.state != Game.GameState.COUNTDOWN) {
            gp.jumpPhase = 0
            gp.player.allowFlight = true
        }
    }

    // apply knockback
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerHit(e: EntityDamageByEntityEvent) {
        if (e.entity !is Player || e.damager !is Player) return
        val d = e.entity as Player
        val a = e.damager as Player
        val aGP = GameManager.getGamePlayer(a)
        val dGP = GameManager.getGamePlayer(d)

        if (aGP == null && dGP == null) return
        if (aGP == null || dGP == null) {
            e.isCancelled = true
            return
        }

        if (aGP.game.arena.name != dGP.game.arena.name) return
        if (aGP.state != GamePlayer.PlayerState.IN_GAME || dGP.state != GamePlayer.PlayerState.IN_GAME) {
            e.isCancelled = true
            return
        }

        if (aGP.respawnPoint != null || dGP.respawnPoint != null) {
            e.isCancelled = true
            return
        }

        val aVertMultiplier = -a.velocity.y.coerceAtMost(0.0)
        val aMomentum = aGP.speed
        dGP.damage += Random.nextDouble(2.0, 3.0) + aMomentum * Random.nextDouble(10.0, 15.0)
        e.isCancelled = true
        d.damage(d.health - dGP.displayHealth)
        d.health = dGP.displayHealth
        val dKnockback =
            a.location.direction
                .normalize()
                .setY(if ((d as Entity).isOnGround) 0.5 else 0.5 * sign(a.location.direction.y))
                .multiply(Vector(dGP.damage / 30.0, dGP.damage / 40.0, dGP.damage / 30.0)) // cumulative damage
                .multiply(Vector(aMomentum + 1, (aVertMultiplier + 1) * 0.9, aMomentum + 1)) // current attack

        if (dKnockback.lengthSquared() < 1.9) {
            dKnockback.normalize().multiply(1.1)
        }
        d.velocity = dKnockback

        ActionBar(dGP.actionBarDisplay).send(d)
        dGP.game.scoreboard
            .getObjective("%")
            .getScore(d.name)
            .score = dGP.damage.roundToInt()
    }

    // make players not affected by world
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerHeal(e: EntityRegainHealthEvent) {
        if (e.entity !is Player) return
        val player = e.entity as Player
        if (!GameManager.isPlayerInGame(player)) return

        e.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerLoseHunger(e: FoodLevelChangeEvent) {
        if (e.entity !is Player) return
        val player = e.entity as Player
        if (!GameManager.isPlayerInGame(player)) return

        e.foodLevel = 20
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerFall(e: EntityDamageEvent) {
        if (e.entity !is Player) return
        if (e.cause != EntityDamageEvent.DamageCause.FALL) return
        val player = e.entity as Player
        if (!GameManager.isPlayerInGame(player)) return

        e.isCancelled = true
    }

    // triple jumping
    @EventHandler
    fun onPlayerFly(e: PlayerToggleFlightEvent) {
        val gp = GameManager.getGamePlayer(e.player) ?: return
        if (gp.state == GamePlayer.PlayerState.SPECTATING || gp.game.state == Game.GameState.COUNTDOWN) return
        if (gp.player.gameMode == GameMode.CREATIVE || gp.player.gameMode == GameMode.SPECTATOR) return

        e.isCancelled = true
        e.player.isFlying = false

        e.player.velocity =
            e.player.location.direction
                .multiply(1.1)
                .setY(1.15) // 1.5
        if (gp.jumpPhase == 1) e.player.allowFlight = false
        gp.jumpPhase += 1
    }
}
