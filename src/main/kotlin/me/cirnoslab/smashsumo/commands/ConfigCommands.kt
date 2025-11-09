package me.cirnoslab.smashsumo.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver
import me.cirnoslab.smashsumo.SmashSumo
import me.cirnoslab.smashsumo.SmashSumo.Companion.P
import me.cirnoslab.smashsumo.Utils
import me.cirnoslab.smashsumo.arena.ArenaManager
import org.bukkit.World
import org.bukkit.entity.Player

object ConfigCommands {
    fun get(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands
            .literal("config")
            .requires { sender -> sender.sender.hasPermission("smashsumo.admin") }
            .then(
                Commands
                    .literal("reload")
                    .executes { ctx ->
                        ArenaManager.reload()
                        SmashSumo.config.reload()
                        ctx.source.sender.sendRichMessage("${P}Configuration reloaded.")
                        Command.SINGLE_SUCCESS
                    },
            ).then(
                Commands
                    .literal("lobby")
                    .then(
                        Commands
                            .argument("world", ArgumentTypes.world())
                            .then(
                                Commands
                                    .argument("location", ArgumentTypes.finePosition(true))
                                    .executes { ctx ->
                                        val world = ctx.getArgument("world", World::class.java)
                                        val pResolver = ctx.getArgument("arg", FinePositionResolver::class.java)
                                        SmashSumo.config.set("lobby", Utils.l2s(pResolver.resolve(ctx.source).toLocation(world)))
                                        Command.SINGLE_SUCCESS
                                    },
                            ),
                    ).requires { sender -> sender.executor is Player }
                    .executes { ctx ->
                        SmashSumo.config.set("lobby", Utils.l2s(ctx.source.executor!!.location))
                        SmashSumo.config.save()
                        ctx.source.executor!!.sendMessage("${P}Lobby spawn set to your current location.")
                        Command.SINGLE_SUCCESS
                    },
            )
}
