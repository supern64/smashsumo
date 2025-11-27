package me.cirnoslab.smashsumo.item.events

import me.cirnoslab.smashsumo.game.GamePlayer
import me.cirnoslab.smashsumo.game.HitValue
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent

/**
 * Represents a player attacking using an item (does not include projectiles)
 *
 * @property attacker the [GamePlayer] of the player who attacked
 * @property mcAttacker the [Player] of the player who attacked
 * @property defender the [GamePlayer] of the player who was attacked
 * @property defender the [Player] of the player who was attacked
 * @property hit the [HitValue] to use for this hit
 * @property raw the [EntityDamageByEntityEvent] that triggered this, will be null if hit using other interaction
 */
data class ItemHitPlayerEvent(
    val attacker: GamePlayer,
    val defender: GamePlayer,
    val hit: HitValue,
    val raw: EntityDamageByEntityEvent?,
    val mcAttacker: Player = attacker.player,
    val mcDefender: Player = defender.player,
)
