package me.cirnoslab.smashsumo.commands

import me.cirnoslab.smashsumo.Config
import me.cirnoslab.smashsumo.Config.Style.P
import me.cirnoslab.smashsumo.Config.Style.S
import me.cirnoslab.smashsumo.arena.ArenaManager
import me.cirnoslab.smashsumo.game.Game
import me.cirnoslab.smashsumo.game.GameManager
import org.bukkit.command.CommandSender

object GameCommands {
    fun handle(
        s: CommandSender,
        args: Array<out String>,
    ): Boolean {
        if (args.isEmpty() || !listOf("join", "leave", "start").contains(args[0].lowercase())) return false

        if (s !is org.bukkit.entity.Player) {
            s.sendMessage("${P}Only players can use game commands.")
            return true
        }

        when (args[0].lowercase()) {
            "join" -> {
                if (args.size < 2) {
                    s.sendMessage("${P}Usage: $S/smashsumo join [arena]")
                    return true
                }

                val arena = ArenaManager.arenas[args[1]]
                if (arena == null) {
                    s.sendMessage("${P}Arena ${S}${args[1]} ${P}does not exist.")
                    return true
                }

                if (Config.forceEmptyInventory &&
                    (s.inventory.contents.any { a -> a != null } || s.inventory.armorContents.any { a -> a != null })
                ) {
                    s.sendMessage("${P}You must get rid of all of your items and armor before entering the game.")
                    return true
                }

                val success = GameManager.join(s, arena)
                when (success) {
                    GameManager.GameJoinResult.SUCCESS -> {}
                    GameManager.GameJoinResult.ALREADY_IN_GAME -> {
                        s.sendMessage("${P}Failed to join game. You are already in a game.")
                    }
                    GameManager.GameJoinResult.GAME_STARTED -> {
                        s.sendMessage("${P}The game has already started. You have been joined as a spectator.")
                    }
                    GameManager.GameJoinResult.GAME_ENDING -> {
                        s.sendMessage("${P}The game is ending.")
                    }
                }
                return true
            }
            "leave" -> {
                val result = GameManager.leave(s)
                if (result == GameManager.GameLeaveResult.NOT_IN_GAME) {
                    s.sendMessage("${P}You are not in a game.")
                    return true
                }
                s.sendMessage("${P}You have left the game.")
                return true
            }
            "start" -> {
                if (!s.hasPermission("smashsumo.gm")) {
                    s.sendMessage("${P}You do not have permission to use this command.")
                    return true
                }
                val game = GameManager.getGame(s)
                if (game == null) {
                    s.sendMessage("${P}You are not in a game.")
                    return true
                }
                if (game.state != Game.GameState.WAITING) {
                    s.sendMessage("${P}The game has already started.")
                    return true
                }
                if (game.gamePlayers.size < 2 && (args.size < 2 || args[1].lowercase() != "force")) {
                    s.sendMessage("${P}At least 2 players are required to start the game.")
                    return true
                }
                game.initStart()
                return true
            }
            else -> return false
        }
    }
}
