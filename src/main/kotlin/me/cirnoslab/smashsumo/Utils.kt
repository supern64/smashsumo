package me.cirnoslab.smashsumo

import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools
import net.minecraft.server.v1_8_R3.NBTTagCompound
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64
import kotlin.math.sqrt

/**
 * Utility functions
 */
object Utils {
    /**
     * Maps a number from one range to another, with a cap on each end.
     *
     * @param i the value to map
     * @param iMin the original range's minimum value
     * @param iMax the original range's maximum value
     * @param oMin the new range's minimum value
     * @param oMax the new range's maximum value
     * @return the mapped value
     */
    fun ntrc(
        i: Double,
        iMin: Double,
        iMax: Double,
        oMin: Double,
        oMax: Double,
    ): Double {
        if (iMax == iMin) return oMin
        val normalizedValue = (i - iMin) / (iMax - iMin)
        return (oMin + (oMax - oMin) * normalizedValue).coerceIn(minOf(oMin, oMax), maxOf(oMin, oMax))
    }

    /**
     * Serializes a [Location] into a short string format.
     *
     * @param l the Location to serialize
     * @return the serialized Location
     */
    fun l2s(l: Location): String = "${l.world.name}:${l.x}:${l.y}:${l.z}"

    /**
     * Deserializes a String into a [Location]
     *
     * @param s the serialized location
     * @return the deserialized Location
     * @throws IllegalArgumentException the format doesn't contain 4 parts
     */
    fun s2l(s: String): Location {
        val parts = s.split(":")
        require(parts.size == 4)
        val world = SmashSumo.plugin.server.getWorld(parts[0])
        val x = parts[1].toDouble()
        val y = parts[2].toDouble()
        val z = parts[3].toDouble()
        return Location(world, x, y, z)
    }

    /**
     * Gets the lateral (without considering Y axis) distance of 2 points squared.
     *
     * @param l1 the 1st point
     * @param l2 the 2nd point
     * @return the distance squared
     */
    fun latDS(
        l1: Location,
        l2: Location,
    ): Double {
        val dx = l1.x - l2.x
        val dz = l1.z - l2.z
        return dx * dx + dz * dz
    }

    /**
     * Gets the lateral (without considering Y axis) distance of 2 points.
     *
     * @param l1 the 1st point
     * @param l2 the 2nd point
     * @return the distance
     */
    fun latD(
        l1: Location,
        l2: Location,
    ): Double = sqrt(latDS(l1, l2))

    /**
     * Sets absorption hearts through NMS.
     *
     * @receiver the Player
     * @param ah the absorption hearts
     */
    fun Player.setAbsorptionHearts(ah: Float) {
        (this as CraftPlayer).handle.absorptionHearts = ah
    }

    /**
     * Clears the entire inventory, including armor.
     *
     * @receiver the Player
     */
    fun Player.clearInventory() {
        this.inventory.clear()
        this.inventory.helmet = null
        this.inventory.chestplate = null
        this.inventory.leggings = null
        this.inventory.boots = null
    }

    /**
     * Checks how long [s1] and [s2] match from the start.
     *
     * @param s1 the 1st String
     * @param s2 the 2nd String
     * @return the match count
     */
    fun matchPrefixCount(
        s1: String,
        s2: String,
    ): Int {
        var count = 0
        val minLength = minOf(s1.length, s2.length)

        for (i in 0 until minLength) {
            if (s1[i] == s2[i]) {
                count++
            } else {
                break
            }
        }
        return count
    }

    /**
     * Serializes this ItemStack's NBT data into Base64.
     *
     * @receiver the ItemStack
     * @return the Base64 string
     */
    fun ItemStack.toBase64(): String? {
        val nmsIS = CraftItemStack.asNMSCopy(this) ?: return null
        val outputStream = ByteArrayOutputStream()
        val outputTag = NBTTagCompound()

        nmsIS.save(outputTag)
        NBTCompressedStreamTools.a(outputTag, outputStream)
        return Base64.encode(outputStream.toByteArray())
    }

    /**
     * Deserializes an ItemStack's NBT data.
     *
     * @param s the Base64 string to deserialize
     * @return the ItemStack
     */
    fun deserializeItemStack(s: String): ItemStack {
        val inputBA = Base64.decode(s)
        val inputStream = ByteArrayInputStream(inputBA)

        val nbt = NBTCompressedStreamTools.a(inputStream)
        return CraftItemStack.asBukkitCopy(
            net.minecraft.server.v1_8_R3.ItemStack
                .createStack(nbt),
        )
    }
}
