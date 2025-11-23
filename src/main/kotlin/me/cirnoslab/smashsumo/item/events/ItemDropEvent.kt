package me.cirnoslab.smashsumo.item.events

import me.cirnoslab.smashsumo.game.GamePlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerDropItemEvent

/**
 * Represents a player dropping an item
 *
 * @property player the [GamePlayer] of the player who dropped the item
 * @property mcPlayer the [Player] of the player who dropped the item
 * @property raw the [PlayerDropItemEvent] that triggered this
 */
data class ItemDropEvent(
    val player: GamePlayer,
    val mcPlayer: Player,
    val raw: PlayerDropItemEvent,
)
