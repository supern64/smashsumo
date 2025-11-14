package me.cirnoslab.smashsumo

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

object Config {
    lateinit var config: YamlDocument

    fun init(plugin: JavaPlugin) {
        config =
            YamlDocument.create(
                File(plugin.dataFolder, "config.yml"),
                plugin.getResource("config.yml"),
                UpdaterSettings.builder().setVersioning(BasicVersioning("version")).build(),
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
            )
    }

    fun reload() {
        config.reload()
    }

    fun save() {
        config.save()
    }

    var lobbyPosition: Location?
        get() {
            if (config.getString("lobby") != null) {
                return Utils.s2l(config.getString("lobby"))
            }
            return null
        }
        set(v) {
            if (v == null) {
                config.set("lobby", null)
            } else {
                config.set("lobby", Utils.l2s(v))
            }
        }

    val forceEmptyInventory: Boolean
        get() = config.getBoolean("force-empty-inventory", false)

    object Game {
        val lives: Int
            get() = config.getInt("game.lives", 3)
        val allowBlock: Boolean
            get() = config.getBoolean("game.allow-block", false)
    }

    object Style {
        // default color (white if invalid)
        private fun parseColor(colorString: String): ChatColor =
            try {
                ChatColor.valueOf(colorString)
            } catch (_: IllegalArgumentException) {
                ChatColor.WHITE
            }

        private fun defaultColor(
            key: String,
            def: ChatColor,
        ): ChatColor {
            val color = config.getString(key) ?: return def
            return parseColor(color)
        }

        // shortened here to not be annoying to use in text
        // primary color
        val P: ChatColor
            get() = defaultColor("style.primary-color", ChatColor.DARK_AQUA)

        // secondary color
        val S: ChatColor
            get() = defaultColor("style.secondary-color", ChatColor.AQUA)

        val teamColors: Array<ChatColor>
            get() {
                val colors = config.getStringList("style.team-colors")
                val colorList = colors.map { s -> parseColor(s) }.toTypedArray()
                return if (colorList.isNotEmpty()) colorList else arrayOf(ChatColor.WHITE)
            }
    }
}
