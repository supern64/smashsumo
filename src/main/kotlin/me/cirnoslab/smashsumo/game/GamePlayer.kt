package me.cirnoslab.smashsumo.game

import fr.mrmicky.fastboard.FastBoard
import io.github.theluca98.textapi.ActionBar
import me.cirnoslab.smashsumo.Config
import me.cirnoslab.smashsumo.Config.Style.P
import me.cirnoslab.smashsumo.Config.Style.S
import me.cirnoslab.smashsumo.Config.Style.teamColors
import me.cirnoslab.smashsumo.SmashSumo.Companion.SCOREBOARD_LINE
import me.cirnoslab.smashsumo.Utils
import me.cirnoslab.smashsumo.Utils.clearInventory
import me.cirnoslab.smashsumo.Utils.setAbsorptionHearts
import me.cirnoslab.smashsumo.item.ItemManager
import me.cirnoslab.smashsumo.item.events.ItemArmorEvent
import me.cirnoslab.smashsumo.item.events.ItemHitPlayerEvent
import me.cirnoslab.smashsumo.kit.Kit
import me.cirnoslab.smashsumo.menu.actionitem.KitSelectorMenuItem
import me.cirnoslab.smashsumo.menu.actionitem.QuitGameItem
import me.cirnoslab.smashsumo.menu.actionitem.StartGameItem
import org.bukkit.Bukkit
import org.bukkit.ChatColor.BOLD
import org.bukkit.ChatColor.DARK_RED
import org.bukkit.ChatColor.GRAY
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.WHITE
import org.bukkit.ChatColor.YELLOW
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Egg
import org.bukkit.entity.Entity
import org.bukkit.entity.Fireball
import org.bukkit.entity.FishHook
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team
import org.bukkit.util.Vector
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Represents the game state of a player in a [Game]
 *
 * @property game the Game this GamePlayer is for
 * @property player the Player this GamePlayer is for
 * @property playerNumber the player order (see [Game.initStart])
 * @property lives the number of lives that this player has
 * @property kit the [Kit] this player will have at the start of the game
 */
