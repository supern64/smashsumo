package me.cirnoslab.smashsumo.item.events

import me.cirnoslab.smashsumo.game.GamePlayer
import me.cirnoslab.smashsumo.game.HitValue
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent

/**
 * Represents a player being attacked while wearing an item
 *
 * @property attacker the [GamePlayer] of the player who attacked
 * @property mcAttacker the [Player] of the player who attacked
 * @property defender the [GamePlayer] of the player who was attacked
 * @property mcDefender the [Player] of the player who was attacked
 * @property hit the [HitValue] to use for this hit
 * @property raw the [EntityDamageByEntityEvent] that triggered this, will be null if hit by other interaction
 * @property mcProjectile the [Projectile] that hit the player (if exists)
 */
data class ItemArmorEvent(
    val attacker: GamePlayer,
    val defender: GamePlayer,
    val hit: HitValue,
    val raw: EntityDamageByEntityEvent?,
    val mcAttacker: Player = attacker.player,
    val mcDefender: Player = defender.player,
    val mcProjectile: Projectile? = null,
)
