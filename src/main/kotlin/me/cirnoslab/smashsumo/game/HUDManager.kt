package me.cirnoslab.smashsumo.game

import io.github.theluca98.textapi.ActionBar
import org.bukkit.scheduler.BukkitRunnable

object HUDManager {
    class SendHUD : BukkitRunnable() {
        override fun run() {
            GameManager.games.forEach { game ->
                game.gamePlayers.values.forEach { gp ->
                    ActionBar(gp.actionBarDisplay).send(gp.player)
                    gp.board.updateLines(gp.scoreboardDisplay)
                }
            }
        }
    }
}