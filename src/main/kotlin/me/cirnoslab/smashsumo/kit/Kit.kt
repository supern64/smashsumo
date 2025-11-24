package me.cirnoslab.smashsumo.kit

import me.cirnoslab.smashsumo.Utils.clearInventory
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

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
        if (clear) p.clearInventory()
        for (item in items) {
            p.inventory.setItem(item.slot, item.get())
        }
    }

    /**
     * Get a list of Items where all items in the kit are the amount they should be replenished to (armor is always unique)
     */
    fun toReplenish(): List<Item> {
        val seenItems = mutableListOf<Item>()
        for (item in items) {
            if (!item.replenishOnDeath) continue
            if (item.isArmor) {
                seenItems.add(item)
                continue
            }
            val seen = seenItems.firstOrNull { i -> i.stack.isSimilar(item.stack) }
            if (seen != null) {
                seen.stack.amount += item.stack.amount
            } else {
                seenItems.add(Item(item.slot, item.stack.clone(), true))
            }
        }
        return seenItems
    }

    /**
     * Replenishes items marked as such.
     *
     * @param p the player to replenish items for
     */
    fun replenish(p: Player) {
        val combined = toReplenish()
        for (item in combined) {
            if (!item.isArmor) {
                if (p.inventory.containsAtLeast(item.stack, item.stack.amount)) continue
                val inventoryAmount =
                    p.inventory.contents
                        .filter { r -> r != null && r.isSimilar(item.stack) }
                        .map { r -> r.amount }

                val amount = if (inventoryAmount.isEmpty()) 0 else inventoryAmount.reduce { a, b -> a + b }

                if (amount == 0) {
                    p.inventory.setItem(item.slot, item.get())
                } else {
                    p.inventory.addItem(item.get(item.stack.amount - amount))
                }
            } else {
                if (p.inventory.contains(item.stack)) continue
                p.inventory.setItem(item.slot, item.get())
            }
        }
    }

    companion object {
        /**
         * Creates a kit from a [PlayerInventory].
         *
         * @param i the player inventory
         * @param name the kit's name
         * @return the kit created
         */
        fun fromInventory(
            i: PlayerInventory,
            name: String,
        ): Kit {
            val itemList = mutableListOf<Item>()
            i.contents.forEachIndexed { index, s ->
                if (s == null) return@forEachIndexed
                itemList.add(Item(index, s.clone(), true))
            }
            i.armorContents.forEachIndexed { index, s ->
                if (s == null || s.amount < 1) return@forEachIndexed
                // boots - 36, leggings - 37, chestplate - 38, helmet = 39
                itemList.add(Item(index + 36, s.clone(), true))
            }
            return Kit(name, itemList)
        }
    }

    /**
     * Represents an item within a kit,
     * mcMaterial and ssItemID are mutually exclusive
     *
     * @property slot what inventory slot the item will be in, see [org.bukkit.inventory.PlayerInventory.setItem]
     * @property stack the ItemStack that represents this item
     * @property replenishOnDeath whether to replenish the item up to the original amount when the player dies
     * @throws IllegalArgumentException if mcMaterial and ssItemID are not defined, or both are defined
     */
    class Item(
        val slot: Int,
        val stack: ItemStack,
        val replenishOnDeath: Boolean,
    ) {
        init {
            require(slot in 0..39) { "invalid inventory slot" }
        }

        val isArmor
            get() = slot >= 36

        /**
         * Gets the item represented as an [ItemStack].
         *
         * @return the ItemStack
         */
        fun get(amount: Int? = null): ItemStack {
            val st = stack.clone()
            if (amount == null) return st
            st.amount = amount
            return st
        }
    }
}
