package me.cirnoslab.smashsumo.arena

import me.cirnoslab.smashsumo.Utils
import org.bukkit.Location
import kotlin.math.cos
import kotlin.math.sin

class Arena(
    val name: String,
    val center: Location,
    val spawnRadius: Double,
    val bottomBarrier: Double,
    val sideRadius: Double,
    val respawnHeight: Int,
    val topBarrier: Double? = null,
) {
    var state: ArenaState = ArenaState.AVAILABLE

    fun inArena(location: Location): Boolean {
        if (location.world != center.world) return false
        if (location.y < bottomBarrier) return false
        if (topBarrier != null && location.y > topBarrier) return false
        // don't calculate Y axis
        if (Utils.latDS(location, center) > sideRadius * sideRadius) return false
        return true
    }

    fun getSpawnLocations(playerCount: Int): List<Location> {
        val locations = mutableListOf<Location>()
        val angleIncrement = 2 * Math.PI / playerCount
        for (i in 0 until playerCount) {
            val angle = i * angleIncrement
            val x = center.x + spawnRadius * cos(angle)
            val z = center.z + spawnRadius * sin(angle)
            val y = center.y
            // 90 - Math.toDegrees(angle) + 360
            var yawAngle = (450 - Math.toDegrees(angle)) % 360
            if (yawAngle > 180) yawAngle -= 360
            locations.add(Location(center.world, x, y, z, yawAngle.toFloat(), 0f))
        }
        return locations
    }

    fun getRespawnPoint(n: Int): Location {
        if (n == 0) return Location(center.world, center.blockX.toDouble(), respawnHeight.toDouble(), center.blockZ.toDouble())
        val respawnRadius = spawnRadius / 1.5
        val angle = 2 * Math.PI / 6 * (n - 1) // surely no more than 7 players would die at once right?
        return Location(
            center.world,
            center.x + respawnRadius * cos(angle),
            respawnHeight.toDouble(),
            center.z + respawnRadius * sin(angle),
        )
    }

    data class Builder(
        var name: String,
        var center: Location,
        var spawnRadius: Double = 12.0,
        var bottomBarrier: Double = 0.0,
        var sideRadius: Double = 20.0,
        var respawnHeight: Int = center.blockY + 3,
        var topBarrier: Double? = null,
    ) {
        constructor(arena: Arena) : this(
            arena.name,
            arena.center,
            arena.spawnRadius,
            arena.bottomBarrier,
            arena.sideRadius,
            arena.respawnHeight,
            arena.topBarrier,
        )

        fun checkArena(): List<ArenaInvalidCause> {
            val cause = mutableListOf<ArenaInvalidCause>()
            if (spawnRadius >= sideRadius) cause.add(ArenaInvalidCause.INVALID_SPAWN_RADIUS)
            if (topBarrier != null) {
                if (topBarrier!! <= bottomBarrier ||
                    topBarrier!! - center.y < MINIMUM_TOP_BARRIER_HEIGHT
                ) {
                    cause.add(ArenaInvalidCause.INVALID_TOP_BARRIER)
                }
                if (topBarrier!! <= respawnHeight + MINIMUM_RESPAWN_HEIGHT) cause.add(ArenaInvalidCause.INVALID_RESPAWN_HEIGHT)
            }

            return cause
        }

        fun build() = Arena(name, center, spawnRadius, bottomBarrier, sideRadius, respawnHeight, topBarrier)

        enum class ArenaInvalidCause(
            val description: String,
        ) {
            INVALID_SPAWN_RADIUS("Spawn radius must be less than side radius."),
            INVALID_TOP_BARRIER("Top barrier must be above the bottom barrier, and must be at least 8 blocks above the center."),
            INVALID_RESPAWN_HEIGHT("Respawn height must be lower than top barrier by 3 blocks."),
        }
    }

    enum class ArenaState {
        AVAILABLE,
        WAITING, // or ending
        PLAYING,
    }

    companion object {
        const val MINIMUM_TOP_BARRIER_HEIGHT = 8
        const val MINIMUM_RESPAWN_HEIGHT = 3
    }
}
