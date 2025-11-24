package me.cirnoslab.smashsumo.commands

import me.cirnoslab.smashsumo.Config.Style.P
import me.cirnoslab.smashsumo.Config.Style.S
import me.cirnoslab.smashsumo.Utils
import me.cirnoslab.smashsumo.item.ItemManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Handler for debug related commands
 */
object DebugCommands {
    fun handle(
        s: CommandSender,
        args: Array<out String>,
    ): Boolean {
        if (args.isEmpty() || args[0].lowercase() != "debug") return false

        if (args.size < 2) {
            s.sendMessage("${P}Usage: $S/smashsumo debug [item]")
            return true
        }

        if (s !is Player) {
            s.sendMessage("${P}Only players can use debug commands.")
            return true
        }

        if (!s.hasPermission("smashsumo.debug")) {
            s.sendMessage("${P}You do not have permission to use this command.")
            return true
        }

        when (args[1].lowercase()) {
            "item" -> {
                if (args.size < 3) {
                    s.sendMessage("${P}Please specify an item name.")
                    return true
                }
                val item = ItemManager.getItem(args[2])
                if (item == null) {
                    s.sendMessage("${P}That item does not exist.")
                    return true
                }
                s.inventory.addItem(item.data.get())
                return true
            }
            else -> {
                s.sendMessage("${P}Unknown subcommand. Usage: $S/smashsumo debug [item]")
                return true
            }
        }
    }

    fun complete(args: Array<out String>): List<String> {
        val completions =
            when (args.size) {
                2 -> listOf("item")
                3 -> if (args[1] == "item") ItemManager.items.keys.toList() else listOf()
                else -> listOf()
            }
        return completions.sortedByDescending { a -> Utils.matchPrefixCount(a, args[args.size - 1]) }
    }

    fun canComplete(args: Array<out String>): Boolean = args.isNotEmpty() && args[0].lowercase() == "debug"
}
