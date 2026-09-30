package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float = 1.0f,
    val maxLife: Float = 1.0f,
    var life: Float = 1.0f
)

data class Projectile(
    val id: Long,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val isEnemy: Boolean,
    val damage: Float = 25f,
    val color: Color
)

data class Asteroid(
    val id: Long,
    var x: Float,
    var y: Float,
    val vy: Float,
    val radius: Float,
    var hp: Float,
    val maxHp: Float,
    var rotation: Float = 0f,
    val rotationSpeed: Float
)

data class CyberDrone(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    val vy: Float,
    var hp: Float,
    val maxHp: Float,
    var shootCooldown: Float = 1.5f
)

data class Powerup(
    val id: Long,
    var x: Float,
    var y: Float,
    val type: PowerupType,
    val vy: Float = 120f
)

enum class PowerupType {
    SHIELD_BOOST,
    EMP_NUKE,
    DOUBLE_SCORE,
    QUANTUM_SHARD
}

data class FloatingText(
    val id: Long,
    var x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    var alpha: Float = 1f
)

data class RivalPlayer(
    val id: String,
    val name: String,
    val platform: String, // "PC", "iOS", "Xbox"
    var x: Float,
    var y: Float,
    var hp: Float = 100f,
    var score: Long = 0,
    val color: Color,
    var isAlive: Boolean = true,
    var lastActionText: String? = null
)

