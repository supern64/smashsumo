package me.cirnoslab.smashsumo.menu.actionitem

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

/**
 * Action item manager singleton
 */
object ActionItemManager {
    /**
     * Map of IDs to registered items
     */
    val items = mutableMapOf<String, ActionItem>()

    /**
     * Loads initial action items. Called by [me.cirnoslab.smashsumo.SmashSumo]
     */
    fun init() {
        initItem(StartGameItem())
        initItem(QuitGameItem())
        initItem(KitSelectorMenuItem())
    }

    /**
     * Registers the item as a listener and internally.
     *
     * @param i the Item to register
     * @throws IllegalStateException if an item with the same ID already exists
     * @throws IllegalArgumentException if item ID is empty
     */
    fun initItem(i: ActionItem) {
        if (items.containsKey(i.data.id)) throw IllegalStateException("an item with this ID already exists")
        if (i.data.id.isEmpty()) throw IllegalArgumentException("item ID cannot be empty")
        items[i.data.id] = i
    }

    /**
     * Gets an item instance by its ID.
     *
     * @param id the ID of the item
     * @return the item if exists, otherwise null
     */
    fun getItem(id: String): ActionItem? = items[id]

    /**
     * Gets an item instance by an ItemStack's NBT value
     *
     * @param stack the ItemStack
     * @return the item if exists, otherwise null
     */
    fun getItem(stack: ItemStack): ActionItem? {
        val nmsI = CraftItemStack.asNMSCopy(stack)
        if (nmsI == null || !nmsI.hasTag()) return null
        val compound = nmsI.tag
        val itemID = compound.getCompound("smashsumo").getString("action_item_id")
        if (itemID.isEmpty()) return null
        return items[itemID]
    }
}
