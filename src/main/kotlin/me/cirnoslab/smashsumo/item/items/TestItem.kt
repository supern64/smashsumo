package me.cirnoslab.smashsumo.item.items

import me.cirnoslab.smashsumo.item.Item
import me.cirnoslab.smashsumo.item.ItemData
import me.cirnoslab.smashsumo.item.events.ItemArmorEvent
import me.cirnoslab.smashsumo.item.events.ItemDropEvent
import me.cirnoslab.smashsumo.item.events.ItemHitPlayerEvent
import me.cirnoslab.smashsumo.item.events.ItemInteractEvent
import me.cirnoslab.smashsumo.item.events.ItemPickupEvent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class TestItem : Item() {
    override val data = Companion

    override fun interact(e: ItemInteractEvent) {
        e.mcPlayer.sendMessage("interaction ${e.action}")
    }

    override fun drop(e: ItemDropEvent) {
        e.mcPlayer.sendMessage("drop")
    }

    override fun pickup(e: ItemPickupEvent) {
        e.mcPlayer.sendMessage("pickup")
    }

    override fun hit(e: ItemHitPlayerEvent) {
        e.hit.initialY = 0.0
        e.hit.damage = 1.0
    }

    override fun damage(e: ItemArmorEvent) {
        e.hit.initialY = 1.0
        e.hit.damage = 0.0
    }

    companion object : ItemData() {
        override val id = "base.test"
        override val displayName = "Test Item"

        override fun get(amount: Int): ItemStack {
            val stack = ItemStack(Material.DIAMOND_HELMET, amount)
            return withNBT(stack)
        }
    }
}
