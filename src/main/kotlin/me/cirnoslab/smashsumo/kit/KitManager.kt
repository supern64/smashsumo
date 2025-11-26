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
import java.util.logging.Level

/**
 * Kit manager singleton
 */
object KitManager {
    /**
     * A map of all kit names and their [Kit]
     */
    val kits
        get() = pluginKits + userKits

    /**
     * A map of kits registered by other plugins
     */
    private val pluginKits: MutableMap<String, Kit> = mutableMapOf()

    /**
     * A map of all kits loaded by the user
     */
    val userKits: MutableMap<String, Kit> = mutableMapOf()

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
     * @return the number of kits loaded from files
     */
    fun reload(): Int {
        kitF.reload()
        return loadKitList()
    }

    /**
     * Parses the kit list from internal storage.
     *
     * @return the number of kits loaded from files
     */
    @Suppress("UNCHECKED_CAST")
    fun loadKitList(): Int {
        userKits.clear()
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
            userKits[name] = kit
        }
        return userKits.size
    }

    /**
     * Saves the internal storage to disk.
     */
    fun saveKits() {
        val kitList = mutableListOf<Map<String, Any>>()
        userKits.values.forEach { kit ->
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

    /**
     * Registers a kit (via plugin)
     *
     * @param kit the kit to register
     * @return true if the kit was registered, false if registration failed
     */
    fun registerKit(kit: Kit): Boolean {
        if (pluginKits.containsKey(kit.name)) {
            SmashSumo.log(Level.WARNING, "Kit ${kit.name} was registered twice. The correct kit may not show up.")
            return false
        }
        pluginKits[kit.name] = kit
        return true
    }
}
