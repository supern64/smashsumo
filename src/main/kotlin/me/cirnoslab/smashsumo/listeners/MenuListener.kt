package me.cirnoslab.smashsumo.listeners

import me.cirnoslab.smashsumo.menu.actionitem.ActionItemManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class MenuListener : Listener {
    /**
     * Handles action items.
     */
    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        if (e.item == null) return
        val item = ActionItemManager.getItem(e.item) ?: return
        item.rightClick(e.player)
        e.isCancelled = true
    }
}
