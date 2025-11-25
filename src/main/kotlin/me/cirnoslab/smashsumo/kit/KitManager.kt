package me.cirnoslab.smashsumo.kit

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import me.cirnoslab.smashsumo.SmashSumo
import me.cirnoslab.smashsumo.Utils
import me.cirnoslab.smashsumo.Utils.toBase64
import org.bukkit.Material
import java.io.File

/**
 * Kit manager singleton
 */
object KitManager {
    /**
     * A map of kit names and their [Kit]
     */
    val kits: MutableMap<String, Kit> = mutableMapOf()
    private lateinit var kitF: YamlDocument

    /**
     * Initializes kit storage. Must be called before use of any other function.
     *
     * @param dataFolder the data folder for the plugin.
     * @return the number of kits loaded
     * @throws java.io.IOException
     */
    fun init(dataFolder: File): Int {
        kitF =
            YamlDocument.create(
                File(dataFolder, "kits.yml"),
                SmashSumo.plugin.getResource("kits.yml"),
                UpdaterSettings.builder().setVersioning(BasicVersioning("version")).build(),
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
            )
        return loadKitList()
    }

    /**
     * Reloads the internal storage and kit list.
     *
     * @return the number of kits loaded
     */
    fun reload(): Int {
        kitF.reload()
        return loadKitList()
    }

    /**
     * Parses the kit list from internal storage.
     *
     * @return the number of kits loaded
     */
    @Suppress("UNCHECKED_CAST")
    fun loadKitList(): Int {
        kits.clear()
        kitF.getMapList("kits").forEach { map ->
            val name = map["name"] as String
            val icon = map["icon"] as String
            val mapItem = map["items"] as List<Map<String, *>>
            val items = mutableListOf<Kit.Item>()

            val matIcon = Material.getMaterial(icon) ?: Material.PAPER

            mapItem.forEach { r ->
                val slot = r["slot"] as Int
                val replenishOnDeath = r["replenishOnDeath"] as Boolean
                val nbt = r["nbt"] as String

                val item = Kit.Item(slot, Utils.deserializeItemStack(nbt), replenishOnDeath)
                items.add(item)
            }

            val kit = Kit(name, matIcon, items)
            kits[name] = kit
        }
        return kits.size
    }

    /**
     * Saves the internal storage to disk.
     */
    fun saveKits() {
        val kitList = mutableListOf<Map<String, Any>>()
        kits.values.forEach { kit ->
            val map = mutableMapOf<String, Any>()
            map["name"] = kit.name
            map["icon"] = kit.icon.name
            val mapItem = mutableListOf<Map<String, Any>>()

            kit.items.forEach { item ->
                mapItem.add(
                    mapOf(
                        "slot" to item.slot,
                        "replenishOnDeath" to item.replenishOnDeath,
                        "nbt" to item.get().toBase64()!!,
                    ),
                )
            }
            map["items"] = mapItem
            kitList.add(map)
        }
        kitF.set("kits", kitList)
        kitF.save()
    }
}
