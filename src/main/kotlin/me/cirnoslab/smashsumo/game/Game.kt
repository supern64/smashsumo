package me.cirnoslab.smashsumo.game

import io.papermc.paper.scoreboard.numbers.NumberFormat
import me.cirnoslab.smashsumo.SmashSumo
import me.cirnoslab.smashsumo.SmashSumo.Companion.P
import me.cirnoslab.smashsumo.SmashSumo.Companion.S
import me.cirnoslab.smashsumo.Utils
import me.cirnoslab.smashsumo.Utils.mm
import me.cirnoslab.smashsumo.arena.Arena
import me.cirnoslab.smashsumo.game.GameManager.GameJoinResult
import me.cirnoslab.smashsumo.game.GameManager.GameLeaveResult
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import java.time.Duration
import java.util.UUID
import java.util.logging.Level

class Game(
    // game state is tied to arena state
    val arena: Arena,
) {
    val gamePlayers = mutableMapOf<UUID, GamePlayer>()
    var startingPlayers: List<GamePlayer>? = null
    var state = GameState.WAITING
    val scoreboard: Scoreboard = Bukkit.getScoreboardManager().newScoreboard
    private var startTime: Long? = null

    val formattedTime: Component
        get() =
            startTime?.let {
                val duration = Math.floorDiv(System.currentTimeMillis() - it, 1000L).toInt()
                return "${S}${"%02d".format(Math.floorDiv(duration, 60))}<white>:${S}${"%02d".format(duration % 60)}".mm()
            } ?: "${S}00<white>:${S}00".mm()

    // should be called via GameManager
    fun join(p: Player): GameJoinResult {
        if (gamePlayers.containsKey(p.uniqueId)) return GameJoinResult.ALREADY_IN_GAME
        if (state == GameState.ENDING) return GameJoinResult.GAME_ENDING

        val gp = GamePlayer(this, p, null, SmashSumo.MAX_LIVES)
        gamePlayers[p.uniqueId] = gp

        gp.board.updateTitle("$P<bold>Smash Sumo".mm())

        if (state != GameState.WAITING) {
            gamePlayers[p.uniqueId]!!.state = GamePlayer.PlayerState.SPECTATING
            p.gameMode = GameMode.SPECTATOR
            p.teleport(Location(arena.center.world, arena.center.x, arena.center.y + 2.0, arena.center.z))
            return GameJoinResult.GAME_STARTED
        }

        p.gameMode = GameMode.ADVENTURE
        p.health = 20.0
        p.foodLevel = 20
        p.absorptionAmount = 6.0
        p.getAttribute(Attribute.ATTACK_SPEED)!!.baseValue = 6.0

        p.updateCommands()

        // double jump setup
        p.allowFlight = true
        p.isFlying = false

        p.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, Int.MAX_VALUE, 1))
        p.teleport(Location(arena.center.world, arena.center.x, arena.center.y + 2.0, arena.center.z))

        messageAll("${S}${p.name} ${P}has joined the game!")
        return GameJoinResult.SUCCESS
    }

    // should be called via GameManager
    fun leave(p: Player): GameLeaveResult {
        val gp = gamePlayers[p.uniqueId] ?: throw IllegalArgumentException("tried to kick GamePlayer that doesn't exist")

        if (gp.state == GamePlayer.PlayerState.IN_GAME) {
            // handle player leaving during game
            gp.state = GamePlayer.PlayerState.GHOST
            arena.center.world.strikeLightningEffect(gp.player.location)

            if (gp.respawnPoint != null) {
                arena.center.world
                    .getBlockAt(gp.respawnPoint!!)
                    .type = Material.AIR
                gp.respawnPoint = null
            }

            if (getActivePlayers().size == 1) {
                endGame()
            }
        }

        scoreboard.getTeam("P${gp.playerNumber}")?.unregister()
        p.scoreboard = Bukkit.getScoreboardManager().mainScoreboard

        gp.board.delete()

        gamePlayers.remove(p.uniqueId)
        p.gameMode = GameMode.SURVIVAL
        p.absorptionAmount = 0.0
        p.removePotionEffect(PotionEffectType.JUMP_BOOST)
        p.getAttribute(Attribute.ATTACK_SPEED)!!.baseValue = 4.0
        p.updateCommands()

        p.allowFlight = false
        p.isFlying = false

        val lobby = SmashSumo.config.getString("lobby")
        if (lobby == null) {
            SmashSumo.log(Level.WARNING, "Lobby location not set. Player will not be teleported.")
        } else {
            p.teleport(Utils.s2l(lobby))
        }

        if (startingPlayers == null || startingPlayers!!.contains(gp)) messageAll("${S}${p.name} ${P}has left the game!")
        return GameLeaveResult.SUCCESS
    }

    // countdown routine
    fun initStart() {
        arena.state = Arena.ArenaState.PLAYING
        state = GameState.COUNTDOWN

        val spawnPoints = arena.getSpawnLocations(gamePlayers.size)
        val health = scoreboard.registerNewObjective("percent", Criteria.DUMMY, text("%"))
        health.displaySlot = DisplaySlot.BELOW_NAME

        gamePlayers.values.forEachIndexed { i, gp ->
            gp.state = GamePlayer.PlayerState.IN_GAME
            gp.player.teleport(spawnPoints[i])
            gp.playerNumber = i + 1

            health.getScore(gp.player.name).numberFormat(
                NumberFormat.fixed(
                    gp.lifeComponent
                        .appendSpace()
                        .append(text("%.1f".format(gp.damage), gp.damageColor)),
                ),
            )

            val gpTeam = scoreboard.registerNewTeam("P${gp.playerNumber}")
            gpTeam.prefix(text("[P${gp.playerNumber}] ", gp.color))
            gpTeam.addEntry(gp.player.name)
            gp.player.scoreboard = scoreboard
        }
        startingPlayers = gamePlayers.values.toList()
        CountdownTask(this).runTaskTimer(SmashSumo.plugin, 0L, 20L)
    }

    class CountdownTask(
        val game: Game,
    ) : BukkitRunnable() {
        var countdown = 3

        override fun run() {
            if (countdown > 0) {
                for (gp in game.getActivePlayers()) {
                    gp.player.playSound(gp.player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.95f)
                    gp.player.showTitle(
                        Title.title(
                            text(countdown),
                            Component.empty(),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO),
                        ),
                    )
                }
                countdown--
                return
            }
            for (gp in game.getActivePlayers()) {
                gp.player.playSound(gp.player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.9f)
                gp.player.showTitle(
                    Title.title(
                        text("GO!"),
                        Component.empty(),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(750), Duration.ofMillis(250)),
                    ),
                )
            }
            game.startTime = System.currentTimeMillis()
            game.state = GameState.IN_GAME
            this.cancel()
        }
    }

    // death routine
    fun kill(gp: GamePlayer) {
        if (!gamePlayers.containsKey(gp.player.uniqueId)) throw IllegalArgumentException("tried to kill GamePlayer from incorrect session")
        if (gp.state != GamePlayer.PlayerState.IN_GAME) throw IllegalStateException("tried to kill out of game player")
        arena.center.world.strikeLightningEffect(gp.player.location)

        gp.lives -= 1
        gp.damage = 0.0
        gp.player.health = 20.0
        gp.player.absorptionAmount = gp.lives * 2.0

        scoreboard.getObjective("percent")?.getScore(gp.player.name)?.numberFormat(
            NumberFormat.fixed(
                gp.lifeComponent
                    .appendSpace()
                    .append(text("%.1f".format(gp.damage), gp.damageColor)),
            ),
        )

        if (gp.lives == 0) {
            scoreboard.getTeam("P${gp.playerNumber}")?.suffix(text(" [DEAD]", GRAY))
            gp.state = GamePlayer.PlayerState.SPECTATING
            if (getActivePlayers().size == 1) {
                // game end routine
                endGame()
            } else {
                gp.player.gameMode = GameMode.SPECTATOR
                gp.player.sendRichMessage("<red>You have been eliminated! You are now a spectator.")
            }
        } else {
            gp.player.gameMode = GameMode.SPECTATOR

            val respawnLocation = arena.getRespawnPoint(getRespawningPlayers().size)
            gp.respawnPoint = respawnLocation
            gp.waitRespawn = true
            // respawn routine
            RespawnSetupTask(this, gp).runTaskLater(SmashSumo.plugin, 18)
            PlayerRespawnTask(gp).runTaskLater(SmashSumo.plugin, 20)
            RespawnPlatformExpireTask(this, gp).runTaskTimer(SmashSumo.plugin, 35, 15)
        }
    }

    @Suppress("DEPRECATION")
    class RespawnSetupTask(
        val game: Game,
        val gp: GamePlayer,
    ) : BukkitRunnable() {
        override fun run() {
            val rl = gp.respawnPoint ?: return
            val block =
                game.arena.center.world
                    .getBlockAt(rl)
            block.type = Material.GREEN_STAINED_GLASS
        }
    }

    class PlayerRespawnTask(
        val gp: GamePlayer,
    ) : BukkitRunnable() {
        override fun run() {
            val rl = gp.respawnPoint ?: return // should never return unless player leaves midgame
            gp.player.playSound(gp.player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.9f)
            gp.player.gameMode = GameMode.ADVENTURE
            gp.player.teleport(Location(rl.world, rl.blockX + 0.5, rl.blockY + 1.0, rl.blockZ + 0.5, 0f, 90f))
            gp.waitRespawn = false
        }
    }

    @Suppress("DEPRECATION")
    class RespawnPlatformExpireTask(
        val game: Game,
        val gp: GamePlayer,
    ) : BukkitRunnable() {
        var phase = 0

        override fun run() {
            if (gp.respawnPoint == null) { // don't run if respawn state has been reset
                this.cancel()
                return
            }
            val block =
                game.arena.center.world
                    .getBlockAt(gp.respawnPoint!!)
            when (phase) {
                0 -> block.type = Material.YELLOW_STAINED_GLASS // yellow
                1 -> block.type = Material.ORANGE_STAINED_GLASS // orange
                2 -> block.type = Material.RED_STAINED_GLASS // red
                3 -> {
                    block.type = Material.AIR
                    gp.respawnPoint = null
                    this.cancel()
                }
            }
            phase++
        }
    }

    fun endGame() {
        state = GameState.ENDING
        val winnerName = getActivePlayers()[0].player.name
        messageAll("<gold><bold>$winnerName</bold> <yellow>is the winner!")

        gamePlayers.values.forEach { gp ->
            gp.state = GamePlayer.PlayerState.ENDING
            gp.player.gameMode = GameMode.ADVENTURE
            gp.player.teleport(Location(arena.center.world, arena.center.x, arena.center.y + 2.0, arena.center.z))
            gp.player.showTitle(
                Title.title(
                    "${P}GAME!".mm(),
                    "<yellow>Winner: <gold><bold>$winnerName".mm(),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofMillis(500)),
                ),
            )
        }
        // deinit and destroy own instance
        EndGameTask(this).runTaskLater(SmashSumo.plugin, 20 * 6)
    }

    class EndGameTask(
        val game: Game,
    ) : BukkitRunnable() {
        override fun run() {
            game.arena.state = Arena.ArenaState.AVAILABLE
            val lobby = SmashSumo.config.getString("lobby")
            if (lobby == null) {
                SmashSumo.log(Level.WARNING, "Lobby location not set. Players will not be teleported.")
            }
            val gps = game.gamePlayers.values.toList()
            game.gamePlayers.clear()
            gps.forEach { gp ->
                gp.player.gameMode = GameMode.SURVIVAL
                gp.player.absorptionAmount = 0.0
                gp.player.health = 20.0
                gp.player.removePotionEffect(PotionEffectType.JUMP_BOOST)
                if (lobby != null) {
                    gp.player.teleport(Utils.s2l(lobby))
                }
                gp.board.delete()
                gp.player.allowFlight = false
                gp.player.isFlying = false
                gp.player.getAttribute(Attribute.ATTACK_SPEED)!!.baseValue = 4.0
                gp.player.updateCommands()
                gp.player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
            }

            GameManager.removeGame(game)
        }
    }

    fun messageAll(m: String) {
        gamePlayers.values.forEach { gp -> gp.player.sendRichMessage(m) }
    }

    fun getActivePlayers(): List<GamePlayer> = gamePlayers.values.filter { it.state == GamePlayer.PlayerState.IN_GAME }

    fun getRespawningPlayers(): List<GamePlayer> = gamePlayers.values.filter { it.respawnPoint != null }

    enum class GameState {
        WAITING,
        COUNTDOWN,
        IN_GAME,
        ENDING,
    }
}
