package me.cirnoslab.smashsumo.game

/**
 * Modifiable knockback and damage settings
 *
 * @see KnockbackConfig
 * @property damage the amount of damage (%) dealt
 */
data class HitValue(
    var initialY: Double,
    var minimumSize: Double,
    var xzDamageMultiplier: Double,
    var yDamageMultiplier: Double,
    var xzMomentumMultiplier: Double,
    var yMomentumMultiplier: Double,
    var noDamageTicks: Int,
    var damage: Double,
) {
    constructor(e: KnockbackConfig, damage: Double) : this(
        e.initialY,
        e.minimumSize,
        e.xzDamageMultiplier,
        e.yDamageMultiplier,
        e.xzMomentumMultiplier,
        e.yMomentumMultiplier,
        e.noDamageTicks,
        damage,
    )
}
