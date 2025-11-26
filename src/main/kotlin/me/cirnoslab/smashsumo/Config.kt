package me.cirnoslab.smashsumo

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import me.cirnoslab.smashsumo.game.KnockbackConfig
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * Configuration singleton
 */
object Config {
    /**
     * The internal [YamlDocument] instance
     */
    lateinit var config: YamlDocument

    /**
     * Initializes data storage. Must be called before use of any other function.
     *
     * @param plugin the [JavaPlugin] of the main plugin
     * @throws java.io.IOException
     */
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

    /**
     * Reloads the internal storage.
     */
    fun reload() {
        config.reload()
    }

    /**
     * Saves the internal storage to disk.
     */
    fun save() {
        config.save()
    }

    /**
     * The lobby position to teleport players to when the game ends.
     */
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

    /**
     * The [GameMode] to set players to when the game ends
     */
    val lobbyGameMode: GameMode
        get() =
            try {
                GameMode.valueOf(config.getString("lobby-gamemode", "ADVENTURE"))
            } catch (_: IllegalArgumentException) {
                GameMode.ADVENTURE
            }

    /**
     * Whether to force an empty inventory before allowing players to enter
     */
    val forceEmptyInventory: Boolean
        get() = config.getBoolean("force-empty-inventory", true)

    /**
     * Whether to enable the kit selector or not
     */
    val enableKitSelector: Boolean
        get() = config.getBoolean("enable-kit-selector", true)

    /**
     * Whether to require players have a permission to use specific kits
     */
    val enforceKitPermissions: Boolean
        get() = config.getBoolean("enforce-kit-permissions", false)

    /**
     * Game settings
     *
     * @see me.cirnoslab.smashsumo.game.GameSettings
     */
    object Game {
        /**
         * The number of lives the player has in a game
         */
        val lives: Int
            get() = config.getInt("game.lives", 3)

        /**
         * Whether to allow block placement and breakage
         */
        val allowBlock: Boolean
            get() = config.getBoolean("game.allow-block", false)

        /**
         * The time a player has to wait to respawn (ticks)
         */
        val respawnTime: Long
            get() = (config.getLong("game.respawn-time", 1000) / 50).coerceAtLeast(2)

        /**
         * The time a player can stand on the respawn platform before it despawns (ticks)
         */
        val platformDespawnTime: Long
            get() = config.getLong("game.platform-despawn-time", 3000) / 50

        /**
         * The time items stay after being dropped by a player before despawning (ticks)
         */
        val itemDespawnTime: Long
            get() = config.getLong("game.item-despawn-time", 3000) / 50

        /**
         * The name of the kit all players will have by default, if this is null no kit will be given
         */
        val defaultKitName: String?
            get() = config.getString("game.default-kit", null)

        /**
         * The [KnockbackConfig] used for the game
         */
        val knockback: KnockbackConfig
            get() =
                KnockbackConfig(
                    config.getDouble("game.knockback.initial-y", 0.5),
                    config.getDouble("game.knockback.minimum-size", 1.1),
                    config.getDouble("game.knockback.xz-damage-multiplier", 0.033),
                    config.getDouble("game.knockback.y-damage-multiplier", 0.025),
                    config.getDouble("game.knockback.xz-momentum-multiplier", 1.0),
                    config.getDouble("game.knockback.y-momentum-multiplier", 0.9),
                    config.getInt("game.knockback.no-damage-ticks", 8),
                )
    }

    /**
     * Chat styling settings
     *
     * @see ChatColor
     */
    object Style {
        /**
         * Parses a [ChatColor] from an enum String.
         * Returns ChatColor.WHITE if color is invalid.
         *
         * @param colorString the String to parse
         * @return the ChatColor
         */
        private fun parseColor(colorString: String): ChatColor =
            try {
                ChatColor.valueOf(colorString)
            } catch (_: IllegalArgumentException) {
                ChatColor.WHITE
            }

        /**
         * Gets a [ChatColor] from the [key] in the config.
         * Returns [def] if key doesn't exist and ChatColor.WHITE if value is invalid.
         *
         * @param key the key to get the color from
         * @param def the color to default to
         * @return the ChatColor
         */
        private fun defaultColor(
            key: String,
            def: ChatColor,
        ): ChatColor {
            val color = config.getString(key) ?: return def
            return parseColor(color)
        }

        /**
         * Primary color
         */
        val P: ChatColor
            get() = defaultColor("style.primary-color", ChatColor.DARK_AQUA)

        /**
         * Secondary color
         */
        val S: ChatColor
            get() = defaultColor("style.secondary-color", ChatColor.AQUA)

        /**
         * Team colors
         */
        val teamColors: Array<ChatColor>
            get() {
                val colors = config.getStringList("style.team-colors")
                val colorList = colors.map { s -> parseColor(s) }.toTypedArray()
                return if (colorList.isNotEmpty()) colorList else arrayOf(ChatColor.WHITE)
            }
    }
}
