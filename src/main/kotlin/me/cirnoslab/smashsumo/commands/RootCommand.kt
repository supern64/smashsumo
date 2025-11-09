package me.cirnoslab.smashsumo.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.cirnoslab.smashsumo.SmashSumo.Companion.P
import me.cirnoslab.smashsumo.SmashSumo.Companion.S
import me.cirnoslab.smashsumo.arena.Arena
import me.cirnoslab.smashsumo.arena.ArenaManager
import me.cirnoslab.smashsumo.game.Game
import me.cirnoslab.smashsumo.game.GameManager
import org.bukkit.entity.Player

// root command + game
object RootCommand {
    fun get(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands
            .literal("smashsumo")
            .then(
                Commands
                    .literal("join")
                    .requires { sender -> sender.executor is Player }
                    .then(
                        Commands
                            .argument("name", StringArgumentType.word())
                            .suggests { _, builder ->
                                ArenaManager.arenas.forEach { (arenaName, arena) ->
                                    if (arena.state != Arena.ArenaState.PLAYING) builder.suggest(arenaName)
                                }
                                builder.buildFuture()
                            }.executes { ctx ->
                                val arenaName = StringArgumentType.getString(ctx, "name")
                                val p = ctx.source.executor!! as Player
                                if (!ArenaManager.arenas.containsKey(arenaName)) {
                                    ctx.source.executor!!.sendRichMessage("${P}Arena ${S}$arenaName ${P}does not exist.")
                                    return@executes -1
                                }
                                val success = GameManager.join(p, ArenaManager.arenas[arenaName]!!)
                                when (success) {
                                    GameManager.GameJoinResult.SUCCESS -> {}
                                    GameManager.GameJoinResult.ALREADY_IN_GAME -> {
                                        p.sendRichMessage("${P}Failed to join game. You are already in a game.")
                                    }
                                    GameManager.GameJoinResult.GAME_STARTED -> {
                                        p.sendRichMessage("${P}The game has already started. You have been joined as a spectator.")
                                    }
                                    GameManager.GameJoinResult.GAME_ENDING -> {
                                        p.sendRichMessage("${P}The game is ending.")
                                    }
                                }
                                Command.SINGLE_SUCCESS
                            },
                    ),
            ).then(
                Commands
                    .literal("leave")
                    .requires { sender -> sender.executor is Player && GameManager.isPlayerInGame(sender.executor as Player) }
                    .executes { ctx ->
                        val p = ctx.source.executor as Player
                        GameManager.leave(p)
                        // should be always in game
                        p.sendRichMessage("${P}You have left the game.")
                        Command.SINGLE_SUCCESS
                    },
            ).then(
                Commands
                    .literal("start")
                    .requires { sender ->
                        sender.executor is Player &&
                            sender.sender.hasPermission("smashsumo.gm") &&
                            GameManager.isPlayerInGame(sender.executor as Player)
                    }.executes { ctx ->
                        val p = ctx.source.executor as Player
                        val game = GameManager.getGame(p)!!
                        if (game.state != Game.GameState.WAITING) {
                            p.sendRichMessage("${P}The game has already started.")
                            return@executes -1
                        }
                        if (game.gamePlayers.size < 2) {
                            p.sendRichMessage("${P}At least 2 players are required to start the game.")
                            return@executes -1
                        }
                        game.initStart()
                        Command.SINGLE_SUCCESS
                    },
            ).then(ArenaCommands.get())
            .then(ConfigCommands.get())
    }
}
