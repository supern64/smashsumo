package me.cirnoslab.smashsumo.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.cirnoslab.smashsumo.SmashSumo.Companion.P
import me.cirnoslab.smashsumo.SmashSumo.Companion.S
import me.cirnoslab.smashsumo.Utils
import me.cirnoslab.smashsumo.arena.Arena
import me.cirnoslab.smashsumo.arena.ArenaManager
import org.bukkit.entity.Player

object ArenaCommands {
    private var selectedArena: Arena.Builder? = null

    fun get(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands
            .literal("arena")
            .requires { sender -> sender.executor is Player && sender.sender.hasPermission("smashsumo.admin") }
            .then(
                Commands
                    .literal("select")
                    .then(
                        Commands
                            .argument("name", StringArgumentType.word())
                            .suggests { _, builder ->
                                ArenaManager.arenas.keys.forEach { arenaName ->
                                    builder.suggest(arenaName)
                                }
                                builder.buildFuture()
                            }.executes { ctx ->
                                val p = ctx.source.executor as Player
                                val arenaName = StringArgumentType.getString(ctx, "name")
                                if (!ArenaManager.arenas.containsKey(arenaName)) {
                                    p.sendRichMessage("${P}Arena ${S}$arenaName ${P}does not exist.")
                                    return@executes -1
                                }
                                selectedArena = Arena.Builder(ArenaManager.arenas[arenaName]!!)
                                p.sendRichMessage("${P}Arena ${S}$arenaName ${P}has been selected.")
                                p.updateCommands()
                                Command.SINGLE_SUCCESS
                            },
                    ),
            ).then(
                Commands
                    .literal("create")
                    .then(
                        Commands
                            .argument("name", StringArgumentType.word())
                            .executes { ctx ->
                                val p = ctx.source.executor as Player
                                val arenaName = StringArgumentType.getString(ctx, "name")
                                if (ArenaManager.arenas.containsKey(arenaName)) {
                                    p.sendRichMessage("${P}Arena ${S}$arenaName ${P}already exists.")
                                    return@executes -1
                                }
                                selectedArena = Arena.Builder(arenaName, p.location)
                                p.sendRichMessage("${P}Arena ${S}$arenaName ${P}created and selected.")
                                p.updateCommands()
                                Command.SINGLE_SUCCESS
                            },
                    ),
            ).then(
                Commands
                    .literal("center")
                    .requires { selectedArena != null }
                    .executes { ctx ->
                        selectedArena!!.center = ctx.source.executor!!.location
                        ctx.source.executor!!.sendRichMessage(
                            "${P}Arena ${S}${selectedArena!!.name}'s ${P}center has been set at your current location.",
                        )
                        Command.SINGLE_SUCCESS
                    },
            ).then(
                Commands
                    .literal("spawn")
                    .requires { selectedArena != null }
                    .executes { ctx ->
                        selectedArena!!.spawnRadius = Utils.latD(selectedArena!!.center, ctx.source.executor!!.location)
                        ctx.source.executor!!.sendRichMessage(
                            "${P}Arena ${S}${selectedArena!!.name}'s ${P}spawn radius has been set to ${S}${String.format(
                                "%.2f",
                                selectedArena!!.spawnRadius,
                            )}$P.",
                        )
                        Command.SINGLE_SUCCESS
                    },
            ).then(
                Commands
                    .literal("side")
                    .requires { selectedArena != null }
                    .executes { ctx ->
                        selectedArena!!.sideRadius = Utils.latD(selectedArena!!.center, ctx.source.executor!!.location)
                        ctx.source.executor!!.sendRichMessage(
                            "${P}Arena ${S}${selectedArena!!.name}'s ${P}side barrier radius has been set to ${S}${String.format(
                                "%.2f",
                                selectedArena!!.sideRadius,
                            )}$P.",
                        )
                        Command.SINGLE_SUCCESS
                    },
            ).then(
                Commands
                    .literal("bottom")
                    .requires { selectedArena != null }
                    .executes { ctx ->
                        selectedArena!!.bottomBarrier =
                            ctx.source.executor!!
                                .location.y
                        ctx.source.executor!!.sendRichMessage(
                            "${P}Arena ${S}${selectedArena!!.name}'s ${P}bottom barrier has been set to Y${S}${String.format(
                                "%.2f",
                                selectedArena!!.bottomBarrier,
                            )}$P.",
                        )
                        Command.SINGLE_SUCCESS
                    },
            ).then(
                Commands
                    .literal("top")
                    .requires { selectedArena != null }
                    .executes { ctx ->
                        selectedArena!!.topBarrier =
                            ctx.source.executor!!
                                .location.y
                        ctx.source.executor!!.sendRichMessage(
                            "${P}Arena ${S}${selectedArena!!.name}'s ${P}bottom barrier has been set to Y${S}${String.format(
                                "%.2f",
                                selectedArena!!.bottomBarrier,
                            )}$P.",
                        )
                        Command.SINGLE_SUCCESS
                    },
            ).then(
                Commands
                    .literal("respawn")
                    .requires { selectedArena != null }
                    .executes { ctx ->
                        selectedArena!!.respawnHeight =
                            ctx.source.executor!!
                                .location.blockY
                        ctx.source.executor!!.sendRichMessage(
                            "${P}Arena ${S}${selectedArena!!.name}'s ${P}respawn height has been set to Y${S}${selectedArena!!.respawnHeight}$P.",
                        )
                        Command.SINGLE_SUCCESS
                    },
            ).then(
                Commands
                    .literal("save")
                    .requires { selectedArena != null }
                    .executes { ctx ->
                        val arenaCheck = selectedArena!!.checkArena()
                        if (!arenaCheck.isEmpty()) {
                            ctx.source.executor!!.sendRichMessage(
                                "${P}Invalid arena.${S}\n${arenaCheck.joinToString("\n") { a -> a.description }}}",
                            )
                            return@executes -1
                        }
                        ArenaManager.arenas[selectedArena!!.name] = selectedArena!!.build()
                        ArenaManager.saveArenas()
                        ctx.source.executor!!.sendRichMessage("${P}Arenas saved.")
                        Command.SINGLE_SUCCESS
                    },
            ).then(
                Commands
                    .literal("info")
                    .then(
                        Commands
                            .argument("name", StringArgumentType.word())
                            .suggests { _, builder ->
                                ArenaManager.arenas.keys.forEach { arenaName ->
                                    builder.suggest(arenaName)
                                }
                                builder.buildFuture()
                            }.executes { ctx ->
                                val arenaName = StringArgumentType.getString(ctx, "name")
                                if (!ArenaManager.arenas.containsKey(arenaName)) {
                                    ctx.source.executor!!.sendRichMessage("${P}Arena ${S}$arenaName ${P}does not exist.")
                                    return@executes -1
                                }
                                ctx.source.executor!!.sendRichMessage(Utils.arenaInfo(ArenaManager.arenas[arenaName]!!))
                                Command.SINGLE_SUCCESS
                            },
                    ).requires { selectedArena != null }
                    .executes { ctx ->
                        ctx.source.executor!!.sendRichMessage(Utils.arenaInfo(selectedArena!!))
                        Command.SINGLE_SUCCESS
                    },
            ).then(
                Commands
                    .literal("list")
                    .executes { ctx ->
                        ctx.source.executor!!.sendRichMessage("${P}Available arenas: ${S}${ArenaManager.arenas.keys.joinToString(", ")}")
                        Command.SINGLE_SUCCESS
                    },
            )
    }
}
