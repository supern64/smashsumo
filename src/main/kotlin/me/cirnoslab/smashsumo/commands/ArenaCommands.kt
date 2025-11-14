package me.cirnoslab.smashsumo.commands

import me.cirnoslab.smashsumo.Config.Style.P
import me.cirnoslab.smashsumo.Config.Style.S
import me.cirnoslab.smashsumo.Utils
import me.cirnoslab.smashsumo.arena.Arena
import me.cirnoslab.smashsumo.arena.ArenaManager
import org.bukkit.command.CommandSender
import java.util.UUID

object ArenaCommands {
    private var selectedArena: MutableMap<UUID, Arena.Builder> = mutableMapOf()

    fun handle(
        s: CommandSender,
        args: Array<out String>,
    ): Boolean {
        if (args.isEmpty() || args[0].lowercase() != "arena") return false

        if (s !is org.bukkit.entity.Player) {
            s.sendMessage("${P}Only players can use arena commands.")
            return true
        }

        if (args.size < 2) {
            s.sendMessage("${P}Usage: $S/smashsumo arena [select|list|create|save|center|spawn|respawn|side|bottom|top|info]")
            return true
        }

        if (!s.hasPermission("smashsumo.admin")) {
            s.sendMessage("${P}You do not have permission to use this command.")
            return true
        }

        when (args[1].lowercase()) {
            "select" -> {
                if (args.size < 3) {
                    s.sendMessage("${P}Usage: $S/smashsumo arena select [name]")
                    return true
                }
                val arenaName = args[2]
                if (ArenaManager.arenas[arenaName] == null) {
                    s.sendMessage("${P}Arena ${S}$arenaName ${P}does not exist.")
                    return true
                }
                selectedArena[s.uniqueId] = Arena.Builder(ArenaManager.arenas[arenaName]!!)
                s.sendMessage("${P}Arena ${S}$arenaName ${P}selected.")
            }
            "create" -> {
                if (args.size < 3) {
                    s.sendMessage("${P}Usage: $S/smashsumo arena create [name]")
                    return true
                }
                val arenaName = args[2]
                if (ArenaManager.arenas.containsKey(arenaName)) {
                    s.sendMessage("${P}Arena ${S}$arenaName ${P}already exists.")
                    return true
                }
                val arena =
                    Arena.Builder(
                        name = arenaName,
                        center = s.location,
                    )
                selectedArena[s.uniqueId] = arena
                s.sendMessage("${P}Arena ${S}$arenaName ${P}created and selected.")
            }
            "center" -> {
                if (selectedArena[s.uniqueId] == null) {
                    s.sendMessage("${P}No arena selected.")
                    return true
                }
                selectedArena[s.uniqueId]!!.center = s.location
                s.sendMessage("${P}Arena ${S}${selectedArena[s.uniqueId]!!.name}'s ${P}center has been set at your current location.")
            }
            "spawn" -> {
                if (selectedArena[s.uniqueId] == null) {
                    s.sendMessage("${P}No arena selected.")
                    return true
                }
                selectedArena[s.uniqueId]!!.spawnRadius = Utils.latD(selectedArena[s.uniqueId]!!.center, s.location)
                s.sendMessage(
                    "${P}Arena ${S}${selectedArena[s.uniqueId]!!.name}'s ${P}spawn radius has been set to ${S}${String.format(
                        "%.2f",
                        selectedArena[s.uniqueId]!!.spawnRadius,
                    )}$P.",
                )
            }
            "side" -> {
                if (selectedArena[s.uniqueId] == null) {
                    s.sendMessage("${P}No arena selected.")
                    return true
                }
                selectedArena[s.uniqueId]!!.sideRadius = Utils.latD(selectedArena[s.uniqueId]!!.center, s.location)
                s.sendMessage(
                    "${P}Arena ${S}${selectedArena[s.uniqueId]!!.name}'s ${P}side barrier radius has been set to ${S}${String.format(
                        "%.2f",
                        selectedArena[s.uniqueId]!!.sideRadius,
                    )}$P.",
                )
            }
            "bottom" -> {
                if (selectedArena[s.uniqueId] == null) {
                    s.sendMessage("${P}No arena selected.")
                    return true
                }
                selectedArena[s.uniqueId]!!.bottomBarrier = s.location.y
                s.sendMessage(
                    "${P}Arena ${S}${selectedArena[s.uniqueId]!!.name}'s ${P}bottom barrier has been set to Y${S}${String.format(
                        "%.2f",
                        selectedArena[s.uniqueId]!!.bottomBarrier,
                    )}$P.",
                )
            }
            "top" -> {
                if (selectedArena[s.uniqueId] == null) {
                    s.sendMessage("${P}No arena selected.")
                    return true
                }
                selectedArena[s.uniqueId]!!.topBarrier = s.location.y
                s.sendMessage(
                    "${P}Arena ${S}${selectedArena[s.uniqueId]!!.name}'s ${P}top barrier has been set to Y${S}${String.format(
                        "%.2f",
                        selectedArena[s.uniqueId]!!.topBarrier!!,
                    )}$P.",
                )
            }
            "respawn" -> {
                if (selectedArena[s.uniqueId] == null) {
                    s.sendMessage("${P}No arena selected.")
                    return true
                }
                selectedArena[s.uniqueId]!!.respawnHeight = s.location.blockY
                s.sendMessage(
                    "${P}Arena ${S}${selectedArena[s.uniqueId]!!.name}'s ${P}respawn height has been set to Y${S}${selectedArena[s.uniqueId]!!.respawnHeight}$P.",
                )
            }
            "save" -> {
                if (selectedArena[s.uniqueId] == null) {
                    s.sendMessage("${P}No arena selected.")
                    return true
                }
                val arenaCheck = selectedArena[s.uniqueId]!!.checkArena()
                if (!arenaCheck.isEmpty()) {
                    s.sendMessage("${P}Invalid arena.${S}\n${arenaCheck.joinToString("\n") { a -> a.description }}}")
                    return true
                }
                ArenaManager.arenas[selectedArena[s.uniqueId]!!.name] = selectedArena[s.uniqueId]!!.build()
                ArenaManager.saveArenas()
                s.sendMessage("${P}Arenas saved.")
            }
            "info" -> {
                lateinit var arena: Arena.Builder
                if (args.size >= 3) {
                    val arenaName = args[2]
                    if (ArenaManager.arenas[arenaName] == null) {
                        s.sendMessage("${P}Arena ${S}$arenaName ${P}does not exist.")
                        return true
                    }
                    arena = Arena.Builder(ArenaManager.arenas[arenaName]!!)
                } else {
                    if (selectedArena[s.uniqueId] == null) {
                        s.sendMessage("${P}No arena selected. (Or specify arena name)")
                        return true
                    }
                    arena = selectedArena[s.uniqueId]!!
                }

                s.sendMessage(
                    """${P}Arena Info:
                    |${P}Name: ${S}${arena.name}
                    |${P}Center: ${S}${arena.center.world?.name} (${String.format(
                        "%.2f",
                        arena.center.x,
                    )}, ${String.format("%.2f", arena.center.y)}, ${String.format("%.2f", arena.center.z)})
                    |${P}Spawn Radius: ${S}${String.format("%.2f", arena.spawnRadius)}
                    |${P}Side Barrier Radius: ${S}${String.format("%.2f", arena.sideRadius)}
                    |${P}Respawn Height: ${S}${arena.respawnHeight}
                    |${P}Bottom Barrier: ${S}Y${String.format("%.2f", arena.bottomBarrier)}
                    |${P}Top Barrier: ${if (arena.topBarrier != null) "${S}Y${String.format("%.2f", arena.topBarrier!!)}" else "${S}None"}
                    """.trimMargin(),
                )
            }
            "list" -> {
                s.sendMessage("${P}Available arenas: ${S}${ArenaManager.arenas.keys.joinToString(", ")}")
            }
            else -> {
                s.sendMessage("${P}Unknown subcommand. Usage: $S/smashsumo arena [select|list]")
            }
        }
        return true
    }
}
