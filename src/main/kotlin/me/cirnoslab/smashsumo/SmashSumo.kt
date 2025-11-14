package me.cirnoslab.smashsumo

import me.cirnoslab.smashsumo.arena.ArenaManager
import me.cirnoslab.smashsumo.commands.ArenaCommands
import me.cirnoslab.smashsumo.commands.ConfigCommands
import me.cirnoslab.smashsumo.commands.GameCommands
import me.cirnoslab.smashsumo.game.HUDManager
import me.cirnoslab.smashsumo.listeners.GameListener
import me.cirnoslab.smashsumo.listeners.PlayerMechanicListener
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class SmashSumo : JavaPlugin() {
    override fun onEnable() {
        plugin = this
        server.pluginManager.registerEvents(PlayerMechanicListener(), this)
        server.pluginManager.registerEvents(GameListener(), this)
        HUDManager.SendHUD().runTaskTimer(this, 0L, 5L)

        Config.init(this)
        val arenaCount = ArenaManager.init(dataFolder)
        log("Plugin enabled. Loaded config and $arenaCount arenas.")
    }

    override fun onDisable() {
        log("Plugin disabled.")
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (!command.name.equals("smashsumo", ignoreCase = true)) return false
        return ArenaCommands.handle(sender, args) || GameCommands.handle(sender, args) || ConfigCommands.handle(sender, args)
    }

    companion object {
        fun log(text: String) {
            plugin.logger.log(Level.INFO, text)
        }

        fun log(
            level: Level,
            text: String,
        ) {
            plugin.logger.log(level, text)
        }

        lateinit var plugin: SmashSumo

        val SCOREBOARD_LINE: String = "${ChatColor.WHITE}${ChatColor.STRIKETHROUGH}-------------------${ChatColor.RESET}"
    }
}
