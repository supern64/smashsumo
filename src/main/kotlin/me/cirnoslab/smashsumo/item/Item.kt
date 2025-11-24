package me.cirnoslab.smashsumo.item

import me.cirnoslab.smashsumo.item.events.ItemArmorEvent
import me.cirnoslab.smashsumo.item.events.ItemDropEvent
import me.cirnoslab.smashsumo.item.events.ItemHitPlayerEvent
import me.cirnoslab.smashsumo.item.events.ItemInteractEvent
import me.cirnoslab.smashsumo.item.events.ItemPickupEvent
import net.minecraft.server.v1_8_R3.NBTTagCompound
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

/**
 * Represents an item. Must be registered with [ItemManager].
 * Can consist of item-specific functions defined here, or regular event listeners.
 * (NOTE: If you use a regular event listener, you are responsible for checking that your item is the correct one)
 */
abstract class Item : Listener {
    /**
     * The [ItemData] associated with this item
     */
    abstract val data: ItemData

    // simplified interaction events for less complex items

    /**
     * Calls whenever the player interacts with an item
     *
     * @param e the [ItemInteractEvent]
     */
    open fun interact(e: ItemInteractEvent) {}

    /**
     * Calls whenever a player drops the item
     *
     * @param e the [ItemInteractEvent]
     */
    open fun drop(e: ItemDropEvent) {}

    /**
     * Calls whenever a player picks up an item
     *
     * @param e the [ItemPickupEvent]
     */
    open fun pickup(e: ItemPickupEvent) {}

    /**
     * Calls whenever a player attacks another player with this item
     *
     * @param e the [ItemHitPlayerEvent]
     */
    open fun hit(e: ItemHitPlayerEvent) {}

    /**
     * Calls whenever a player is attacked while wearing this item
     *
     * @param e the [ItemArmorEvent]
     */
    open fun damage(e: ItemArmorEvent) {}

    /**
     * Companion object (must be [ItemData])
     */
    companion object : ItemData() {
        override val id = "base.generic_item"
    }
}

/**
 * Represents an [Item]'s companion object which contains its data.
 * Should be used within every single item.
 */
abstract class ItemData {
    /**
     * The ID of the item (must be unique)
     */
    abstract val id: String

    /**
     * The display name used in-game
     */
    open val displayName: String = id

    /**
     * Gets an [ItemStack] of this item.
     * If you override this, you are responsible for modifying the NBT yourself.
     *
     * @see withNBT
     * @param amount how many of the item to get
     * @return the ItemStack of this item
     */
    open fun get(amount: Int = 1): ItemStack {
        val stack = ItemStack(Material.STICK, amount)
        return withNBT(stack)
    }

    /**
     * Returns whether an ItemStack has the required NBT tag for this item.
     *
     * @param i the ItemStack
     * @return true if NBT tag exists, false otherwise
     */
    fun hasNBT(i: ItemStack): Boolean {
        val nmsI = CraftItemStack.asNMSCopy(i)
        if (nmsI == null || !nmsI.hasTag()) return false
        val compound = nmsI.tag
        val detectedID = compound.getCompound("smashsumo").getString("item_id")
        return detectedID.isNotEmpty() && detectedID.equals(id)
    }

    /**
     * Attaches an NBT tag with this item's ID to an ItemStack.
     *
     * @param i the ItemStack
     * @return the ItemStack with NBT Tag
     */
    fun withNBT(i: ItemStack): ItemStack {
        val meta = i.itemMeta
        meta.displayName = displayName
        i.itemMeta = meta

        val nmsI = CraftItemStack.asNMSCopy(i)
        val itemC = if (nmsI.hasTag()) nmsI.tag else NBTTagCompound()
        val ssC = itemC.getCompound("smashsumo")
        ssC.setString("item_id", id)
        itemC.set("smashsumo", ssC)
        nmsI.tag = itemC

        return CraftItemStack.asBukkitCopy(nmsI)
    }
}
