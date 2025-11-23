package me.cirnoslab.smashsumo.item

import me.cirnoslab.smashsumo.item.items.TestItem
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

/**
 * Item manager singleton
 */
object ItemManager {
    /**
     * The [JavaPlugin] instance
     */
    lateinit var p: JavaPlugin

    /**
     * Map of IDs to registered items
     */
    val items = mutableMapOf<String, Item>()

    /**
     * Loads initial base items. Called by [me.cirnoslab.smashsumo.SmashSumo]
     *
     * @param p the plugin instance
     */
    fun init(p: JavaPlugin) {
        this.p = p

        initItem(TestItem())
    }

    /**
     * Registers the item as a listener and internally.
     *
     * @param i the Item to register
     * @throws IllegalStateException if an item with the same ID already exists
     * @throws IllegalArgumentException if item ID is empty
     */
    fun initItem(i: Item) {
        if (items.containsKey(i.data.id)) throw IllegalStateException("an item with this ID already exists")
        if (i.data.id.isEmpty()) throw IllegalArgumentException("item ID cannot be empty")
        items[i.data.id] = i
        p.server.pluginManager.registerEvents(i, p)
    }

    /**
     * Gets an item instance by its ID.
     *
     * @param id the ID of the item
     * @return the item if exists, otherwise null
     */
    fun getItem(id: String): Item? = items[id]

    /**
     * Gets an item instance by an ItemStack's NBT value
     *
     * @param stack the ItemStack
     * @return the item if exists, otherwise null
     */
    fun getItem(stack: ItemStack): Item? {
        val nmsI = CraftItemStack.asNMSCopy(stack)
        if (nmsI == null || !nmsI.hasTag()) return null
        val compound = nmsI.tag
        val itemID = compound.getCompound("smashsumo").getString("item_id")
        if (itemID.isEmpty()) return null
        return items[itemID]
    }
}
