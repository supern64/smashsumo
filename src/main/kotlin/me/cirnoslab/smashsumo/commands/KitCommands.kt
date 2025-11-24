package me.cirnoslab.smashsumo.commands

import me.cirnoslab.smashsumo.Config.Style.P
import me.cirnoslab.smashsumo.Config.Style.S
import me.cirnoslab.smashsumo.Utils
import me.cirnoslab.smashsumo.kit.Kit
import me.cirnoslab.smashsumo.kit.KitManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Handler for kit related commands
 */
object KitCommands {
    fun handle(
        s: CommandSender,
        args: Array<out String>,
    ): Boolean {
        if (args.isEmpty() || args[0].lowercase() != "kit") return false

        if (s !is Player) {
            s.sendMessage("${P}Only players can use kit commands.")
            return true
        }

        if (args.size < 2) {
            s.sendMessage("${P}Usage: $S/smashsumo kit [save|equip|list|delete]")
            return true
        }

        if (!s.hasPermission("smashsumo.admin")) {
            s.sendMessage("${P}You do not have permission to use this command.")
            return true
        }

        when (args[1].lowercase()) {
            "save" -> {
                if (args.size < 3) {
                    s.sendMessage("${P}Please specify a kit name.")
                    return true
                }
                val name = args[2]
                val kit = Kit.fromInventory(s.inventory, name)
                if (kit.items.isEmpty()) {
                    s.sendMessage("${P}Cannot save a kit with no items.")
                    return true
                }
                KitManager.kits[name] = kit
                KitManager.saveKits()
                s.sendMessage("${P}Saved current inventory to kit ${S}${name}$P.")
            }
            "equip" -> {
                if (args.size < 3) {
                    s.sendMessage("${P}Please specify a kit name.")
                    return true
                }
                val name = args[2]
                if (!KitManager.kits.containsKey(name)) {
                    s.sendMessage("${P}Kit ${S}$name ${P}does not exist.")
                    return true
                }
                KitManager.kits[name]!!.apply(s)
                s.sendMessage("${P}Loaded kit ${S}$name$P.")
            }
            "list" -> {
                s.sendMessage("${P}Available kits: ${S}${KitManager.kits.keys.joinToString(", ")}")
            }
            "delete" -> {
                if (args.size < 3) {
                    s.sendMessage("${P}Please specify a kit name.")
                    return true
                }
                if (!KitManager.kits.containsKey(args[2])) {
                    s.sendMessage("${P}Kit ${S}${args[2]} ${P}does not exist.")
                    return true
                }
                KitManager.kits.remove(args[2])
                KitManager.saveKits()
                s.sendMessage("${P}Kit ${S}${args[2]} ${P}removed.")
            }
            else -> {
                s.sendMessage(
                    "${P}Unknown subcommand. Usage: $S/smashsumo kit [save|equip|list|delete]",
                )
            }
        }
        return true
    }

    fun complete(args: Array<out String>): List<String> {
        val completions =
            when (args.size) {
                2 -> listOf("save", "equip", "list", "delete")
                3 -> if (listOf("save", "equip", "delete").contains(args[1])) KitManager.kits.keys.toList() else listOf()
                else -> listOf()
            }
        return completions.sortedByDescending { a -> Utils.matchPrefixCount(a, args[args.size - 1]) }
    }

    fun canComplete(args: Array<out String>): Boolean = args.isNotEmpty() && args[0].lowercase() == "kit"
}
