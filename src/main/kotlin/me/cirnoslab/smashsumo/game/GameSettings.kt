package me.cirnoslab.smashsumo.game

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
     * The [KnockbackConfig] used for the game
     */
    val knockback: KnockbackConfig,
)
