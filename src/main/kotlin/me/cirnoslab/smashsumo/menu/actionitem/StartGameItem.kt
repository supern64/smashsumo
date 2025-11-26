package me.cirnoslab.smashsumo.menu.actionitem

import me.cirnoslab.smashsumo.Config.Style.P
import me.cirnoslab.smashsumo.game.Game
import me.cirnoslab.smashsumo.game.GameManager
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Action item to start game, only usable with smashsumo.gm
 */
class StartGameItem : ActionItem() {
    override val data = Companion

    override fun rightClick(p: Player) {
        val g = GameManager.getGame(p) ?: return
        if (g.state != Game.GameState.WAITING) return
        if (!p.hasPermission("smashsumo.gm")) return

        if (g.gamePlayers.size < 2) {
            p.sendMessage("${P}At least 2 players are required to start the game.")
            return
        }
        g.initStart()
    }

    companion object : ItemData() {
        override val id = "base.menu.start_game"
        override val displayName = "${ChatColor.GREEN}Start Game ${ChatColor.GRAY}(Right Click)"

        override fun get(amount: Int): ItemStack {
            val stack = ItemStack(Material.LEASH, amount)
            return withNBT(stack)
        }
    }
}
