package me.cirnoslab.smashsumo.menu.actionitem

import me.cirnoslab.smashsumo.game.Game
import me.cirnoslab.smashsumo.game.GameManager
import me.cirnoslab.smashsumo.menu.inventory.InventoryMenuManager
import me.cirnoslab.smashsumo.menu.inventory.KitSelectorMenu
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Action item to open kit selector
 */
class KitSelectorMenuItem : ActionItem() {
    override val data = Companion

    override fun rightClick(p: Player) {
        val gp = GameManager.getGamePlayer(p) ?: return
        if (gp.game.state != Game.GameState.WAITING) return

        InventoryMenuManager.display(KitSelectorMenu(gp))
    }

    companion object : ItemData() {
        override val id = "base.menu.kit_selector"
        override val displayName = "${ChatColor.GREEN}Select Kit ${ChatColor.GRAY}(Right Click)"

        override fun get(amount: Int): ItemStack {
            val stack = ItemStack(Material.DIAMOND_BARDING, amount)
            return withNBT(stack)
        }
    }
}
