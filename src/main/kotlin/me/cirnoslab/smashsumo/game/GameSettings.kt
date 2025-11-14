package me.cirnoslab.smashsumo.game

data class GameSettings(
    val lives: Int,
    val allowBlock: Boolean,
    val respawnTime: Long,
    val platformDespawnTime: Long,
    val knockback: KnockbackConfig,
)
