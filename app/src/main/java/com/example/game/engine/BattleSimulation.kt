package com.example.game.engine

import com.example.game.audio.SfxType
import com.example.game.audio.Ww1AudioEngine
import com.example.game.model.CampaignChapter
import com.example.game.model.SoldierClass
import com.example.game.model.TheatreBiome
import com.example.game.model.UniformStyle
import com.example.game.model.WeaponSpec
import com.example.game.model.WeaponType
import com.example.game.render3d.Math3D
import com.example.game.render3d.SectorFlagSpot
import com.example.game.render3d.Vec3
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class SoldierEntity(
    val id: Int,
    val isEntente: Boolean,
    var x: Float,
    var y: Float,
    var z: Float,
    var yawDeg: Float,
    var targetX: Float,
    var targetZ: Float,
    var hp: Float,
    val maxHp: Float,
    var stridePhase: Float = 0f,
    var isMoving: Boolean = false,
    var isCrouching: Boolean = false,
    var isDead: Boolean = false,
    var deathTimer: Float = 0f,
    var fireCooldown: Float = 1.2f,
    var muzzleFlashTimer: Float = 0f
)

data class TracerVfx(
    var x: Float,
    var y: Float,
    var z: Float,
    val vx: Float,
    val vy: Float,
    val vz: Float,
    var life: Float,
    val isEnemy: Boolean
)

data class BurstVfx(
    var x: Float,
    var y: Float,
    var z: Float,
    var vx: Float,
    var vy: Float,
    var vz: Float,
    var size: Float,
    var life: Float,
    val maxLife: Float,
    val r: Float,
    val g: Float,
    val b: Float
)

data class SectorState(
    val spot: SectorFlagSpot,
    var captureProgress: Float = 0f, // 0 = enemy, 1 = captured
    var isCaptured: Boolean = false
)

enum class BattleOutcome {
    PLAYING,
    VICTORY,
    DEFEAT
}

data class HudSnapshot(
    val chapterTitle: String = "",
    val objectiveText: String = "",
    val health: Int = 100,
    val maxHealth: Int = 100,
    val ammoInMag: Int = 8,
    val reserveAmmo: Int = 48,
    val isReloading: Boolean = false,
    val reloadProgress: Float = 0f,
    val isAds: Boolean = false,
    val isScoped: Boolean = false,
    val isCrouching: Boolean = false,
    val kills: Int = 0,
    val targetKills: Int = 12,
    val sectorsCaptured: Int = 0,
    val totalSectors: Int = 2,
    val activeSectorName: String? = null,
    val activeSectorProgress: Float = 0f,
    val hitMarkerAlpha: Float = 0f,
    val killMarker: Boolean = false,
    val damageFlashAlpha: Float = 0f,
    val specialReadyRatio: Float = 1f,
    val specialNameRu: String = "ГРАНАТА",
    val whistleActive: Boolean = false,
    val battleFeedText: String = "",
    val outcome: BattleOutcome = BattleOutcome.PLAYING,
    val elapsedSec: Int = 0,
    val earnedXp: Int = 0,
    val earnedStars: Int = 0
)

