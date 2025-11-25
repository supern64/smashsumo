package me.cirnoslab.smashsumo.menu.inventory

import java.util.UUID

object InventoryMenuManager {
    val menus = mutableMapOf<UUID, InventoryMenu>()

    fun display(im: InventoryMenu) {
        menus[im.owner.uniqueId] = im
        im.owner.openInventory(im.inventory)
    }

    fun close(im: InventoryMenu) {
        menus.remove(im.owner.uniqueId)
        im.owner.closeInventory()
    }
}