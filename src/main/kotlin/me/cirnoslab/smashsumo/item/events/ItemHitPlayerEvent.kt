package me.cirnoslab.smashsumo.item.events

import me.cirnoslab.smashsumo.game.GamePlayer
import me.cirnoslab.smashsumo.game.HitValue
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent

/**
 * Represents a player attacking using an item
 *
 * @property attacker the [GamePlayer] of the player who attacked
 * @property mcAttacker the [Player] of the player who attacked
 * @property defender the [GamePlayer] of the player who was attacked
 * @property defender the [Player] of the player who was attacked
 * @property hit the [HitValue] to use for this hit
 * @property raw the [EntityDamageByEntityEvent] that triggered this
 */
data class ItemHitPlayerEvent(
    val attacker: GamePlayer,
    val mcAttacker: Player,
    val defender: GamePlayer,
    val mcDefender: Player,
    val hit: HitValue,
    val raw: EntityDamageByEntityEvent,
)
