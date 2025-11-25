package me.cirnoslab.smashsumo.menu.inventory

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Represents an inventory menu that can be clicked.
 *
 * @property owner the Player this menu is for
 * @property title the title to use for the header
 * @property options the ItemStacks that will be displayed as choices in the menu
 */
open class InventoryMenu(
    val owner: Player,
    title: String,
    val options: Array<ItemStack>,
) {
    /*
          B B B B B B B B B
          B I I I I I I I B 10-16
          B I I I I I I I B 19-25
          B I I I I I I I B 28-34
          B I I I I I I I B 37-43
          B B B B B B B B B
     */

    // options.size / 7 * 9
    val lineCount = (options.size + 7 - 1) / 7
    val inventory: Inventory = Bukkit.createInventory(null, (lineCount + 2) * 9, title)

    init {
        require(options.size in 1..28) { "only 1-28 options are supported" }

        // create filler blocks
        val filler = ItemStack(Material.STAINED_GLASS_PANE, 1, 15)
        val fMeta = filler.itemMeta
        fMeta.displayName = " "
        filler.itemMeta = fMeta

        for (x in 0..8) {
            inventory.setItem(x, filler)
            inventory.setItem(x + (lineCount + 1) * 9, filler)
        }

        for (y in 0..lineCount + 2) {
            inventory.setItem(y * 9, filler)
            inventory.setItem(y * 9 + 8, filler)
        }
    }

    fun update() {
        owner.updateInventory()
    }

    open fun click(
        index: Int?,
        e: InventoryClickEvent,
    ) {}

    open fun close(e: InventoryCloseEvent) {}
}
