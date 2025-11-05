package me.cirnoslab.smashsumo.listeners

import io.github.theluca98.textapi.ActionBar
import me.cirnoslab.smashsumo.game.GameManager
import me.cirnoslab.smashsumo.game.GamePlayer
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
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import kotlin.math.sign
import kotlin.random.Random

class PlayerMechanicListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerMove(e: PlayerMoveEvent) {
        val gp = GameManager.getGamePlayer(e.player) ?: return
        gp.speedSquared = e.to.distanceSquared(e.from)
    }

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
        val dKnockback = a.location.direction
            .normalize()
            .setY(if ((d as Entity).isOnGround) 0.5 else 0.5 * sign(a.location.direction.y))
            .multiply(Vector(dGP.damage / 30.0, dGP.damage / 40.0, dGP.damage / 30.0)) // cumulative damage
            .multiply(Vector(aMomentum + 1, (aVertMultiplier + 1) * 0.9, aMomentum + 1)) // current attack

        if (dKnockback.lengthSquared() < 1.9) {
            dKnockback.normalize().multiply(1.1)
        }
        d.velocity = dKnockback

        ActionBar(dGP.actionBarDisplay).send(d)
    }

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
}