package me.cirnoslab.smashsumo.arena

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import me.cirnoslab.smashsumo.SmashSumo
import me.cirnoslab.smashsumo.Utils
import java.io.File

/**
 * Arena management singleton
 */
object ArenaManager {
    /**
     * A map of arena names and their [Arena]
     */
    val arenas: MutableMap<String, Arena> = mutableMapOf()
    private lateinit var arenaF: YamlDocument

    /**
     * Initializes arena storage. Must be called before use of any other function.
     *
     * @param dataFolder the data folder for the plugin.
     * @return the number of arenas loaded
     * @throws java.io.IOException
     */
    fun init(dataFolder: File): Int {
        arenaF =
            YamlDocument.create(
                File(dataFolder, "arenas.yml"),
                SmashSumo.plugin.getResource("arenas.yml"),
                UpdaterSettings.builder().setVersioning(BasicVersioning("version")).build(),
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
            )
        return loadArenaList()
    }

    /**
     * Reloads the internal storage and arena list.
     *
     * @return the number of arenas loaded
     */
    fun reload(): Int {
        arenaF.reload()
        return loadArenaList()
    }

    /**
     * Parses the arena list from internal storage.
     *
     * @return the number of arenas loaded
     */
    fun loadArenaList(): Int {
        arenas.clear()
        arenaF.getMapList("arenas").forEach { map ->
            val name = map["name"] as String
            val center = Utils.s2l(map["center"] as String)
            val spawnRadius = (map["spawnRadius"] as Number).toDouble()
            val bottomBarrier = (map["bottomBarrier"] as Number).toDouble()
            val sideRadius = (map["sideRadius"] as Number).toDouble()
            val respawnHeight = (map["respawnHeight"] as Number).toInt()
            val topBarrier = if (map.containsKey("topBarrier")) (map["topBarrier"] as Number).toDouble() else null
            val arena = Arena(name, center, spawnRadius, bottomBarrier, sideRadius, respawnHeight, topBarrier)
            arenas[name] = arena
        }
        return arenas.size
    }

    /**
     * Saves the internal storage to disk.
     */
    fun saveArenas() {
        val arenaList = mutableListOf<Map<String, Any>>()
        arenas.values.forEach { arena ->
            val map =
                mutableMapOf<String, Any>(
                    "name" to arena.name,
                    "center" to Utils.l2s(arena.center),
                    "spawnRadius" to arena.spawnRadius,
                    "bottomBarrier" to arena.bottomBarrier,
                    "sideRadius" to arena.sideRadius,
                    "respawnHeight" to arena.respawnHeight,
                )
            if (arena.topBarrier != null) {
                map["topBarrier"] = arena.topBarrier
            }
            arenaList.add(map)
        }
        arenaF.set("arenas", arenaList)
        arenaF.save()
    }
}
