package me.cirnoslab.smashsumo

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import me.cirnoslab.smashsumo.arena.ArenaManager
import me.cirnoslab.smashsumo.commands.ArenaCommands
import me.cirnoslab.smashsumo.commands.ConfigCommands
import me.cirnoslab.smashsumo.commands.GameCommands
import me.cirnoslab.smashsumo.game.HUDManager
import me.cirnoslab.smashsumo.listeners.GameListener
import me.cirnoslab.smashsumo.listeners.PlayerMechanicListener
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class SmashSumo : JavaPlugin() {
    override fun onEnable() {
        plugin = this
        server.pluginManager.registerEvents(PlayerMechanicListener(), this)
        server.pluginManager.registerEvents(GameListener(), this)
        HUDManager.SendHUD().runTaskTimer(this, 0L, 5L)

        val arenaCount = ArenaManager.init(dataFolder)
        Companion.config = YamlDocument.create(
            File(dataFolder, "config.yml"), getResource("config.yml"),
            UpdaterSettings.builder().setVersioning(BasicVersioning("version")).build(),
            LoaderSettings.builder().setAutoUpdate(true).build(),
            DumperSettings.DEFAULT
        )
        log("Plugin enabled. Loaded config and $arenaCount arenas.")
    }

    override fun onDisable() {
        log("Plugin disabled.")
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!command.name.equals("smashsumo", ignoreCase = true)) return false
        return ArenaCommands.handle(sender, args) || GameCommands.handle(sender, args) || ConfigCommands.handle(sender, args)
    }

    companion object {
        fun log(text: String) {
            Bukkit.getConsoleSender().sendMessage("[SmashSumo] $text")
        }
        lateinit var plugin: SmashSumo
        lateinit var config: YamlDocument

        val P = "${ChatColor.DARK_AQUA}"
        val S = "${ChatColor.AQUA}"
        const val MAX_LIVES = 3
        val playerColor = listOf(ChatColor.RED, ChatColor.BLUE, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.AQUA, ChatColor.WHITE, ChatColor.LIGHT_PURPLE, ChatColor.GRAY)
        val SCOREBOARD_LINE: String = "${ChatColor.WHITE}${ChatColor.STRIKETHROUGH}-------------------${ChatColor.RESET}"
    }
}