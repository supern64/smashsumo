package me.cirnoslab.smashsumo.item.events

import me.cirnoslab.smashsumo.game.GamePlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerPickupItemEvent

/**
 * Represents a player picking up an item
 *
 * @property player the [GamePlayer] of the player who picked up the item
 * @property mcPlayer the [Player] of the player who picked up the item
 * @property raw the [PlayerPickupItemEvent] that triggered this
 */
data class ItemPickupEvent(
    val player: GamePlayer,
    val mcPlayer: Player,
    val raw: PlayerPickupItemEvent,
)
