package me.cirnoslab.smashsumo.menu.actionitem

import net.minecraft.server.v1_8_R3.NBTTagCompound
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

/**
 * Represents an action item that can be right-clicked to trigger events.
 */
abstract class ActionItem : Listener {
    /**
     * The [ItemData] associated with this item
     */
    abstract val data: ItemData

    /**
     * Calls whenever the player right-clicks an item.
     */
    open fun rightClick(p: Player) {}

    /**
     * Companion object (must be [ItemData])
     */
    companion object : ItemData() {
        override val id = "base.generic_action_item"
    }

    /**
     * Represents an [ActionItem]'s companion object which contains its data.
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
         * Gets an [org.bukkit.inventory.ItemStack] of this item.
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
            val detectedID = compound.getCompound("smashsumo").getString("action_item_id")
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
            ssC.setString("action_item_id", id)
            itemC.set("smashsumo", ssC)
            nmsI.tag = itemC

            return CraftItemStack.asBukkitCopy(nmsI)
        }
    }
}
