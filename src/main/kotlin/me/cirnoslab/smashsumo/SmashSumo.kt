package me.cirnoslab.smashsumo

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import me.cirnoslab.smashsumo.arena.ArenaManager
import me.cirnoslab.smashsumo.commands.RootCommand
import me.cirnoslab.smashsumo.game.HUDManager
import me.cirnoslab.smashsumo.listeners.GameListener
import me.cirnoslab.smashsumo.listeners.PlayerMechanicListener
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Level

class SmashSumo : JavaPlugin() {
    override fun onEnable() {
        plugin = this
        server.pluginManager.registerEvents(PlayerMechanicListener(), this)
        server.pluginManager.registerEvents(GameListener(), this)
        HUDManager.SendHUD().runTaskTimer(this, 0L, 5L)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            commands.registrar().register(RootCommand.get().build())
        }

        val arenaCount = ArenaManager.init(dataFolder)
        Companion.config =
            YamlDocument.create(
                File(dataFolder, "config.yml"),
                getResource("config.yml")!!,
                UpdaterSettings.builder().setVersioning(BasicVersioning("version")).build(),
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
            )
        log("Plugin enabled. Loaded config and $arenaCount arenas.")
    }

    override fun onDisable() {
        log("Plugin disabled.")
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
        lateinit var config: YamlDocument

        const val P = "<dark_aqua>"
        const val S = "<aqua>"
        const val MAX_LIVES = 3
        val playerColor =
            listOf(
                NamedTextColor.RED,
                NamedTextColor.BLUE,
                NamedTextColor.YELLOW,
                NamedTextColor.GREEN,
                NamedTextColor.AQUA,
                NamedTextColor.WHITE,
                NamedTextColor.LIGHT_PURPLE,
                NamedTextColor.GRAY,
            )
        val SCOREBOARD_LINE = text("                      ").decorate(TextDecoration.STRIKETHROUGH)
    }
}
