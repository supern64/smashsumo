package me.cirnoslab.smashsumo.menu.actionitem

import me.cirnoslab.smashsumo.game.Game
import me.cirnoslab.smashsumo.game.GameManager
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QuitGameItem : ActionItem() {
    override val data = Companion

    override fun rightClick(p: Player) {
        val g = GameManager.getGame(p) ?: return
        if (g.state != Game.GameState.WAITING) return

        GameManager.leave(p)
    }

    companion object : ItemData() {
        override val id = "base.menu.quit_game"
        override val displayName = "${ChatColor.RED}Quit Game ${ChatColor.GRAY}(Right Click)"

        override fun get(amount: Int): ItemStack {
            val stack = ItemStack(Material.BED, amount)
            return withNBT(stack)
        }
    }
}
