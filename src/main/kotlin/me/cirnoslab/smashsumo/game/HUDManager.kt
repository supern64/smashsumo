package me.cirnoslab.smashsumo.game

import org.bukkit.scheduler.BukkitRunnable

object HUDManager {
    class SendHUD : BukkitRunnable() {
        override fun run() {
            GameManager.games.forEach { game ->
                game.gamePlayers.values.forEach { gp ->
                    gp.player.sendActionBar(gp.actionBarDisplay)
                    gp.board.updateLines(gp.scoreboardDisplay)
                }
            }
        }
    }
}
