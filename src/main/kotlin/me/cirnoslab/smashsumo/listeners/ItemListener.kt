package me.cirnoslab.smashsumo.listeners

import me.cirnoslab.smashsumo.game.GameManager
import me.cirnoslab.smashsumo.game.GamePlayer
import me.cirnoslab.smashsumo.item.ItemManager
import me.cirnoslab.smashsumo.item.events.ItemDropEvent
import me.cirnoslab.smashsumo.item.events.ItemInteractEvent
import me.cirnoslab.smashsumo.item.events.ItemPickupEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerPickupItemEvent

/**
 * Primary listener for item usage
 *
 * @see PlayerMechanicListener
 */
class ItemListener : Listener {
    /**
     * Detects whenever a player interacts using an item.
     * (left/right click)
     */
    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        val gp = GameManager.getGamePlayer(e.player) ?: return
        if (gp.state != GamePlayer.PlayerState.IN_GAME) return
        if (e.item == null) return
        val item = ItemManager.getItem(e.item) ?: return
        item.interact(ItemInteractEvent(gp, e.player, e.action, e))
    }

    /**
     * Detects whenever a player drops an item.
     */
    @EventHandler
    fun onDrop(e: PlayerDropItemEvent) {
        val gp = GameManager.getGamePlayer(e.player) ?: return
        if (gp.state != GamePlayer.PlayerState.IN_GAME) return
        val item = ItemManager.getItem(e.itemDrop.itemStack) ?: return
        item.drop(ItemDropEvent(gp, e.player, e))
    }

    /**
     * Detects whenever a player picks up an item
     */
    @EventHandler
    fun onPickup(e: PlayerPickupItemEvent) {
        val gp = GameManager.getGamePlayer(e.player) ?: return
        if (gp.state != GamePlayer.PlayerState.IN_GAME) return
        val item = ItemManager.getItem(e.item.itemStack) ?: return
        item.pickup(ItemPickupEvent(gp, e.player, e))
    }
}
