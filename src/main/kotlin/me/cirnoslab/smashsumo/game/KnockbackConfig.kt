package me.cirnoslab.smashsumo.game

data class KnockbackConfig(
    val initialY: Double,
    val minimumSize: Double,
    val xzDamageMultiplier: Double,
    val yDamageMultiplier: Double,
    val xzMomentumMultiplier: Double,
    val yMomentumMultiplier: Double,
)
