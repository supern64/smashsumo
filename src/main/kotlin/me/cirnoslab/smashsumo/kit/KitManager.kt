package me.cirnoslab.smashsumo.kit

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import me.cirnoslab.smashsumo.SmashSumo
import java.io.File
import kotlin.collections.forEach

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
    fun loadKitList(): Int {
        TODO("not implemented")
    }

    /**
     * Saves the internal storage to disk.
     */
    fun saveKits() {
        TODO("not implemented")
    }
}
