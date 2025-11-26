package me.cirnoslab.smashsumo.menu.inventory

import java.util.UUID

/**
 * Inventory menu manager singleton
 */
object InventoryMenuManager {
    /**
     * A map of active inventory menus for player UUIDs
     */
    val menus = mutableMapOf<UUID, InventoryMenu>()

    /**
     * Displays an [InventoryMenu]
     *
     * @param im the InventoryMenu
     */
    fun display(im: InventoryMenu) {
        menus[im.owner.uniqueId] = im
        im.owner.openInventory(im.inventory)
    }

    /**
     * Closes an [InventoryMenu]
     *
     * @param im the InventoryMenu
     */
    fun close(im: InventoryMenu) {
        menus.remove(im.owner.uniqueId)
        im.owner.closeInventory()
    }
}