class BattleSimulation(
    val chapter: CampaignChapter,
    val soldierClass: SoldierClass,
    val weapon: WeaponSpec,
    val uniform: UniformStyle,
    val perkId: String,
    private val audio: Ww1AudioEngine
) {
    private val rng = Random(System.currentTimeMillis())

    // Player state
    var playerX = 0f
    var playerY = 0f
    var playerZ = 18.2f
    var playerYaw = 0f   // 0 = looking North (-Z)
    var playerPitch = -3f
    var health = soldierClass.maxHealth.toFloat()
    var maxHealth = soldierClass.maxHealth.toFloat()

    var ammoInMag = weapon.magazineSize
    var reserveAmmo = weapon.reserveAmmo
    var isReloading = false
    var reloadTimer = 0f
    var fireCooldown = 0f
    var boltAnimTimer = 0f
    private var boltClickPlayed = false
    var recoilOffset = 0f
    var weaponBobPhase = 0f
    var muzzleFlashTimer = 0f

    var isCrouching = false
    var isAds = false
    var isFiringHeld = false
    var moveInputX = 0f
    var moveInputY = 0f // +1 forward, -1 backward

    var specialCooldownRemaining = 0f
    var whistleTimer = 0f
    var hitMarkerTimer = 0f
    var killMarkerFlag = false
    var damageFlashTimer = 0f
    var cameraShake = 0f
    var smokeTimer = 0f
    private var regenCooldown = 0f

    var kills = 0
    var elapsedTime = 0f
    var outcome = BattleOutcome.PLAYING
    private var battleFeed = chapter.objectiveTitleRu
    private var feedTimer = 6f
    private var artilleryTimer = 5.5f
    private var shipSmokeTimer = 0f
    private var nextEntityId = 1
    private var enemyRespawnTimer = 1.5f

    var lookSensitivity = 1.0f
    var aimAssistEnabled = true
    var invertY = false

    val soldiers = mutableListOf<SoldierEntity>()
    val tracers = mutableListOf<TracerVfx>()
    val particles = mutableListOf<BurstVfx>()
    val sectors = mutableListOf<SectorState>()

    init {
        playerY = Math3D.terrainHeightAt(playerX, playerZ, chapter.theatre)
        sectors.add(SectorState(SectorFlagSpot(0, "СЕКТОР A", 0f, 0f)))
        sectors.add(SectorState(SectorFlagSpot(1, "СЕКТОР B", 2f, -20f)))
        if (chapter.sectorsToCapture >= 3) {
            sectors.add(SectorState(SectorFlagSpot(2, "СЕКТОР C", -1f, -35f)))
        }

        // Spawn 4 Entente allied squadmates in the friendly trench
        for (i in 0 until 4) {
            val sx = -9f + i * 6f
            val sz = 18.0f + (i % 2) * 1.1f
            soldiers.add(
                SoldierEntity(
                    id = nextEntityId++,
                    isEntente = true,
                    x = sx,
                    y = Math3D.terrainHeightAt(sx, sz, chapter.theatre),
                    z = sz,
                    yawDeg = 180f,
                    targetX = sx,
                    targetZ = 10f,
                    hp = 120f,
                    maxHp = 120f
                )
            )
        }

        // Spawn initial enemy defenders along no-man's-land and enemy trench
        for (i in 0 until 7) {
            spawnEnemy(initialWave = true, idx = i)
        }
    }

    private fun spawnEnemy(initialWave: Boolean = false, idx: Int = 0) {
        val sx = (rng.nextFloat() * 34f) - 17f
        val sz = if (initialWave) {
            if (idx < 3) -2f - rng.nextFloat() * 6f else -19f - rng.nextFloat() * 8f
        } else {
            -22f - rng.nextFloat() * 16f
        }
        val baseHp = if (chapter.index >= 2) 105f else 90f
        soldiers.add(
            SoldierEntity(
                id = nextEntityId++,
                isEntente = false,
                x = sx,
                y = Math3D.terrainHeightAt(sx, sz, chapter.theatre),
                z = sz,
                yawDeg = 0f,
                targetX = (sx + rng.nextFloat() * 8f - 4f).coerceIn(-20f, 20f),
                targetZ = (sz + 6f).coerceAtMost(8f),
                hp = baseHp,
                maxHp = baseHp,
                fireCooldown = 1.0f + rng.nextFloat() * 1.5f
            )
        )
    }

    fun eyePosition(): Vec3 {
        val eyeHeight = if (isCrouching) 1.12f else 1.62f
        val shakeX = if (cameraShake > 0f) (rng.nextFloat() - 0.5f) * cameraShake * 0.14f else 0f
        val shakeY = if (cameraShake > 0f) (rng.nextFloat() - 0.5f) * cameraShake * 0.14f else 0f
        return Vec3(playerX + shakeX, playerY + eyeHeight + shakeY, playerZ)
    }

    fun applyLookDelta(dxPx: Float, dyPx: Float) {
        val sens = 0.16f * lookSensitivity * (if (isAds) 0.55f else 1f)
        playerYaw = (playerYaw + dxPx * sens) % 360f
        val ySign = if (invertY) 1f else -1f
        playerPitch = (playerPitch + dyPx * sens * ySign).coerceIn(-68f, 68f)
    }

    fun triggerSingleShot() {
        if (outcome != BattleOutcome.PLAYING) return
        if (fireCooldown <= 0f && !isReloading) {
            firePlayerWeapon()
        }
    }

    fun startReload() {
        if (outcome != BattleOutcome.PLAYING || isReloading) return
        if (ammoInMag >= weapon.magazineSize || reserveAmmo <= 0) return
        isReloading = true
        val fastMult = if (perkId == "fast_bolt") 0.8f else 1.0f
        reloadTimer = weapon.reloadDurationSec * fastMult
        isAds = false
        audio.playSfx(SfxType.RELOAD)
    }

    fun triggerSpecialAbility() {
        if (outcome != BattleOutcome.PLAYING || specialCooldownRemaining > 0f) return
        specialCooldownRemaining = soldierClass.specialCooldownSec

        when (soldierClass) {
            SoldierClass.OFFICER -> {
                audio.playSfx(SfxType.WHISTLE)
                whistleTimer = 6.0f
                val aim = Math3D.forwardFromYawPitch(playerYaw, playerPitch)
                val targetX = (playerX + aim.x * 20f).coerceIn(-25f, 25f)
                val targetZ = (playerZ + aim.z * 20f).coerceIn(-40f, 20f)
                triggerArtilleryStrikeAt(targetX, targetZ, isPlayerCall = true)
                battleFeed = "АРТУДАР НАВЕДЁН! ВПЕРЁД, В АТАКУ!"
                feedTimer = 4f
            }
            SoldierClass.ENGINEER -> {
                health = (health + 45f).coerceAtMost(maxHealth)
                reserveAmmo += weapon.magazineSize * 2
                audio.playSfx(SfxType.RELOAD)
                battleFeed = "ПОЛЕВОЙ КОМПЛЕКТ: +45 ОЗ И БОЕПРИПАСЫ"
                feedTimer = 3.5f
            }
            SoldierClass.SNIPER -> {
                smokeTimer = 6.5f
                health = (health + 25f).coerceAtMost(maxHealth)
                val p = eyePosition()
                spawnSmokeCluster(p.x, playerY + 0.8f, p.z - 4f, 12)
                audio.playSfx(SfxType.GRENADE_BLAST)
                battleFeed = "ДЫМОВАЯ ЗАВЕСА ПОСТАВЛЕНА"
                feedTimer = 3.5f
            }
            else -> {
                // Throw offensive grenade forward
                val aim = Math3D.forwardFromYawPitch(playerYaw, playerPitch)
                val gx = (playerX + aim.x * 13f).coerceIn(-28f, 28f)
                val gz = (playerZ + aim.z * 13f).coerceIn(-42f, 24f)
                detonateExplosion(gx, Math3D.terrainHeightAt(gx, gz, chapter.theatre), gz, radius = 6.2f, maxDmg = 135f, isPlayer = true)
                audio.playSfx(SfxType.GRENADE_BLAST)
                battleFeed = "ГРАНАТА БРОШЕНА!"
                feedTimer = 2.5f
            }
        }
    }

    fun step(dt: Float, isMenuPreview: Boolean) {
        if (isMenuPreview) {
            stepAtmosphereOnly(dt)
            return
        }
        if (outcome != BattleOutcome.PLAYING) {
            stepParticles(dt)
            return
        }

        elapsedTime += dt
        if (fireCooldown > 0f) fireCooldown -= dt
        if (muzzleFlashTimer > 0f) muzzleFlashTimer -= dt
        if (specialCooldownRemaining > 0f) specialCooldownRemaining = (specialCooldownRemaining - dt).coerceAtLeast(0f)
        if (whistleTimer > 0f) whistleTimer = (whistleTimer - dt).coerceAtLeast(0f)
        if (hitMarkerTimer > 0f) hitMarkerTimer = (hitMarkerTimer - dt).coerceAtLeast(0f)
        if (damageFlashTimer > 0f) damageFlashTimer = (damageFlashTimer - dt).coerceAtLeast(0f)
        if (cameraShake > 0f) cameraShake = (cameraShake - dt * 2.5f).coerceAtLeast(0f)
        if (smokeTimer > 0f) smokeTimer = (smokeTimer - dt).coerceAtLeast(0f)
        if (feedTimer > 0f) feedTimer -= dt
        if (regenCooldown > 0f) regenCooldown -= dt

        // Bolt-action cycling animation after rifle shot
        if (boltAnimTimer > 0f) {
            boltAnimTimer = (boltAnimTimer - dt).coerceAtLeast(0f)
            if (!boltClickPlayed && boltAnimTimer < 0.34f) {
                boltClickPlayed = true
                audio.playSfx(SfxType.BOLT_CYCLE)
            }
        }

        recoilOffset *= (1f - (dt * 11f).coerceAtMost(0.95f))

        // Reload completion
        if (isReloading) {
            reloadTimer -= dt
            if (reloadTimer <= 0f) {
                val needed = weapon.magazineSize - ammoInMag
                val load = needed.coerceAtMost(reserveAmmo)
                ammoInMag += load
                reserveAmmo -= load
                isReloading = false
            }
        } else if (isFiringHeld && weapon.isAutomatic && fireCooldown <= 0f) {
            firePlayerWeapon()
        }

        // Player movement
        val perkSpeed = if (perkId == "bayonet_charge") 1.10f else 1.0f
        val whistleMult = if (whistleTimer > 0f) 1.15f else 1.0f
        val crouchMult = if (isCrouching) 0.56f else if (isAds) 0.72f else 1.0f
        val speed = soldierClass.moveSpeed * perkSpeed * whistleMult * crouchMult

        val fwd = Math3D.forwardFromYawPitch(playerYaw, 0f)
        val right = Math3D.rightFromYaw(playerYaw)
        val moveVec = Vec3(
            fwd.x * moveInputY + right.x * moveInputX,
            0f,
            fwd.z * moveInputY + right.z * moveInputX
        )
        val moveMag = moveVec.length()
        if (moveMag > 0.05f) {
            val dir = moveVec * (1f / moveMag.coerceAtLeast(1f))
            playerX = (playerX + dir.x * speed * dt).coerceIn(-25f, 25f)
            playerZ = (playerZ + dir.z * speed * dt).coerceIn(-40f, 23f)
            weaponBobPhase += dt * (if (isCrouching) 5.5f else 9.2f) * moveMag.coerceAtMost(1f)
        } else {
            weaponBobPhase += dt * 1.4f
        }
        playerY = Math3D.terrainHeightAt(playerX, playerZ, chapter.theatre)

        // Field medic / cover health regen
        if (regenCooldown <= 0f && health < maxHealth) {
            val regenRate = if (isCrouching) 8.5f else 2.5f
            val perkBonus = if (perkId == "field_medic") 1.6f else 1.0f
            health = (health + regenRate * perkBonus * dt).coerceAtMost(maxHealth)
        }

        // Ambient coastal ship smoke & periodic battlefield artillery
        stepAtmosphereOnly(dt)

        // Update AI soldiers & waves
        stepSoldiers(dt)
        stepTracers(dt)
        stepParticles(dt)
        stepSectors(dt)

        // Check win / lose conditions
        val capturedCount = sectors.count { it.isCaptured }
        if (capturedCount >= sectors.size && kills >= chapter.targetKills) {
            outcome = BattleOutcome.VICTORY
            battleFeed = "ПОБЕДА! ВСЕ СЕКТОРЫ УДЕРЖАНЫ!"
        } else if (health <= 0f) {
            health = 0f
            outcome = BattleOutcome.DEFEAT
            battleFeed = "БОЕЦ ПАЛ НА ПОЛЕ БОЯ"
        }
    }

    private fun stepAtmosphereOnly(dt: Float) {
        if (chapter.theatre == TheatreBiome.COASTAL_LANDING) {
            shipSmokeTimer -= dt
            if (shipSmokeTimer <= 0f) {
                shipSmokeTimer = 0.25f
                particles.add(
                    BurstVfx(
                        x = -20f + rng.nextFloat() * 0.6f,
                        y = 8.2f,
                        z = 66f,
                        vx = 0.8f + rng.nextFloat() * 0.6f,
                        vy = 1.9f + rng.nextFloat() * 0.7f,
                        vz = -0.3f,
                        size = 1.3f,
                        life = 4.2f,
                        maxLife = 4.2f,
                        r = 0.28f, g = 0.29f, b = 0.31f
                    )
                )
            }
        }

        artilleryTimer -= dt
        if (artilleryTimer <= 0f) {
            artilleryTimer = 5.5f + rng.nextFloat() * 5.0f
            val ax = (rng.nextFloat() * 34f) - 17f
            val az = -26f + rng.nextFloat() * 36f
            val ay = Math3D.terrainHeightAt(ax, az, chapter.theatre)
            detonateExplosion(ax, ay, az, radius = 4.5f, maxDmg = 32f, isPlayer = false)
            audio.playSfx(SfxType.ARTILLERY_BOOM)
        }
    }

    private fun triggerArtilleryStrikeAt(tx: Float, tz: Float, isPlayerCall: Boolean) {
        for (i in 0 until 3) {
            val ox = tx + (rng.nextFloat() - 0.5f) * 6.5f
            val oz = tz + (rng.nextFloat() - 0.5f) * 6.5f
            val oy = Math3D.terrainHeightAt(ox, oz, chapter.theatre)
            detonateExplosion(ox, oy, oz, radius = 7.2f, maxDmg = 140f, isPlayer = isPlayerCall)
        }
        audio.playSfx(SfxType.ARTILLERY_BOOM)
    }

    private fun detonateExplosion(
        ex: Float, ey: Float, ez: Float,
        radius: Float, maxDmg: Float, isPlayer: Boolean
    ) {
        // Spawn fireball + mud/smoke particles
        for (i in 0 until 16) {
            val isFire = i < 6
            particles.add(
                BurstVfx(
                    x = ex + (rng.nextFloat() - 0.5f) * 1.4f,
                    y = ey + 0.3f,
                    z = ez + (rng.nextFloat() - 0.5f) * 1.4f,
                    vx = (rng.nextFloat() - 0.5f) * 5.5f,
                    vy = 2.5f + rng.nextFloat() * 6.5f,
                    vz = (rng.nextFloat() - 0.5f) * 5.5f,
                    size = if (isFire) 1.0f + rng.nextFloat() * 0.8f else 0.7f + rng.nextFloat() * 0.9f,
                    life = if (isFire) 0.55f else 1.6f,
                    maxLife = if (isFire) 0.55f else 1.6f,
                    r = if (isFire) 1.0f else 0.25f,
                    g = if (isFire) 0.52f else 0.22f,
                    b = if (isFire) 0.14f else 0.19f
                )
            )
        }

        // Camera shake near blast
        val distToPlayer = sqrt((playerX - ex) * (playerX - ex) + (playerZ - ez) * (playerZ - ez))
        if (distToPlayer < radius * 2.2f) {
            cameraShake = (1f - distToPlayer / (radius * 2.2f)).coerceIn(0.2f, 1f)
        }

        // Splash damage to soldiers
        for (s in soldiers) {
            if (s.isDead) continue
            val d = sqrt((s.x - ex) * (s.x - ex) + (s.z - ez) * (s.z - ez))
            if (d < radius) {
                if (isPlayer && !s.isEntente) {
                    val dmg = maxDmg * (1f - d / radius)
                    damageEnemy(s, dmg, isHeadshot = false)
                } else if (!isPlayer && !s.isEntente && d < radius * 0.4f) {
                    damageEnemy(s, maxDmg * 0.4f, isHeadshot = false)
                }
            }
        }
    }

    private fun spawnSmokeCluster(cx: Float, cy: Float, cz: Float, count: Int) {
        for (i in 0 until count) {
            particles.add(
                BurstVfx(
                    x = cx + (rng.nextFloat() - 0.5f) * 3.5f,
                    y = cy,
                    z = cz + (rng.nextFloat() - 0.5f) * 3.5f,
                    vx = (rng.nextFloat() - 0.5f) * 1.4f,
                    vy = 0.6f + rng.nextFloat() * 1.1f,
                    vz = (rng.nextFloat() - 0.5f) * 1.4f,
                    size = 1.8f,
                    life = 4.5f,
                    maxLife = 4.5f,
                    r = 0.72f, g = 0.75f, b = 0.78f
                )
            )
        }
    }

    private fun firePlayerWeapon() {
        if (ammoInMag <= 0) {
            startReload()
            return
        }

        ammoInMag -= 1
        fireCooldown = weapon.fireIntervalSec
        muzzleFlashTimer = 0.06f

        val steadyMult = if (perkId == "steady_aim") 0.72f else 1.0f
        recoilOffset = (if (weapon.type == WeaponType.BOLT_RIFLE || weapon.type == WeaponType.SCOPED_RIFLE) 0.14f else 0.06f) * steadyMult
        playerPitch = (playerPitch + (if (weapon.isAutomatic) 0.9f else 2.1f) * steadyMult).coerceAtMost(68f)

        if (weapon.type == WeaponType.BOLT_RIFLE || weapon.type == WeaponType.SCOPED_RIFLE) {
            boltAnimTimer = 0.52f
            boltClickPlayed = false
        }

        val sfx = when (weapon.type) {
            WeaponType.BOLT_RIFLE -> SfxType.RIFLE_SHOT
            WeaponType.TRENCH_SMG -> SfxType.SMG_SHOT
            WeaponType.LIGHT_MG -> SfxType.LMG_SHOT
            WeaponType.SCOPED_RIFLE -> SfxType.SNIPER_SHOT
            WeaponType.OFFICER_REVOLVER -> SfxType.REVOLVER_SHOT
        }
        audio.playSfx(sfx)

        val origin = eyePosition()
        var dir = Math3D.forwardFromYawPitch(playerYaw, playerPitch)

        // Subtle mobile Aim Assist magnetism when ADS or close to center
        if (aimAssistEnabled) {
            dir = applyAimAssist(origin, dir)
        }

        // Apply weapon spread
        val spread = weapon.baseSpread * (if (isAds) 0.25f else 1.0f) * (if (isCrouching) 0.65f else 1.0f)
        val shotDir = Vec3(
            dir.x + (rng.nextFloat() - 0.5f) * spread,
            dir.y + (rng.nextFloat() - 0.5f) * spread,
            dir.z + (rng.nextFloat() - 0.5f) * spread
        ).normalized()

        // Spawn visible 3D bullet tracer
        val tracerStart = origin + shotDir * 1.1f + Vec3(0f, -0.06f, 0f)
        tracers.add(
            TracerVfx(
                x = tracerStart.x, y = tracerStart.y, z = tracerStart.z,
                vx = shotDir.x * 95f, vy = shotDir.y * 95f, vz = shotDir.z * 95f,
                life = 0.45f,
                isEnemy = false
            )
        )

        // Instant hitscan ray check against enemy soldiers (head + torso spheres)
        var bestEnemy: SoldierEntity? = null
        var bestDist = 140f
        var bestHeadshot = false

        for (e in soldiers) {
            if (e.isEntente || e.isDead) continue
            val headY = e.y + (if (e.isCrouching) 1.24f else 1.54f)
            val torsoY = e.y + (if (e.isCrouching) 0.85f else 1.08f)

            val dHead = Math3D.raySphereDistance(origin, shotDir, Vec3(e.x, headY, e.z), 0.25f)
            if (dHead > 0f && dHead < bestDist) {
                bestDist = dHead
                bestEnemy = e
                bestHeadshot = true
            }
            val dTorso = Math3D.raySphereDistance(origin, shotDir, Vec3(e.x, torsoY, e.z), 0.46f)
            if (dTorso > 0f && dTorso < bestDist) {
                bestDist = dTorso
                bestEnemy = e
                bestHeadshot = false
            }
        }

        if (bestEnemy != null) {
            val mult = if (bestHeadshot) 2.1f else 1.0f
            damageEnemy(bestEnemy, weapon.damage * mult, bestHeadshot)
        }

        if (ammoInMag == 0 && reserveAmmo > 0) {
            startReload()
        }
    }

    private fun applyAimAssist(origin: Vec3, dir: Vec3): Vec3 {
        var bestDir = dir
        var bestDot = if (isAds) 0.992f else 0.985f
        for (e in soldiers) {
            if (e.isEntente || e.isDead) continue
            val center = Vec3(e.x, e.y + 1.15f, e.z)
            val toEnemy = (center - origin)
            val dist = toEnemy.length()
            if (dist > 65f) continue
            val nDir = toEnemy.normalized()
            val dot = dir.dot(nDir)
            if (dot > bestDot) {
                bestDot = dot
                bestDir = Vec3(
                    dir.x * 0.45f + nDir.x * 0.55f,
                    dir.y * 0.45f + nDir.y * 0.55f,
                    dir.z * 0.45f + nDir.z * 0.55f
                ).normalized()
            }
        }
        return bestDir
    }

    private fun damageEnemy(enemy: SoldierEntity, dmg: Float, isHeadshot: Boolean) {
        if (enemy.isDead) return
        enemy.hp -= dmg
        hitMarkerTimer = 0.24f
        audio.playSfx(SfxType.HIT_MARKER)

        // Dust/hit spark
        particles.add(
            BurstVfx(
                x = enemy.x, y = enemy.y + 1.2f, z = enemy.z,
                vx = (rng.nextFloat() - 0.5f) * 2f, vy = 1.5f, vz = (rng.nextFloat() - 0.5f) * 2f,
                size = 0.35f, life = 0.35f, maxLife = 0.35f,
                r = 0.85f, g = 0.25f, b = 0.18f
            )
        )

        if (enemy.hp <= 0f) {
            enemy.hp = 0f
            enemy.isDead = true
            enemy.deathTimer = 0f
            kills += 1
            killMarkerFlag = true
            battleFeed = if (isHeadshot) "МЕТКИЙ ВЫСТРЕЛ В ГОЛОВУ! ($kills/${chapter.targetKills})"
            else "ПРОТИВНИК НЕЙТРАЛИЗОВАН ($kills/${chapter.targetKills})"
            feedTimer = 2.6f
        } else {
            killMarkerFlag = false
        }
    }

    private fun stepSoldiers(dt: Float) {
        val activeEnemies = soldiers.count { !it.isEntente && !it.isDead }
        if (activeEnemies < 6 && (kills + activeEnemies) < chapter.targetKills + 4) {
            enemyRespawnTimer -= dt
            if (enemyRespawnTimer <= 0f) {
                enemyRespawnTimer = 2.4f
                spawnEnemy(initialWave = false)
            }
        }

        val iterator = soldiers.iterator()
        while (iterator.hasNext()) {
            val s = iterator.next()
            if (s.muzzleFlashTimer > 0f) s.muzzleFlashTimer -= dt
            if (s.isDead) {
                s.deathTimer += dt * 2.4f
                if (s.deathTimer > 6.0f && !s.isEntente) {
                    iterator.remove()
                }
                continue
            }

            // Pick target (allies seek nearest enemy; enemies seek player or nearest ally)
            if (!s.isEntente) {
                val dxP = playerX - s.x
                val dzP = playerZ - s.z
                val distPlayer = sqrt(dxP * dxP + dzP * dzP)

                // Move toward tactical cover target
                val dxT = s.targetX - s.x
                val dzT = s.targetZ - s.z
                val distT = sqrt(dxT * dxT + dzT * dzT)
                if (distT > 0.8f) {
                    val spd = 2.35f
                    s.x = (s.x + (dxT / distT) * spd * dt).coerceIn(-22f, 22f)
                    s.z = (s.z + (dzT / distT) * spd * dt).coerceIn(-38f, 16f)
                    s.stridePhase += dt * 7.5f
                    s.isMoving = true
                    s.isCrouching = false
                } else {
                    s.isMoving = false
                    s.isCrouching = (s.id % 2 == 0)
                    if (rng.nextFloat() < dt * 0.25f) {
                        s.targetX = (s.x + rng.nextFloat() * 10f - 5f).coerceIn(-20f, 20f)
                        s.targetZ = (s.z + rng.nextFloat() * 6f - 2f).coerceIn(-34f, 14f)
                    }
                }
                s.y = Math3D.terrainHeightAt(s.x, s.z, chapter.theatre)
                s.yawDeg = (atan2(dxP, dzP) * 180f / Math.PI.toFloat())

                s.fireCooldown -= dt
                if (s.fireCooldown <= 0f && distPlayer < 48f) {
                    s.fireCooldown = 1.6f + rng.nextFloat() * 1.9f
                    s.muzzleFlashTimer = 0.08f
                    fireEnemyShot(s, distPlayer)
                }
            } else {
                // Allied AI follows player's general advance toward next uncaptured sector
                val nextSector = sectors.firstOrNull { !it.isCaptured }?.spot
                val goalZ = (nextSector?.z ?: playerZ) + 2.5f
                val goalX = ((s.id - 2.5f) * 4.5f).coerceIn(-16f, 16f)
                val dx = goalX - s.x
                val dz = goalZ - s.z
                val dist = sqrt(dx * dx + dz * dz)
                if (dist > 2.2f) {
                    val spd = if (whistleTimer > 0f) 3.6f else 2.6f
                    s.x += (dx / dist) * spd * dt
                    s.z += (dz / dist) * spd * dt
                    s.stridePhase += dt * 8.0f
                    s.isMoving = true
                    s.yawDeg = (atan2(dx, dz) * 180f / Math.PI.toFloat())
                } else {
                    s.isMoving = false
                    s.yawDeg = 180f // face North
                }
                s.y = Math3D.terrainHeightAt(s.x, s.z, chapter.theatre)

                // Shoot at nearest enemy
                s.fireCooldown -= dt
                if (s.fireCooldown <= 0f) {
                    val enemy = soldiers.firstOrNull { !it.isEntente && !it.isDead && absZ(it.z - s.z) < 28f }
                    if (enemy != null) {
                        s.fireCooldown = 1.4f + rng.nextFloat() * 1.4f
                        s.muzzleFlashTimer = 0.07f
                        s.yawDeg = (atan2(enemy.x - s.x, enemy.z - s.z) * 180f / Math.PI.toFloat())
                        tracers.add(
                            TracerVfx(
                                x = s.x, y = s.y + 1.28f, z = s.z,
                                vx = (enemy.x - s.x) * 3.5f, vy = 0.1f, vz = (enemy.z - s.z) * 3.5f,
                                life = 0.25f,
                                isEnemy = false
                            )
                        )
                        if (rng.nextFloat() < 0.24f) {
                            enemy.hp -= 24f
                            if (enemy.hp <= 0f) {
                                enemy.isDead = true
                                kills += 1
                            }
                        }
                    }
                }
            }
        }
    }

    private fun absZ(v: Float) = if (v < 0f) -v else v

    private fun fireEnemyShot(enemy: SoldierEntity, distPlayer: Float) {
        val eye = eyePosition()
        val toPlayer = (eye - Vec3(enemy.x, enemy.y + 1.3f, enemy.z)).normalized()
        tracers.add(
            TracerVfx(
                x = enemy.x, y = enemy.y + 1.3f, z = enemy.z,
                vx = toPlayer.x * 68f, vy = toPlayer.y * 68f, vz = toPlayer.z * 68f,
                life = 0.5f,
                isEnemy = true
            )
        )

        // Cover / smoke / distance reduce enemy hit probability
        var hitChance = (0.34f - distPlayer * 0.0045f).coerceIn(0.08f, 0.34f)
        if (isCrouching) hitChance *= 0.45f
        if (smokeTimer > 0f) hitChance *= 0.28f

        if (rng.nextFloat() < hitChance) {
            val armorMult = if (perkId == "hardened") 0.85f else 1.0f
            val dmg = (8f + rng.nextFloat() * 6f) * armorMult
            health = (health - dmg).coerceAtLeast(0f)
            damageFlashTimer = 0.35f
            regenCooldown = 4.2f
            audio.playSfx(SfxType.HURT)
        }
    }

    private fun stepTracers(dt: Float) {
        val it = tracers.iterator()
        while (it.hasNext()) {
            val t = it.next()
            t.x += t.vx * dt
            t.y += t.vy * dt
            t.z += t.vz * dt
            t.life -= dt
            if (t.life <= 0f) it.remove()
        }
    }

    private fun stepParticles(dt: Float) {
        val it = particles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.z += p.vz * dt
            p.vy -= 1.4f * dt
            p.size += dt * 0.5f
            p.life -= dt
            if (p.life <= 0f) it.remove()
        }
    }

    private fun stepSectors(dt: Float) {
        for (sec in sectors) {
            if (sec.isCaptured) continue
            val dx = playerX - sec.spot.x
            val dz = playerZ - sec.spot.z
            val dPlayer = sqrt(dx * dx + dz * dz)
            if (dPlayer < 6.8f) {
                sec.captureProgress = (sec.captureProgress + dt * 0.22f).coerceIn(0f, 1f)
                if (sec.captureProgress >= 1f) {
                    sec.isCaptured = true
                    audio.playSfx(SfxType.WHISTLE)
                    battleFeed = "${sec.spot.nameRu} ЗАХВАЧЕН!"
                    feedTimer = 4.0f
                }
            }
        }
    }

    fun snapshot(): HudSnapshot {
        val nearbySector = sectors.firstOrNull { sec ->
            !sec.isCaptured &&
                sqrt((playerX - sec.spot.x) * (playerX - sec.spot.x) + (playerZ - sec.spot.z) * (playerZ - sec.spot.z)) < 8.5f
        }
        val capCount = sectors.count { it.isCaptured }
        val activeObj = if (capCount < sectors.size) {
            "Захватить секторы ($capCount/${sectors.size}) • Враги: $kills/${chapter.targetKills}"
        } else {
            "Подавить остатки врага ($kills/${chapter.targetKills})"
        }
        val reloadNorm = if (isReloading) {
            val total = weapon.reloadDurationSec * (if (perkId == "fast_bolt") 0.8f else 1f)
            (1f - reloadTimer / total).coerceIn(0f, 1f)
        } else 0f

        val xpEarned = if (outcome == BattleOutcome.VICTORY) 320 + kills * 20 else kills * 12
        val starsEarned = if (outcome == BattleOutcome.VICTORY) 120 + kills * 5 else kills * 2

        return HudSnapshot(
            chapterTitle = chapter.titleRu,
            objectiveText = activeObj,
            health = health.toInt(),
            maxHealth = maxHealth.toInt(),
            ammoInMag = ammoInMag,
            reserveAmmo = reserveAmmo,
            isReloading = isReloading,
            reloadProgress = reloadNorm,
            isAds = isAds,
            isScoped = isAds && weapon.type == WeaponType.SCOPED_RIFLE,
            isCrouching = isCrouching,
            kills = kills,
            targetKills = chapter.targetKills,
            sectorsCaptured = capCount,
            totalSectors = sectors.size,
            activeSectorName = nearbySector?.spot?.nameRu,
            activeSectorProgress = nearbySector?.captureProgress ?: 0f,
            hitMarkerAlpha = (hitMarkerTimer / 0.24f).coerceIn(0f, 1f),
            killMarker = killMarkerFlag,
            damageFlashAlpha = (damageFlashTimer / 0.35f).coerceIn(0f, 1f),
            specialReadyRatio = (1f - specialCooldownRemaining / soldierClass.specialCooldownSec).coerceIn(0f, 1f),
            specialNameRu = soldierClass.specialAbilityRu,
            whistleActive = whistleTimer > 0f,
            battleFeedText = if (feedTimer > 0f) battleFeed else "",
            outcome = outcome,
            elapsedSec = elapsedTime.toInt(),
            earnedXp = xpEarned,
            earnedStars = starsEarned
        )
    }
}
