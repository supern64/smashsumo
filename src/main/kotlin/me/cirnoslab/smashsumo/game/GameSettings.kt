package me.cirnoslab.smashsumo.game

import me.cirnoslab.smashsumo.kit.Kit

/**
 * Game settings
*/
data class GameSettings(
    /**
     * The number of lives the player has in a game
     */
    val lives: Int,
    /**
     * Whether to allow block placement and breakage
     */
    val allowBlock: Boolean,
    /**
     * The time a player has to wait to respawn (ticks)
     */
    val respawnTime: Long,
    /**
     * The time a player can stand on the respawn platform before it despawns (ticks)
     */
    val platformDespawnTime: Long,
    /**
     * The time items stay after being dropped by a player before despawning (ticks)
     */
    val itemDespawnTime: Long,
    /**
     * The [KnockbackConfig] used for the game
     */
    val knockback: KnockbackConfig,
    /**
     * The [Kit] that is given out to everyone when the game starts
     */
    val defaultKit: Kit?,
)
