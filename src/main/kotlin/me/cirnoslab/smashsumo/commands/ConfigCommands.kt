package me.cirnoslab.smashsumo.commands

import me.cirnoslab.smashsumo.Config
import me.cirnoslab.smashsumo.Config.Style.P
import me.cirnoslab.smashsumo.Config.Style.S
import me.cirnoslab.smashsumo.arena.ArenaManager
import org.bukkit.command.CommandSender

object ConfigCommands {
    fun handle(
        s: CommandSender,
        args: Array<out String>,
    ): Boolean {
        if (args.isEmpty() || args[0].lowercase() != "config") return false

        if (args.size < 2) {
            s.sendMessage("${P}Usage: $S/smashsumo config [lobby|reload]")
            return true
        }

        if (!s.hasPermission("smashsumo.admin")) {
            s.sendMessage("${P}You do not have permission to use this command.")
            return true
        }

        when (args[1].lowercase()) {
            "reload" -> {
                val arenaCount = ArenaManager.reload()
                Config.reload()
                s.sendMessage("${P}Configuration reloaded. ${S}$arenaCount ${P}arenas loaded.")
                return true
            }
            "lobby" -> {
                if (s !is org.bukkit.entity.Player) {
                    s.sendMessage("${P}Only players can set the lobby spawn.")
                    return true
                }
                Config.lobbyPosition = s.location
                Config.save()
                s.sendMessage("${P}Lobby spawn set to your current location.")
                return true
            }
            else -> {
                s.sendMessage("${P}Unknown subcommand. Usage: $S/smashsumo config [lobby|reload]")
                return true
            }
        }
    }
}
