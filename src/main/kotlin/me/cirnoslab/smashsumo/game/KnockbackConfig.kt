package me.cirnoslab.smashsumo.game

/**
 * Knockback settings
 */
data class KnockbackConfig(
    /**
     * Base vertical knockback
     */
    val initialY: Double,
    /**
     * Minimum knockback size
     */
    val minimumSize: Double,
    /**
     * Horizontal damage knockback multiplier
     */
    val xzDamageMultiplier: Double,
    /**
     * Vertical damage knockback multiplier
     */
    val yDamageMultiplier: Double,
    /**
     * Horizontal momentum knockback multiplier
     */
    val xzMomentumMultiplier: Double,
    /**
     * Vertical momentum knockback multiplier
     */
    val yMomentumMultiplier: Double,
    /**
     * Number of ticks required between hits
     */
    val noDamageTicks: Int,
)
