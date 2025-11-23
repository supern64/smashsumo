package me.cirnoslab.smashsumo.kit

import me.cirnoslab.smashsumo.item.ItemManager
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Represents a kit that can be given to a player
 *
 * @property name the name of the kit
 * @property items the list of items to give
 */
class Kit(
    val name: String,
    val items: List<Item>,
) {
    /**
     * Applies this kit onto a player.
     *
     * @param p the player to apply this kit to
     * @param clear whether to clear the inventory before applying the kit
     */
    fun apply(
        p: Player,
        clear: Boolean = true,
    ) {
        if (clear) p.inventory.clear()
        for (item in items) {
            p.inventory.setItem(item.inventorySlot, item.get())
        }
    }

    /**
     * Replenishes items marked as such.
     *
     * @param p the player to replenish items for
     */
    fun replenish(p: Player) {
        for (item in items) {
            if (!item.replenishOnDeath) continue
            p.inventory.setItem(item.inventorySlot, item.get())
        }
    }

    /**
     * Represents an item within a kit,
     * mcMaterial and ssItemID are mutually exclusive
     *
     * @property amount the amount of the item
     * @property inventorySlot what inventory slot the item will be in, see [org.bukkit.inventory.PlayerInventory.setItem]
     * @property replenishOnDeath whether to replenish the item up to the original amount when the player dies
     * @property mcMaterial the [Material] this item represents
     * @property ssItemID the ID of the plugin item this item represents
     * @throws IllegalArgumentException if mcMaterial and ssItemID are not defined, or both are defined
     */
    class Item(
        val amount: Int,
        val inventorySlot: Int,
        val replenishOnDeath: Boolean,
        val mcMaterial: Material?,
        val ssItemID: String?,
    ) {
        init {
            require(mcMaterial != null || ssItemID != null) { "either mcMaterial or ssItemID must be defined" }
            require(mcMaterial == null || ssItemID == null) { "only one of mcMaterial and ssItemID can exist" }
            require(inventorySlot in 0..39) { "invalid inventory slot" }
        }

        /**
         * Gets the item represented as an [ItemStack].
         * Returns a stick named "INVALID ITEM" if the custom item doesn't exist.
         *
         * @return the ItemStack
         */
        fun get(): ItemStack {
            if (mcMaterial != null) {
                return ItemStack(mcMaterial, amount)
            } else {
                val item = ItemManager.getItem(ssItemID!!)
                if (item == null) {
                    val h = ItemStack(Material.STICK, 1)
                    val meta = h.itemMeta
                    meta.displayName = "${ChatColor.RED}${ChatColor.BOLD}INVALID ITEM"
                    h.itemMeta = meta
                    return h
                }
                return item.data.get(amount)
            }
        }
    }
}
