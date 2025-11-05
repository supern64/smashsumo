package me.cirnoslab.smashsumo.commands

import me.cirnoslab.smashsumo.SmashSumo
import me.cirnoslab.smashsumo.SmashSumo.Companion.P
import me.cirnoslab.smashsumo.SmashSumo.Companion.S
import me.cirnoslab.smashsumo.Utils
import me.cirnoslab.smashsumo.arena.ArenaManager
import org.bukkit.command.CommandSender

object ConfigCommands {
    fun handle(s: CommandSender, args: Array<out String>): Boolean {
        if (args.isEmpty() || args[0].lowercase() != "config") return false

        when (args[1].lowercase()) {
            "reload" -> {
                ArenaManager.reload()
                SmashSumo.config.reload()
                s.sendMessage("${P}Configuration reloaded.")
                return true
            }
            "lobby" -> {
                if (s !is org.bukkit.entity.Player) {
                    s.sendMessage("${P}Only players can set the lobby spawn.")
                    return true
                }
                SmashSumo.config.set("lobby", Utils.l2s(s.location))
                SmashSumo.config.save()
                s.sendMessage("${P}Lobby spawn set to your current location.")
                return true
            }
            else -> {
                s.sendMessage("${P}Unknown subcommand. Usage: ${S}/smashsumo config [lobby|reload]")
                return true
            }
        }
    }
}