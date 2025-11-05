package me.cirnoslab.smashsumo

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import kotlin.math.roundToInt
import kotlin.math.sqrt

object Utils {
    // n(umber) t(o) r(ange) c(apped)
    fun ntrc(
        i: Double,
        iMin: Double,
        iMax: Double,
        oMin: Double,
        oMax: Double
    ): Double {
        if (iMax == iMin) return oMin
        val normalizedValue = (i - iMin) / (iMax - iMin)
        return (oMin + (oMax - oMin) * normalizedValue).coerceIn(minOf(oMin, oMax), maxOf(oMin, oMax))
    }

    fun l2s(l: Location) : String {
        return "${l.world.name}:${l.x}:${l.y}:${l.z}"
    }

    fun s2l(s: String) : Location {
        val parts = s.split(":")
        val world = SmashSumo.plugin.server.getWorld(parts[0])!!
        val x = parts[1].toDouble()
        val y = parts[2].toDouble()
        val z = parts[3].toDouble()
        return Location(world, x, y, z)
    }

    // lat(eral) d(istance) s(quared)
    fun latDS(l1: Location, l2: Location): Double {
        val dx = l1.x - l2.x
        val dz = l1.z - l2.z
        return dx * dx + dz * dz
    }

    fun latD(l1: Location, l2: Location): Double {
        return sqrt(latDS(l1, l2))
    }

    fun Player.getAbsorptionHearts(): Float {
        return (this as CraftPlayer).handle.absorptionHearts
    }

    fun Player.setAbsorptionHearts(ah: Float) {
        (this as CraftPlayer).handle.absorptionHearts = ah
    }
}