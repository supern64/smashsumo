package me.cirnoslab.smashsumo.game

import io.github.theluca98.textapi.Title
import me.cirnoslab.smashsumo.SmashSumo
import me.cirnoslab.smashsumo.SmashSumo.Companion.S
import me.cirnoslab.smashsumo.SmashSumo.Companion.P
import me.cirnoslab.smashsumo.Utils
import me.cirnoslab.smashsumo.Utils.setAbsorptionHearts
import me.cirnoslab.smashsumo.arena.Arena
import me.cirnoslab.smashsumo.game.GameManager.GameJoinResult
import me.cirnoslab.smashsumo.game.GameManager.GameLeaveResult
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Scoreboard
import java.util.UUID

class Game(
    val arena: Arena // game state is tied to arena state
) {
    val gamePlayers = mutableMapOf<UUID, GamePlayer>()
    var startingPlayers: List<GamePlayer>? = null
    var state = GameState.WAITING
    val scoreboard: Scoreboard = Bukkit.getScoreboardManager().newScoreboard
    private var startTime: Long? = null

    val formattedTime: String
        get() = startTime?.let {
            val duration = Math.floorDiv(System.currentTimeMillis() - it, 1000L).toInt()
            return "${S}${"%02d".format(Math.floorDiv(duration, 60))}${WHITE}:${S}${"%02d".format(duration % 60)}"
        } ?: "${S}00${WHITE}:${S}00"

    // should be called via GameManager
    fun join(p: Player): GameJoinResult {
        if (gamePlayers.containsKey(p.uniqueId)) return GameJoinResult.ALREADY_IN_GAME
        if (state == GameState.ENDING) return GameJoinResult.GAME_ENDING

        val gp = GamePlayer(this, p, null, SmashSumo.MAX_LIVES)
        gamePlayers[p.uniqueId] = gp
        if (state != GameState.WAITING) {
            gamePlayers[p.uniqueId]!!.state = GamePlayer.PlayerState.SPECTATING
            p.gameMode = GameMode.SPECTATOR
            p.teleport(Location(arena.center.world, arena.center.x, arena.center.y + 2.0, arena.center.z))
            return GameJoinResult.GAME_STARTED
        }

        gp.board.updateTitle("${P}${BOLD}Smash Sumo")

        p.gameMode = GameMode.ADVENTURE
        p.health = 20.0
        p.foodLevel = 20
        p.setAbsorptionHearts(6f)

        p.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE,1))
        p.teleport(Location(arena.center.world, arena.center.x, arena.center.y + 2.0, arena.center.z))

        messageAll("${S}${p.name} ${P}has joined the game!")
        return GameJoinResult.SUCCESS
    }

    // should be called via GameManager
    fun leave(p: Player) : GameLeaveResult {
        val gp = gamePlayers[p.uniqueId] ?: throw IllegalArgumentException("tried to kick GamePlayer that doesn't exist")

        if (gp.state == GamePlayer.PlayerState.IN_GAME) {
            // handle player leaving during game
            gp.state = GamePlayer.PlayerState.GHOST
            arena.center.world.strikeLightningEffect(gp.player.location)

            if (gp.respawnPoint != null) {
                arena.center.world.getBlockAt(gp.respawnPoint).type = Material.AIR
                gp.respawnPoint = null
            }

            if (getActivePlayers().size == 1) {
                endGame()
            }
        }

        gp.board.delete()
        scoreboard.getTeam("P${gp.playerNumber}").unregister()
        p.scoreboard = Bukkit.getScoreboardManager().mainScoreboard

        gamePlayers.remove(p.uniqueId)
        p.gameMode = GameMode.SURVIVAL
        p.setAbsorptionHearts(0f)
        p.removePotionEffect(PotionEffectType.JUMP)

        val lobby = SmashSumo.config.getString("lobby")
        if (lobby == null) {
            SmashSumo.log("[Warning] Lobby location not set. Player will not be teleported.")
        } else {
            p.teleport(Utils.s2l(lobby))
        }
        messageAll("${S}${p.name} ${P}has left the game!")
        return GameLeaveResult.SUCCESS
    }

    // countdown routine
    fun initStart() {
        arena.state = Arena.ArenaState.PLAYING
        state = GameState.COUNTDOWN

        val spawnPoints = arena.getSpawnLocations(gamePlayers.size)
        gamePlayers.values.forEachIndexed { i, gp ->
            gp.state = GamePlayer.PlayerState.IN_GAME
            gp.player.teleport(spawnPoints[i])
            gp.playerNumber = i + 1

            val gpTeam = scoreboard.registerNewTeam("P${gp.playerNumber}")
            gpTeam.prefix = "${gp.color}[P${gp.playerNumber}] "
            gpTeam.addEntry(gp.player.name)
            gp.player.scoreboard = scoreboard
        }
        startingPlayers = gamePlayers.values.toList()
        CountdownTask(this).runTaskTimer(SmashSumo.plugin, 0L, 20L)
    }

    class CountdownTask(val game: Game) : BukkitRunnable() {
        var countdown = 3
        override fun run() {
            if (countdown > 0) {
                for (gp in game.getActivePlayers()) {
                    gp.player.playSound(gp.player.location, Sound.NOTE_PIANO, 1.0f, 0.95f)
                    Title("$countdown", "", 0, 20, 0).send(gp.player)
                }
                countdown--
                return
            }
            for (gp in game.getActivePlayers()) {
                gp.player.playSound(gp.player.location, Sound.NOTE_PIANO, 1.0f, 1.9f)
                Title("GO!", "", 0, 15, 5).send(gp.player)
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

        gp.player.setAbsorptionHearts(gp.lives * 2.0f)

        if (gp.lives == 0) {
            gp.state = GamePlayer.PlayerState.SPECTATING
            if (getActivePlayers().size == 1) {
                // game end routine
                endGame()
            } else {
                gp.player.gameMode = GameMode.SPECTATOR
                gp.player.sendMessage("${RED}You have been eliminated! You are now a spectator.")
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

    class RespawnSetupTask(val game: Game, val gp: GamePlayer) : BukkitRunnable() {
        override fun run() {
            val rl = gp.respawnPoint ?: return
            val block = game.arena.center.world.getBlockAt(rl)
            block.type = Material.STAINED_GLASS
            block.data = 13 // green stained glass
        }
    }

    class PlayerRespawnTask(val gp: GamePlayer) : BukkitRunnable() {
        override fun run() {
            val rl = gp.respawnPoint ?: return // should never return unless player leaves midgame
            gp.player.playSound(gp.player.location, Sound.NOTE_PIANO, 1.0f, 1.9f)
            gp.player.gameMode = GameMode.ADVENTURE
            gp.player.teleport(Location(rl.world, rl.blockX + 0.5, rl.blockY + 1.0, rl.blockZ + 0.5))
            gp.waitRespawn = false
        }
    }

    class RespawnPlatformExpireTask(val game: Game, val gp: GamePlayer) : BukkitRunnable() {
        var phase = 0
        override fun run() {
            if (gp.respawnPoint == null) { // don't run if respawn state has been reset
                this.cancel()
                return
            }
            val block = game.arena.center.world.getBlockAt(gp.respawnPoint)
            when (phase) {
                0 -> block.data = 4 // yellow
                1 -> block.data = 1 // orange
                2 -> block.data = 14 // red
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
        messageAll("${GOLD}${BOLD}${winnerName} ${YELLOW}is the winner!")
        val title = Title("${P}GAME!", "${YELLOW}Winner: ${GOLD}${BOLD}$winnerName", 0, 60, 10)

        gamePlayers.values.forEach { gp ->
            gp.state = GamePlayer.PlayerState.ENDING
            gp.player.gameMode = GameMode.ADVENTURE
            gp.player.teleport(Location(arena.center.world, arena.center.x, arena.center.y + 2.0, arena.center.z))
            title.send(gp.player)
        }
        // deinit and destroy own instance
        EndGameTask(this).runTaskLater(SmashSumo.plugin, 20 * 6)
    }

    class EndGameTask(val game: Game) : BukkitRunnable() {
        override fun run() {
            game.arena.state = Arena.ArenaState.AVAILABLE
            val lobby = SmashSumo.config.getString("lobby")
            if (lobby == null) {
                SmashSumo.log("[Warning] Lobby location not set. Players will not be teleported.")
            }
            game.gamePlayers.values.forEach { gp ->
                gp.player.gameMode = GameMode.SURVIVAL
                gp.player.setAbsorptionHearts(0f)
                gp.player.health = 20.0
                gp.player.removePotionEffect(PotionEffectType.JUMP)
                if (lobby != null) {
                    gp.player.teleport(Utils.s2l(lobby))
                }
                gp.board.delete()
                gp.player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
            }
            game.gamePlayers.clear()
            GameManager.removeGame(game)
        }
    }

    fun messageAll(m: String) {
        gamePlayers.values.forEach { gp -> gp.player.sendMessage(m) }
    }

    fun messagePlayers(m: String) {
        gamePlayers.values.forEach { gp -> if (gp.state != GamePlayer.PlayerState.SPECTATING) gp.player.sendMessage(m) }
    }

    fun messageExcept(m: String, gp: GamePlayer) {
        gamePlayers.values.forEach { ggp -> if (ggp.player.uniqueId != gp.player.uniqueId) gp.player.sendMessage(m) }
    }

    fun getActivePlayers(): List<GamePlayer> {
        return gamePlayers.values.filter { it.state == GamePlayer.PlayerState.IN_GAME }
    }

    fun getRespawningPlayers(): List<GamePlayer> {
        return gamePlayers.values.filter { it.respawnPoint != null }
    }

    enum class GameState {
        WAITING,
        COUNTDOWN,
        IN_GAME,
        ENDING
    }
}