class GamePlayer(
    val game: Game,
    val player: Player,
    var playerNumber: Int? = null,
    var lives: Int,
    var kit: Kit? = null,
) {
    /**
     * The current state of this player
     *
     * @see PlayerState
     */
    var state: PlayerState = PlayerState.WAITING

    /**
     * The current damage percentage of this player
     */
    var damage: Double = 0.0

    /**
     * The current speed squared of this player, measured from [org.bukkit.event.player.PlayerMoveEvent]
     *
     * @see me.cirnoslab.smashsumo.listeners.PlayerMechanicListener.onPlayerHit
     */
    var speedSquared: Double = 0.0

    /**
     * The [FastBoard] instance used by this player
     */
    var board = FastBoard(player)

    /**
     * The [Team] used to display the player's prefix
     * Only exists after game starts counting down
     */
    lateinit var sbTeam: Team

    /**
     * The [Objective] used to display the player's health
     * Only exists after game starts counting down
     */
    lateinit var sbHealth: Objective

    /**
     * The [Location] this player will respawn after death
     *
     * @see Game.kill
     */
    var respawnPoint: Location? = null

    /**
     * Whether this player is still waiting to be respawned
     *
     * @see Game.kill
     */
    var waitRespawn = false

    /**
     * The current double jump phase
     *
     * @see me.cirnoslab.smashsumo.listeners.PlayerMechanicListener.onPlayerFly
     */
    var jumpPhase = 0

    /**
     * The current speed of this player, measured from [org.bukkit.event.player.PlayerMoveEvent]
     *
     * @see me.cirnoslab.smashsumo.listeners.PlayerMechanicListener.onPlayerHit
     */
    val speed: Double
        get() = sqrt(speedSquared)

    /**
     * The current health mapped from [damage]
     */
    val displayHealth: Double
        get() = Utils.ntrc(damage, 0.0, 125.0, 20.0, 1.0)

    /**
     * The display string for life count
     */
    val lifeString: String
        get() = "${color}${"⬤".repeat(lives)}${GRAY}${"⬤".repeat(game.settings.lives - lives)}"

    // HUDs are here so they can be modified per player

    /**
     * The display string for the player's action bar
     *
     * @see HUDManager
     */
    val actionBarDisplay: String
        get() {
            return when (state) {
                PlayerState.SPECTATING -> "${P}Currently spectating"
                PlayerState.WAITING -> {
                    if (!Config.enableKitSelector) {
                        "${P}Waiting for players... (${S}${game.gamePlayers.count()}$P)"
                    } else {
                        "${P}Selected Kit: ${S}${if (kit == null) "None" else kit!!.name}"
                    }
                }
                PlayerState.IN_GAME -> "${color}P$playerNumber $P| ${damageColor}${"%.1f".format(damage)}%$P | Lives: ${S}$lives"
                PlayerState.ENDING -> "${P}Waiting to teleport back..."
                else -> "You should not see this."
            }
        }

    /**
     * The display list for the player's scoreboard
     *
     * @see HUDManager
     */
    val scoreboardDisplay: List<String>
        get() {
            return when (state) {
                PlayerState.WAITING ->
                    listOf(
                        SCOREBOARD_LINE,
                        "Waiting for players:",
                        "${S}${game.gamePlayers.size} ${WHITE}currently waiting",
                        SCOREBOARD_LINE,
                    )
                PlayerState.ENDING ->
                    listOf(
                        SCOREBOARD_LINE,
                        "Game ended.",
                        SCOREBOARD_LINE,
                    )

                PlayerState.GHOST ->
                    listOf()
                else -> {
                    val board =
                        mutableListOf(
                            SCOREBOARD_LINE,
                            "Duration: ${game.formattedTime}",
                        )
                    for (gp in game.startingPlayers!!) {
                        if (gp.playerNumber == null) continue // joined as spectator, should not be possible
                        if (board.size >= 14) break // 12 players max
                        if (gp.state != PlayerState.IN_GAME) {
                            board.add("  ${gp.color}P${gp.playerNumber} ${RED}ELIMINATED")
                        } else {
                            board.add("  ${gp.color}P${gp.playerNumber} ${gp.damageColor}${"%.1f".format(gp.damage)}% ${gp.lifeString}")
                        }
                    }
                    board.add(SCOREBOARD_LINE)
                    return board
                }
            }
        }

    /**
     * The main color of the player (based on [playerNumber])
     */
    val color
        get() = if (playerNumber != null) teamColors[(playerNumber!! - 1) % teamColors.size] else GRAY

    /**
     * The damage color used
     */
    val damageColor
        get() =
            when (damage) {
                in 0.0..35.0 -> S
                in 35.001..65.0 -> YELLOW
                in 65.001..100.0 -> RED
                else -> DARK_RED
            }

    /**
     * Initializes a player for the waiting room. Called by [Game.join]
     * (does not teleport the player)
     *
     * @param spectate whether to set up as a spectator
     */
    fun init(spectate: Boolean = false) {
        val arena = game.arena
        board.updateTitle("${P}${BOLD}Smash Sumo")

        if (spectate) {
            state = PlayerState.SPECTATING
            player.gameMode = GameMode.SPECTATOR
            player.teleport(Location(arena.center.world, arena.center.x, arena.center.y + 2.0, arena.center.z))
            return
        }

        player.gameMode = GameMode.ADVENTURE
        player.health = 20.0
        player.foodLevel = 20
        player.setAbsorptionHearts(game.settings.lives * 2f)

        // double jump setup
        player.allowFlight = true
        player.isFlying = false

        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 1))
        player.clearInventory()

        if (player.hasPermission("smashsumo.gm")) {
            player.inventory.setItem(0, StartGameItem.get())
        }
        if (Config.enableKitSelector) {
            player.inventory.setItem(1, KitSelectorMenuItem.get())
        }
        player.inventory.setItem(8, QuitGameItem.get())
    }

    /**
     * Sets up a player for the game itself. Called by [Game.initStart]
     *
     * @param pn the player number assigned
     */
    fun setup(
        pn: Int,
        health: Objective,
    ) {
        sbHealth = health

        state = PlayerState.IN_GAME
        this.playerNumber = pn
        sbHealth.getScore(player.name).score = 0

        sbTeam = game.scoreboard.registerNewTeam("P$playerNumber")
        sbTeam.prefix = "$color[P$playerNumber] "
        sbTeam.suffix = " $lifeString"
        sbTeam.addEntry(player.name)
        player.scoreboard = game.scoreboard

        jumpPhase = 0
        player.allowFlight = false
        player.isFlying = false

        player.clearInventory()
        kit?.apply(player)
    }

    /**
     * Processes a hit by another player.
     *
     * @param aGP the attacker's GamePlayer
     * @param hitValue the HitValue to use for this hit
     * @param processItems whether to take both player's items into consideration
     * @param dEvent the EntityDamageByEntityEvent that triggered this hit
     */
    fun hit(
        aGP: GamePlayer,
        hitValue: HitValue? = null,
        processItems: Boolean = true,
        dEvent: EntityDamageByEntityEvent? = null,
    ): HitValue {
        var hv = hitValue
        val aVertMultiplier =
            -aGP.player.velocity.y
                .coerceAtMost(0.0)
        val aMomentum = aGP.speed
        // initial damage calculation
        if (hv == null) {
            val initialDamage = Random.nextDouble(2.0, 3.0) + aMomentum * Random.nextDouble(10.0, 15.0)
            hv = HitValue(game.settings.playerKnockback, initialDamage)
        }

        if (processItems) {
            val aItem = ItemManager.getItem(aGP.player.itemInHand)
            aItem?.hit(ItemHitPlayerEvent(aGP, this, hv, dEvent))

            player.inventory.armorContents.forEach { i ->
                val dItem = ItemManager.getItem(i)
                dItem?.damage(ItemArmorEvent(aGP, this, hv, dEvent))
            }
        }

        damage(hv.damage)
        knock(aGP, hv, aVertMultiplier, aMomentum)
        return hv
    }

    /**
     * Processes a hit by a projectile shot by another player.
     *
     * @param aGP the projectile owner's GamePlayer
     * @param projectile the projectile that shot the player
     * @param hitValue the HitValue to use for this hit
     * @param processItems whether to take both player's items into consideration
     * @param dEvent the EntityDamageByEntityEvent that triggered this hit
     */
    fun hit(
        projectile: Projectile,
        aGP: GamePlayer,
        hitValue: HitValue? = null,
        processItems: Boolean = true,
        dEvent: EntityDamageByEntityEvent? = null,
    ): HitValue {
        var hv = hitValue

        // initial damage calculation
        if (hv == null) {
            val initialDamage = Random.nextDouble(1.0, 5.0)
            hv = HitValue(game.settings.projectileKnockback, initialDamage)

            when (projectile) {
                is Egg, is Snowball -> hv = hv.multiply(0.4, 0.9)
                is Fireball -> hv = hv.multiply(1.2, 1.0)
                is FishHook -> hv = hv.multiply(0.3, 0.8)
            }
        }

        if (processItems) {
            player.inventory.armorContents.forEach { i ->
                val dItem = ItemManager.getItem(i)
                dItem?.damage(ItemArmorEvent(aGP, this, hv, dEvent, mcProjectile = projectile))
            }
        }

        damage(hv.damage)
        knock(projectile, aGP, hv)
        return hv
    }

    /**
     * Applies damage and updates the display.
     *
     * @param damage the damage to apply
     */
    fun damage(damage: Double) {
        this.damage += damage
        player.health = displayHealth
        updateDisplays()
    }

    /**
     * Applies vanilla knockback from a player.
     *
     * @param aGP the player attacking
     * @param hitValue the HitValue to use
     * @param aVertMultiplier the vertical momentum multiplier to use
     * @param aMomentum the total momentum multiplier to use
     */
    fun knock(
        aGP: GamePlayer,
        hitValue: HitValue,
        aVertMultiplier: Double,
        aMomentum: Double,
    ) {
        val dKnockback =
            aGP.player.location.direction
                .normalize()
                .setY(if ((player as Entity).isOnGround) hitValue.initialY else hitValue.initialY * sign(aGP.player.location.direction.y))
                // cumulative damage
                .multiply(
                    Vector(
                        damage * hitValue.xzDamageMultiplier,
                        damage * hitValue.yDamageMultiplier,
                        damage * hitValue.xzDamageMultiplier,
                    ),
                )
                // current attack
                .multiply(
                    Vector(
                        (aMomentum + 1) * hitValue.xzMomentumMultiplier,
                        (aVertMultiplier + 1) * hitValue.yMomentumMultiplier,
                        (aMomentum + 1) * hitValue.xzMomentumMultiplier,
                    ),
                )

        if (dKnockback.lengthSquared() < hitValue.minimumSize * hitValue.minimumSize) {
            dKnockback.normalize().multiply(hitValue.minimumSize)
        }
        player.velocity = dKnockback
    }

    /**
     * Applies vanilla knockback from a player's projectile.
     *
     * @param aGP the shooter of the projectile
     * @param projectile the projectile that hit the player
     * @param hitValue the HitValue to use
     */
    fun knock(
        projectile: Projectile,
        aGP: GamePlayer,
        hitValue: HitValue,
    ) {
        val dKnockback =
            projectile.velocity
                .normalize()
                .setY(if ((player as Entity).isOnGround) hitValue.initialY else hitValue.initialY * sign(aGP.player.location.direction.y))
                // cumulative damage
                .multiply(
                    Vector(
                        damage * hitValue.xzDamageMultiplier,
                        damage * hitValue.yDamageMultiplier,
                        damage * hitValue.xzDamageMultiplier,
                    ),
                )

        if (dKnockback.lengthSquared() < hitValue.minimumSize * hitValue.minimumSize) {
            dKnockback.normalize().multiply(hitValue.minimumSize)
        }
        player.velocity = dKnockback
    }

    /**
     * Does an extra midair jump. The maximum is 3 (until [resetJump] is called)
     */
    fun jump() {
        player.isFlying = false
        player.velocity =
            player.location.direction
                .multiply(1.1)
                .setY(1.15)
        if (jumpPhase == 1) player.allowFlight = false
        jumpPhase += 1
    }

    /**
     * Makes a player able to jump again.
     */
    fun resetJump() {
        jumpPhase = 0
        player.allowFlight = true
    }

    /**
     * Kills a player to prepare them for respawn. Called by [Game.kill]
     *
     * @param respawnLocation an available respawn location
     */
    fun kill(respawnLocation: Location?) {
        lives -= 1
        damage = 0.0
        player.health = 20.0
        player.setAbsorptionHearts(lives * 2.0f)
        updateDisplays()

        // dead check
        if (lives == 0) {
            state = PlayerState.SPECTATING
            if (game.getActivePlayers().size == 1) return // let Game handle the end routine
            player.gameMode = GameMode.SPECTATOR
            player.clearInventory()
            player.sendMessage("${RED}You have been eliminated! You are now a spectator.")
        } else {
            player.gameMode = GameMode.SPECTATOR
            require(respawnLocation != null)
            respawnPoint = respawnLocation
            waitRespawn = true
            // wait for respawn by Game
        }
    }

    /**
     * Respawns the player to the point set in [respawnPoint].
     * Will do nothing if respawnPoint is null.
     */
    fun respawn() {
        val rl = respawnPoint ?: return
        player.gameMode = GameMode.ADVENTURE
        kit?.replenish(player)
        player.teleport(Location(rl.world, rl.blockX + 0.5, rl.blockY + 1.0, rl.blockZ + 0.5, 0f, 90f))
        player.playSound(rl, Sound.NOTE_PIANO, 1.0f, 1.9f)
        waitRespawn = false
    }

    /**
     * Sets a player into the ending state.
     */
    fun end() {
        state = PlayerState.ENDING
        player.gameMode = GameMode.ADVENTURE
        player.clearInventory()
    }

    /**
     * Deinitializes this player and restores them to normal.
     */
    fun deinit() {
        player.setAbsorptionHearts(0f)
        player.health = 20.0
        player.removePotionEffect(PotionEffectType.JUMP)
        board.delete()
        player.allowFlight = false
        player.isFlying = false
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        player.gameMode = Config.lobbyGameMode
        player.clearInventory()
    }

    /**
     * Updates a player's displays. Only usable after game has counted down.
     */
    fun updateDisplays() {
        sbTeam.suffix = if (lives > 0) " $lifeString" else " $GRAY[DEAD]"
        sbHealth.getScore(player.name).score = damage.roundToInt()
        ActionBar(actionBarDisplay).send(player)
    }

    /**
     * Represents player state
     */
    enum class PlayerState {
        WAITING,
        IN_GAME,
        SPECTATING,

        /**
         * Does not exist (left midgame)
         */
        GHOST,
        ENDING,
    }
}
