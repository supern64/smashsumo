package me.cirnoslab.smashsumo

import me.cirnoslab.smashsumo.SmashSumo.Companion.P
import me.cirnoslab.smashsumo.SmashSumo.Companion.S
import me.cirnoslab.smashsumo.arena.Arena
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import kotlin.math.sqrt

object Utils {
    // n(umber) t(o) r(ange) c(apped)
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

    fun l2s(l: Location): String = "${l.world.name}:${l.x}:${l.y}:${l.z}"

    fun s2l(s: String): Location {
        val parts = s.split(":")
        require(parts.size == 4)
        val world = SmashSumo.plugin.server.getWorld(parts[0])
        val x = parts[1].toDouble()
        val y = parts[2].toDouble()
        val z = parts[3].toDouble()
        return Location(world, x, y, z)
    }

    // lat(eral) d(istance) s(quared)
    fun latDS(
        l1: Location,
        l2: Location,
    ): Double {
        val dx = l1.x - l2.x
        val dz = l1.z - l2.z
        return dx * dx + dz * dz
    }

    fun latD(
        l1: Location,
        l2: Location,
    ): Double = sqrt(latDS(l1, l2))

    fun arenaInfo(arena: Arena.Builder) =
        """${P}Arena Info:
        |${P}Name: ${S}${arena.name}
        |${P}Center: ${S}${arena.center.world?.name} (${String.format(
            "%.2f",
            arena.center.x,
        )}, ${String.format("%.2f", arena.center.y)}, ${String.format("%.2f", arena.center.z)})
        |${P}Spawn Radius: ${S}${String.format("%.2f", arena.spawnRadius)}
        |${P}Side Barrier Radius: ${S}${String.format("%.2f", arena.sideRadius)}
        |${P}Respawn Height: ${S}${arena.respawnHeight}
        |${P}Bottom Barrier: ${S}Y${String.format("%.2f", arena.bottomBarrier)}
        |${P}Top Barrier: ${if (arena.topBarrier != null) "${S}Y${String.format("%.2f", arena.topBarrier!!)}" else S + "None"}
        """.trimMargin()

    fun arenaInfo(arena: Arena) = arenaInfo(Arena.Builder(arena))

    fun String.mm() = MiniMessage.miniMessage().deserialize(this)
}
