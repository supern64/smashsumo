package me.cirnoslab.smashsumo.commands

import me.cirnoslab.smashsumo.Config.Style.P
import me.cirnoslab.smashsumo.Utils
import me.cirnoslab.smashsumo.item.ItemManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Handler for item command
 */
object ItemCommands {
    fun handle(
        s: CommandSender,
        args: Array<out String>,
    ): Boolean {
        if (args.isEmpty() || args[0].lowercase() != "item") return false

        if (args.size < 2) {
            s.sendMessage("${P}Please specify an item name.")
            return true
        }

        if (s !is Player) {
            s.sendMessage("${P}Only players can use item commands.")
            return true
        }

        if (!s.hasPermission("smashsumo.admin")) {
            s.sendMessage("${P}You do not have permission to use this command.")
            return true
        }

        val item = ItemManager.getItem(args[1])
        if (item == null) {
            s.sendMessage("${P}That item does not exist.")
            return true
        }
        s.inventory.addItem(item.data.get())

        return true
    }

    fun complete(args: Array<out String>): List<String> {
        val completions =
            when (args.size) {
                2 -> ItemManager.items.keys.toList()
                else -> listOf()
            }
        return completions.sortedByDescending { a -> Utils.matchPrefixCount(a, args[args.size - 1]) }
    }

    fun canComplete(args: Array<out String>): Boolean = args.isNotEmpty() && args[0].lowercase() == "item"
}
