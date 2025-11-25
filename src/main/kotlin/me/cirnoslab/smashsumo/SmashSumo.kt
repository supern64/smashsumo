package me.cirnoslab.smashsumo

import me.cirnoslab.smashsumo.arena.ArenaManager
import me.cirnoslab.smashsumo.commands.ArenaCommands
import me.cirnoslab.smashsumo.commands.ConfigCommands
import me.cirnoslab.smashsumo.commands.DebugCommands
import me.cirnoslab.smashsumo.commands.KitCommands
import me.cirnoslab.smashsumo.commands.RootCommands
import me.cirnoslab.smashsumo.game.GameManager
import me.cirnoslab.smashsumo.game.HUDManager
import me.cirnoslab.smashsumo.item.ItemManager
import me.cirnoslab.smashsumo.kit.KitManager
import me.cirnoslab.smashsumo.listeners.GameListener
import me.cirnoslab.smashsumo.listeners.ItemListener
import me.cirnoslab.smashsumo.listeners.MenuListener
import me.cirnoslab.smashsumo.listeners.PlayerMechanicListener
import me.cirnoslab.smashsumo.menu.actionitem.ActionItemManager
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

/**
 * Main entrypoint for plugin
 */
class SmashSumo : JavaPlugin() {
    override fun onEnable() {
        plugin = this
        server.pluginManager.registerEvents(PlayerMechanicListener(), this)
        server.pluginManager.registerEvents(GameListener(), this)
        server.pluginManager.registerEvents(ItemListener(), this)
        server.pluginManager.registerEvents(MenuListener(), this)
        HUDManager.SendHUD().runTaskTimer(this, 0L, 5L)

        val arenaCount = ArenaManager.init(dataFolder)

        ItemManager.init(this)
        ActionItemManager.init()

        val kitCount = KitManager.init(dataFolder)
        Config.init(this)
        log("Plugin enabled. Loaded config with $arenaCount arenas and $kitCount kits.")
    }

    override fun onDisable() {
        GameManager.games.forEach { game ->
            game.gamePlayers.values.forEach { gp ->
                gp.deinit()
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
        return ArenaCommands.handle(sender, args) ||
            ConfigCommands.handle(sender, args) ||
            KitCommands.handle(sender, args) ||
            DebugCommands.handle(sender, args) ||
            RootCommands.handle(sender, args)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String> {
        if (!command.name.equals("smashsumo", ignoreCase = true)) return listOf() // shouldn't be possible
        if (ArenaCommands.canComplete(args)) return ArenaCommands.complete(sender, args)
        if (ConfigCommands.canComplete(args)) return ConfigCommands.complete(args)
        if (KitCommands.canComplete(args)) return KitCommands.complete(args)
        if (DebugCommands.canComplete(args)) return DebugCommands.complete(args)
        return RootCommands.complete(sender, args)
    }

    companion object {
        /**
         * Logs text to the console with level [Level.INFO].
         *
         * @param text the String to log
         */
        fun log(text: String) {
            plugin.logger.log(Level.INFO, text)
        }

        /**
         * Logs text to the console with level [level].
         *
         * @param level the [Level] to log as
         * @param text the String to log
         */
        fun log(
            level: Level,
            text: String,
        ) {
            plugin.logger.log(level, text)
        }

        /**
         * The plugin instance
         */
        lateinit var plugin: SmashSumo

        /**
         * One long line on the scoreboard
         */
        val SCOREBOARD_LINE: String = "${ChatColor.WHITE}${ChatColor.STRIKETHROUGH}-------------------${ChatColor.RESET}"
    }
}
