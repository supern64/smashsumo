package me.cirnoslab.smashsumo.menu.inventory

import io.github.theluca98.textapi.ActionBar
import me.cirnoslab.smashsumo.Config
import me.cirnoslab.smashsumo.Config.Style.P
import me.cirnoslab.smashsumo.Config.Style.S
import me.cirnoslab.smashsumo.game.Game
import me.cirnoslab.smashsumo.game.GameManager
import me.cirnoslab.smashsumo.game.GamePlayer
import me.cirnoslab.smashsumo.kit.KitManager
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Menu to select kits
 */
class KitSelectorMenu(
    owner: GamePlayer,
    private val displayList: Array<ItemStack> = getMenuList(owner),
) : InventoryMenu(owner.player, "Kit Selector", displayList) {
    override fun click(
        index: Int?,
        e: InventoryClickEvent,
    ) {
        if (index == null || e.whoClicked !is Player) return
        val gp = GameManager.getGamePlayer(e.whoClicked as Player) ?: return
        if (gp.game.state != Game.GameState.WAITING) return
        if (index >= displayList.size) return

        if (index == 0) {
            gp.kit = null
        } else {
            val targetKitName = displayList[index].itemMeta.displayName
            if (Config.enforceKitPermissions &&
                !e.whoClicked.hasPermission("smashsumo.kit.$targetKitName") &&
                gp.game.settings.defaultKit
                    ?.name != targetKitName
            ) {
                return
            }

            gp.kit = KitManager.kits[targetKitName]
        }

        ActionBar(gp.actionBarDisplay).send(gp.player)
        gp.player.playSound(gp.player.location, Sound.NOTE_PIANO, 1.0f, 1.9f)
        gp.player.sendMessage("${P}Selected ${if (gp.kit != null) "the ${S}${gp.kit!!.name} ${P}kit." else "no kit."}")
        InventoryMenuManager.close(this)
    }

    companion object {
        private fun getMenuList(gp: GamePlayer): Array<ItemStack> {
            val list =
                KitManager.kits.values
                    .map { m ->
                        val h = ItemStack(m.icon, 1)
                        val hMeta = h.itemMeta
                        hMeta.displayName = m.name
                        h.itemMeta = hMeta
                        h
                    }.filter { k ->
                        !Config.enforceKitPermissions ||
                            gp.player.hasPermission("smashsumo.kit.${k.itemMeta.displayName}") ||
                            gp.game.settings.defaultKit
                                ?.name == k.itemMeta.displayName
                    }.toMutableList()
            val noneItem = ItemStack(Material.BARRIER, 1)
            val noneIM = noneItem.itemMeta
            noneIM.displayName = "${ChatColor.RED}None"
            noneItem.itemMeta = noneIM
            list.add(0, noneItem)
            return list.toTypedArray()
        }
    }
}
