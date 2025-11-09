package me.cirnoslab.smashsumo.listeners

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import io.papermc.paper.event.entity.EntityKnockbackEvent
import io.papermc.paper.scoreboard.numbers.NumberFormat
import me.cirnoslab.smashsumo.game.Game
import me.cirnoslab.smashsumo.game.GameManager
import me.cirnoslab.smashsumo.game.GamePlayer
import net.kyori.adventure.text.Component.text
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
import kotlin.math.sign
import kotlin.random.Random

class PlayerMechanicListener : Listener {
    // check player speed
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerMove(e: PlayerMoveEvent) {
        val gp = GameManager.getGamePlayer(e.player) ?: return
        gp.speedSquared = e.to.distanceSquared(e.from)

        // bonus double jump reset
        if ((e.player as Entity).isOnGround) {
            gp.jumpPhase = 0
            gp.player.allowFlight = true
        }
    }

    // apply knockback & damage
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

        aGP.lastHitVerticalMultiplier = -a.velocity.y.coerceAtMost(0.0)
        aGP.lastHitMomentum = aGP.speed
        dGP.damage += Random.nextDouble(2.0, 3.0) + aGP.lastHitMomentum * Random.nextDouble(10.0, 15.0)
        e.damage = (d.health - dGP.displayHealth).coerceAtLeast(0.0)

        dGP.player.sendActionBar(dGP.actionBarDisplay)
        dGP.game.scoreboard.getObjective("percent")?.getScore(dGP.player.name)?.numberFormat(
            NumberFormat.fixed(
                dGP.lifeComponent
                    .appendSpace()
                    .append(text("%.1f".format(dGP.damage), dGP.damageColor)),
            ),
        )
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerKnockback(e: EntityKnockbackByEntityEvent) {
        if (e.cause != EntityKnockbackEvent.Cause.ENTITY_ATTACK) return
        if (e.entity !is Player || e.hitBy !is Player) return
        val d = e.entity as Player
        val a = e.hitBy as Player
        val aGP = GameManager.getGamePlayer(a)
        val dGP = GameManager.getGamePlayer(d)

        if (aGP == null && dGP == null) return
        if (aGP == null || dGP == null) {
            e.isCancelled = true
            return
        }

        // must've passed through EntityDamageByEntityEvent to get to this point
        val dKnockback =
            a.location.direction
                .normalize()
                .setY(if ((d as Entity).isOnGround) 0.5 else 0.5 * sign(a.location.direction.y))
                .multiply(Vector(dGP.damage / 30.0, dGP.damage / 40.0, dGP.damage / 30.0)) // cumulative damage
                // current attack
                .multiply(Vector(aGP.lastHitMomentum + 1, (aGP.lastHitVerticalMultiplier + 1) * 0.9, aGP.lastHitMomentum + 1))

        if (dKnockback.lengthSquared() < 1.9) {
            dKnockback.normalize().multiply(1.1)
        }
        e.knockback = dKnockback
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
        if (gp.jumpPhase == 2) {
            e.player.allowFlight = false
            return
        }
        e.player.velocity =
            e.player.location.direction
                .multiply(1.1)
                .setY(1.15) // 1.5
        gp.jumpPhase += 1
    }
}