class GameEngine(
    val gameMode: String, // "Endless Odyssey", "Multiplayer Arena", "Daily Challenge"
    val shipSkinId: String = "apex_phoenix",
    val trailFxId: String = "plasma_cyan"
) {
    // Player Starship State
    var playerX = 0f
    var playerY = 0f
    var targetX = 0f
    var targetY = 0f
    var playerShield = 100f
    val maxShield = 100f
    var playerEnergy = 0f
    val maxEnergy = 100f
    var isAlive = true
    var score = 0L
    var kills = 0
    var shardsCollected = 0
    var survivalTimeSec = 0
    var scoreMultiplier = 1
    var multiplierTimeLeft = 0f
    var isEmpReady = false

    var arenaWidth = 800f
    var arenaHeight = 1200f
    private var initialized = false

    // Entities
    val projectiles = mutableListOf<Projectile>()
    val asteroids = mutableListOf<Asteroid>()
    val drones = mutableListOf<CyberDrone>()
    val powerups = mutableListOf<Powerup>()
    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()
    val rivals = mutableListOf<RivalPlayer>()

    // Spawning timers
    private var asteroidTimer = 0f
    private var droneTimer = 0f
    private var powerupTimer = 0f
    private var fireCooldown = 0f
    private var nextEntityId = 1L
    private var survivalTimer = 0f

    fun initializeArena(width: Float, height: Float) {
        if (!initialized) {
            arenaWidth = width
            arenaHeight = height
            playerX = width / 2f
            playerY = height * 0.8f
            targetX = playerX
            targetY = playerY

            if (gameMode.contains("Multiplayer")) {
                rivals.clear()
                rivals.add(RivalPlayer("R1", "ValkyrieX", "PC", width * 0.25f, height * 0.78f, 100f, 0, Color(0xFF00E5FF)))
                rivals.add(RivalPlayer("R2", "Kira_Kinetix", "iOS", width * 0.75f, height * 0.78f, 100f, 0, Color(0xFFFF4081)))
                rivals.add(RivalPlayer("R3", "TitanStrike", "Xbox", width * 0.5f, height * 0.86f, 100f, 0, Color(0xFFFFD700)))
            }
            initialized = true
        }
    }

    fun setTargetPosition(x: Float, y: Float) {
        targetX = x.coerceIn(40f, arenaWidth - 40f)
        targetY = y.coerceIn(100f, arenaHeight - 60f)
    }

    fun triggerEmpBlast(): Boolean {
        if (playerEnergy >= 75f && isAlive) {
            playerEnergy = 0f
            isEmpReady = false

            // Blast all asteroids and drones on screen
            for (asteroid in asteroids) {
                spawnExplosion(asteroid.x, asteroid.y, Color(0xFF00F0FF), 12)
                score += 50 * scoreMultiplier
            }
            asteroids.clear()

            for (drone in drones) {
                spawnExplosion(drone.x, drone.y, Color(0xFFFF007F), 15)
                score += 150 * scoreMultiplier
                kills++
            }
            drones.clear()
            projectiles.removeAll { it.isEnemy }

            spawnFloatingText(playerX, playerY - 40f, "EMP HYPER BLAST!", Color(0xFF00F0FF))
            return true
        }
        return false
    }

    fun update(deltaSeconds: Float) {
        if (!initialized || !isAlive) return

        val dt = deltaSeconds.coerceIn(0.001f, 0.05f)

        // Smooth ship lerp
        playerX += (targetX - playerX) * (14f * dt)
        playerY += (targetY - playerY) * (14f * dt)

        // Survival timer
        survivalTimer += dt
        if (survivalTimer >= 1.0f) {
            survivalTimeSec++
            survivalTimer -= 1.0f
            score += 10 * scoreMultiplier
        }

        // Score multiplier decay
        if (scoreMultiplier > 1) {
            multiplierTimeLeft -= dt
            if (multiplierTimeLeft <= 0) {
                scoreMultiplier = 1
                spawnFloatingText(playerX, playerY - 20f, "Normal Speed", Color.Gray)
            }
        }

        // Ship trail particles
        val trailColor = when (trailFxId) {
            "neon_magenta" -> Color(0xFFFF007F)
            "cosmic_gold" -> Color(0xFFFFD700)
            else -> Color(0xFF00F0FF)
        }
        particles.add(
            Particle(
                x = playerX + Random.nextFloat() * 16f - 8f,
                y = playerY + 24f,
                vx = Random.nextFloat() * 20f - 10f,
                vy = 80f + Random.nextFloat() * 60f,
                color = trailColor,
                size = Random.nextFloat() * 6f + 3f,
                life = 0.6f,
                maxLife = 0.6f
            )
        )

        // Auto-firing dual blaster lasers
        fireCooldown -= dt
        if (fireCooldown <= 0f) {
            fireCooldown = 0.16f
            projectiles.add(
                Projectile(
                    id = nextEntityId++,
                    x = playerX - 14f,
                    y = playerY - 20f,
                    vx = 0f,
                    vy = -950f,
                    isEnemy = false,
                    damage = 25f,
                    color = Color(0xFF00F0FF)
                )
            )
            projectiles.add(
                Projectile(
                    id = nextEntityId++,
                    x = playerX + 14f,
                    y = playerY - 20f,
                    vx = 0f,
                    vy = -950f,
                    isEnemy = false,
                    damage = 25f,
                    color = Color(0xFF00F0FF)
                )
            )
        }

        // Update Projectiles
        val projIter = projectiles.iterator()
        while (projIter.hasNext()) {
            val p = projIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            if (p.y < -50f || p.y > arenaHeight + 50f || p.x < -20f || p.x > arenaWidth + 20f) {
                projIter.remove()
            }
        }

        // Asteroids logic
        asteroidTimer -= dt
        val asteroidSpawnRate = if (gameMode.contains("Daily")) 0.7f else 1.1f
        if (asteroidTimer <= 0f) {
            asteroidTimer = asteroidSpawnRate
            val radius = Random.nextFloat() * 24f + 16f
            asteroids.add(
                Asteroid(
                    id = nextEntityId++,
                    x = Random.nextFloat() * (arenaWidth - 60f) + 30f,
                    y = -40f,
                    vy = Random.nextFloat() * 120f + 140f,
                    radius = radius,
                    hp = radius * 1.5f,
                    maxHp = radius * 1.5f,
                    rotationSpeed = Random.nextFloat() * 3f - 1.5f
                )
            )
        }

        val astIter = asteroids.iterator()
        while (astIter.hasNext()) {
            val ast = astIter.next()
            ast.y += ast.vy * dt
            ast.rotation += ast.rotationSpeed * dt

            // Collision with player
            val distToPlayer = hypot(ast.x - playerX, ast.y - playerY)
            if (distToPlayer < ast.radius + 22f) {
                astIter.remove()
                takeDamage(25f)
                spawnExplosion(ast.x, ast.y, Color(0xFFFFA000), 10)
                continue
            }

            // Collision with friendly projectiles
            val pIter = projectiles.iterator()
            var destroyed = false
            while (pIter.hasNext()) {
                val p = pIter.next()
                if (!p.isEnemy) {
                    val dist = hypot(ast.x - p.x, ast.y - p.y)
                    if (dist < ast.radius + 8f) {
                        pIter.remove()
                        ast.hp -= p.damage
                        spawnHitSparks(p.x, p.y, Color(0xFF00F0FF))
                        if (ast.hp <= 0) {
                            destroyed = true
                            break
                        }
                    }
                }
            }

            if (destroyed) {
                astIter.remove()
                score += (ast.radius * 2 * scoreMultiplier).toLong()
                playerEnergy = (playerEnergy + 8f).coerceAtMost(maxEnergy)
                isEmpReady = playerEnergy >= 75f
                spawnExplosion(ast.x, ast.y, Color(0xFFFFB300), 12)
                spawnFloatingText(ast.x, ast.y, "+${(ast.radius * 2 * scoreMultiplier).toInt()}", Color(0xFFFFD54F))

                // Chance to drop powerup
                if (Random.nextFloat() < 0.25f) {
                    val pType = when (Random.nextInt(4)) {
                        0 -> PowerupType.SHIELD_BOOST
                        1 -> PowerupType.EMP_NUKE
                        2 -> PowerupType.DOUBLE_SCORE
                        else -> PowerupType.QUANTUM_SHARD
                    }
                    powerups.add(Powerup(nextEntityId++, ast.x, ast.y, pType))
                }
                continue
            }

            if (ast.y > arenaHeight + 60f) {
                astIter.remove()
            }
        }

        // Cyber Drones logic
        droneTimer -= dt
        if (droneTimer <= 0f) {
            droneTimer = Random.nextFloat() * 2.2f + 2.5f
            drones.add(
                CyberDrone(
                    id = nextEntityId++,
                    x = Random.nextFloat() * (arenaWidth - 80f) + 40f,
                    y = -30f,
                    vx = (if (Random.nextBoolean()) 1 else -1) * (Random.nextFloat() * 80f + 60f),
                    vy = Random.nextFloat() * 60f + 70f,
                    hp = 50f,
                    maxHp = 50f
                )
            )
        }

        val droneIter = drones.iterator()
        while (droneIter.hasNext()) {
            val drone = droneIter.next()
            drone.x += drone.vx * dt
            drone.y += drone.vy * dt

            // Bounce on screen edges
            if (drone.x < 30f || drone.x > arenaWidth - 30f) {
                drone.vx = -drone.vx
            }

            // Drone firing
            drone.shootCooldown -= dt
            if (drone.shootCooldown <= 0f) {
                drone.shootCooldown = 2.0f
                projectiles.add(
                    Projectile(
                        id = nextEntityId++,
                        x = drone.x,
                        y = drone.y + 16f,
                        vx = (playerX - drone.x) * 0.4f,
                        vy = 450f,
                        isEnemy = true,
                        damage = 18f,
                        color = Color(0xFFFF1744)
                    )
                )
            }

            // Collision with friendly projectiles
            val pIter = projectiles.iterator()
            var droneKilled = false
            while (pIter.hasNext()) {
                val p = pIter.next()
                if (!p.isEnemy) {
                    val dist = hypot(drone.x - p.x, drone.y - p.y)
                    if (dist < 26f) {
                        pIter.remove()
                        drone.hp -= p.damage
                        spawnHitSparks(p.x, p.y, Color(0xFFFF007F))
                        if (drone.hp <= 0) {
                            droneKilled = true
                            break
                        }
                    }
                }
            }

            if (droneKilled) {
                droneIter.remove()
                kills++
                score += 150 * scoreMultiplier
                playerEnergy = (playerEnergy + 15f).coerceAtMost(maxEnergy)
                isEmpReady = playerEnergy >= 75f
                spawnExplosion(drone.x, drone.y, Color(0xFFFF007F), 18)
                spawnFloatingText(drone.x, drone.y, "DRONE DESTROYED +150", Color(0xFFFF4081))
                // Guarantee shard drop
                powerups.add(Powerup(nextEntityId++, drone.x, drone.y, PowerupType.QUANTUM_SHARD))
                continue
            }

            if (drone.y > arenaHeight + 50f) {
                droneIter.remove()
            }
        }

        // Enemy projectile hits player
        val enemyPIter = projectiles.iterator()
        while (enemyPIter.hasNext()) {
            val p = enemyPIter.next()
            if (p.isEnemy) {
                val dist = hypot(p.x - playerX, p.y - playerY)
                if (dist < 24f) {
                    enemyPIter.remove()
                    takeDamage(p.damage)
                    spawnHitSparks(p.x, p.y, Color(0xFFFF3D00))
                }
            }
        }

        // Powerups collection
        val powerIter = powerups.iterator()
        while (powerIter.hasNext()) {
            val p = powerIter.next()
            p.y += p.vy * dt
            val dist = hypot(p.x - playerX, p.y - playerY)
            if (dist < 36f) {
                powerIter.remove()
                when (p.type) {
                    PowerupType.SHIELD_BOOST -> {
                        playerShield = (playerShield + 35f).coerceAtMost(maxShield)
                        spawnFloatingText(playerX, playerY - 30f, "SHIELD RECHARGED +35", Color(0xFF00E676))
                    }
                    PowerupType.EMP_NUKE -> {
                        playerEnergy = maxEnergy
                        isEmpReady = true
                        spawnFloatingText(playerX, playerY - 30f, "HYPER EMP READY!", Color(0xFF00F0FF))
                    }
                    PowerupType.DOUBLE_SCORE -> {
                        scoreMultiplier = 2
                        multiplierTimeLeft = 12f
                        spawnFloatingText(playerX, playerY - 30f, "2X SCORE MULTIPLIER!", Color(0xFFFFD700))
                    }
                    PowerupType.QUANTUM_SHARD -> {
                        shardsCollected += 10
                        score += 50 * scoreMultiplier
                        spawnFloatingText(playerX, playerY - 30f, "+10 SHARDS", Color(0xFF00FFA3))
                    }
                }
                spawnExplosion(p.x, p.y, Color(0xFF00FFA3), 8)
                continue
            }
            if (p.y > arenaHeight + 40f) {
                powerIter.remove()
            }
        }

        // Update Rivals in Multiplayer mode
        if (gameMode.contains("Multiplayer")) {
            for (rival in rivals) {
                if (!rival.isAlive) continue

                // Rivals weave around and fight simulated threats
                rival.x += (sin(survivalTimer * 2f + rival.x) * 1.5f).toFloat()
                rival.x = rival.x.coerceIn(30f, arenaWidth - 30f)

                // Rival score increment
                if (Random.nextFloat() < 0.35f) {
                    rival.score += (Random.nextInt(15, 45) * scoreMultiplier).toLong()
                }

                // Chance to take damage from simulated asteroids
                if (Random.nextFloat() < 0.005f) {
                    rival.hp -= Random.nextFloat() * 12f
                    if (rival.hp <= 0f) {
                        rival.hp = 0f
                        rival.isAlive = false
                        spawnFloatingText(rival.x, rival.y, "${rival.name} ELIMINATED!", Color(0xFFFF1744))
                    }
                }

                // Random tactical pings
                if (Random.nextFloat() < 0.003f) {
                    val pings = listOf("Flipping left!", "Laser overdrive!", "Got shield!", "Focus center!")
                    rival.lastActionText = pings.random()
                }
            }
        }

        // Update Floating Texts
        val txtIter = floatingTexts.iterator()
        while (txtIter.hasNext()) {
            val txt = txtIter.next()
            txt.y -= 40f * dt
            txt.alpha -= 0.8f * dt
            if (txt.alpha <= 0f) {
                txtIter.remove()
            }
        }

        // Update Particles
        val partIter = particles.iterator()
        while (partIter.hasNext()) {
            val part = partIter.next()
            part.x += part.vx * dt
            part.y += part.vy * dt
            part.life -= dt
            part.alpha = (part.life / part.maxLife).coerceIn(0f, 1f)
            if (part.life <= 0f) {
                partIter.remove()
            }
        }
    }

    private fun takeDamage(amount: Float) {
        playerShield -= amount
        if (playerShield <= 0f) {
            playerShield = 0f
            isAlive = false
            spawnExplosion(playerX, playerY, Color(0xFFFF1744), 20)
            spawnFloatingText(playerX, playerY, "SHIP DESTROYED!", Color(0xFFFF1744))
        } else {
            spawnFloatingText(playerX, playerY - 30f, "-${amount.toInt()}", Color(0xFFFF5252))
        }
    }

    private fun spawnExplosion(x: Float, y: Float, color: Color, count: Int) {
        if (particles.size > 100) return
        val safeCount = count.coerceAtMost(16)
        for (i in 0 until safeCount) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * 180f + 60f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    size = Random.nextFloat() * 6f + 3f,
                    life = 0.4f,
                    maxLife = 0.4f
                )
            )
        }
    }

    private fun spawnHitSparks(x: Float, y: Float, color: Color) {
        if (particles.size > 110) return
        for (i in 0..3) {
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = Random.nextFloat() * 100f - 50f,
                    vy = Random.nextFloat() * 100f - 50f,
                    color = color,
                    size = 2.5f,
                    life = 0.2f,
                    maxLife = 0.2f
                )
            )
        }
    }

    private fun spawnFloatingText(x: Float, y: Float, text: String, color: Color) {
        if (floatingTexts.size >= 12) {
            floatingTexts.removeAt(0)
        }
        floatingTexts.add(
            FloatingText(
                id = nextEntityId++,
                x = x,
                y = y,
                text = text,
                color = color
            )
        )
    }

    fun restartGame() {
        playerShield = maxShield
        playerEnergy = 0f
        isAlive = true
        score = 0L
        kills = 0
        shardsCollected = 0
        survivalTimeSec = 0
        scoreMultiplier = 1
        isEmpReady = false
        projectiles.clear()
        asteroids.clear()
        drones.clear()
        powerups.clear()
        floatingTexts.clear()
        particles.clear()
        if (gameMode.contains("Multiplayer")) {
            rivals.forEach {
                it.hp = 100f
                it.score = 0
                it.isAlive = true
            }
        }
    }

    fun getPlayerPlacement(): Int {
        if (!gameMode.contains("Multiplayer")) return 1
        val allScores = mutableListOf(score)
        allScores.addAll(rivals.map { it.score })
        allScores.sortDescending()
        return allScores.indexOf(score) + 1
    }
}
