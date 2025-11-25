package me.cirnoslab.smashsumo.listeners

import me.cirnoslab.smashsumo.menu.actionitem.ActionItemManager
import me.cirnoslab.smashsumo.menu.inventory.InventoryMenuManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent

class MenuListener : Listener {
    /**
     * Handles action items.
     */
    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        if (e.item == null || (e.action != Action.RIGHT_CLICK_AIR && e.action != Action.RIGHT_CLICK_BLOCK)) return
        val item = ActionItemManager.getItem(e.item) ?: return
        item.rightClick(e.player)
        e.isCancelled = true
    }

    /**
     * Handles inventory clicking.
     */
    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (!InventoryMenuManager.menus.containsKey(e.whoClicked.uniqueId)) return
        val offsetSlot = e.rawSlot - 10
        val isBorder = e.rawSlot !in (10..16) + (19..25) + (28..34) + (37..43)
        val chosen = if (!isBorder) offsetSlot - (offsetSlot + 1) / 9 * 2 else null // magic ass formula
        InventoryMenuManager.menus[e.whoClicked.uniqueId]!!.click(chosen, e)
        e.isCancelled = true
    }

    /**
     * Cleans up if inventory is closed.
     */
    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        if (!InventoryMenuManager.menus.containsKey(e.player.uniqueId)) return
        InventoryMenuManager.menus.remove(e.player.uniqueId)
    }
}
