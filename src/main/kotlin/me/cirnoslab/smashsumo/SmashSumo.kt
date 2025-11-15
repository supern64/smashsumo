package me.cirnoslab.smashsumo

import me.cirnoslab.smashsumo.arena.ArenaManager
import me.cirnoslab.smashsumo.commands.ArenaCommands
import me.cirnoslab.smashsumo.commands.ConfigCommands
import me.cirnoslab.smashsumo.commands.RootCommands
import me.cirnoslab.smashsumo.game.Game
import me.cirnoslab.smashsumo.game.GameManager
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
        GameManager.games.forEach { game ->
            game.gamePlayers.values.forEach { gp ->
                Game.deinitPlayer(gp)
            }
        }
        GameManager.games.clear()
        log("Plugin disabled.")
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (!command.name.equals("smashsumo", ignoreCase = true)) return false // shouldn't be possible
        return ArenaCommands.handle(sender, args) || ConfigCommands.handle(sender, args) || RootCommands.handle(sender, args)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String> {
        if (!command.name.equals("smashsumo", ignoreCase = true)) return listOf() // shouldn't be possible
        if (ArenaCommands.canComplete(sender, args)) return ArenaCommands.complete(sender, args)
        if (ConfigCommands.canComplete(sender, args)) return ConfigCommands.complete(sender, args)
        return RootCommands.complete(sender, args)
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
