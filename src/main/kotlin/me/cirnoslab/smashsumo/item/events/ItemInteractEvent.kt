package me.cirnoslab.smashsumo.item.events

import me.cirnoslab.smashsumo.game.GamePlayer
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Represents an interaction through [PlayerInteractEvent]
 *
 * @property player the [GamePlayer] of the player who interacted
 * @property mcPlayer the [Player] of the player who interacted
 * @property action the [Action] done by the player
 * @property raw the [PlayerInteractEvent] that triggered this
 */
data class ItemInteractEvent(
    val player: GamePlayer,
    val mcPlayer: Player,
    val action: Action,
    val raw: PlayerInteractEvent,
)
