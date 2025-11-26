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

        for (y in 0..lineCount + 1) {
            inventory.setItem(y * 9, filler)
            inventory.setItem(y * 9 + 8, filler)
        }

        updateOptions()
    }

    /**
     * Updates this inventory for the player's owner
     */
    fun update() {
        owner.updateInventory()
    }

    /**
     * Generate the options in [inventory]
     */
    open fun updateOptions() {
        var index = 10
        var inCurrentRow = 0
        for (option in options) {
            inventory.setItem(index, option)
            if (inCurrentRow < 6) {
                index++
                inCurrentRow++
            } else {
                index += 3
                inCurrentRow = 0
            }
        }
    }

    /**
     * Called when a player clicks on the inventory
     *
     * @param index the index of [options] that was clicked, null if click outside
     * @param e the [InventoryClickEvent] that triggered this
     */
    open fun click(
        index: Int?,
        e: InventoryClickEvent,
    ) {}

    /**
     * Calls when a player closes the inventory
     *
     * @param e the [InventoryCloseEvent] that triggered this
     */
    open fun close(e: InventoryCloseEvent) {}
}
