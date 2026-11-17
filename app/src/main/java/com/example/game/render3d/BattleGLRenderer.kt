package com.example.game.render3d

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.game.engine.BattleSimulation
import com.example.game.model.TheatreBiome
import com.example.game.model.WeaponType
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class BattleGLRenderer(
    private val onHudSnapshot: (com.example.game.engine.HudSnapshot) -> Unit
) : GLSurfaceView.Renderer {

    private lateinit var shader: LitShaderProgram
    private lateinit var models: ModelCatalog3D
    private var scene: BuiltBattlefield? = null
    private var builtBiome: TheatreBiome? = null

    @Volatile
    var simulation: BattleSimulation? = null

    @Volatile
    var isMenuPreviewMode: Boolean = true

    @Volatile
    var menuBiome: TheatreBiome = TheatreBiome.COASTAL_LANDING

    private val projMat = FloatArray(16)
    private val viewMat = FloatArray(16)
    private val vpMat = FloatArray(16)
    private val modelMat = FloatArray(16)
    private val mvpMat = FloatArray(16)
    private val identityMat = FloatArray(16)

    private var surfaceWidth = 1
    private var surfaceHeight = 1
    private var lastTimeNs = System.nanoTime()
    private var menuOrbitTime = 0f
    private var hudEmitCounter = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        Matrix.setIdentityM(identityMat, 0)

        shader = LitShaderProgram()
        models = ModelCatalog3D()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        surfaceWidth = width.coerceAtLeast(1)
        surfaceHeight = height.coerceAtLeast(1)
        GLES30.glViewport(0, 0, surfaceWidth, surfaceHeight)
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = System.nanoTime()
        val dt = ((now - lastTimeNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        lastTimeNs = now

        val sim = simulation
        val activeBiome = if (!isMenuPreviewMode && sim != null) sim.chapter.theatre else menuBiome

        if (scene == null || builtBiome != activeBiome) {
            scene?.terrainMesh?.release()
            scene?.oceanMesh?.release()
            val sectorsCount = sim?.chapter?.sectorsToCapture ?: 2
            scene = BattlefieldSceneBuilder.build(activeBiome, sectorsCount)
            builtBiome = activeBiome
        }

        // Atmosphere & lighting per WW1 theatre
        val skyColor: FloatArray
        val sunDir: FloatArray
        val sunColor: FloatArray
        val fogDensity: Float
        when (activeBiome) {
            TheatreBiome.MARNE_TRENCHES -> {
                skyColor = floatArrayOf(0.46f, 0.49f, 0.50f)
                sunDir = floatArrayOf(0.5f, 0.65f, 0.4f)
                sunColor = floatArrayOf(0.95f, 0.88f, 0.74f)
                fogDensity = 0.022f
            }
            TheatreBiome.VERDUN_CRATERS -> {
                skyColor = floatArrayOf(0.36f, 0.34f, 0.33f)
                sunDir = floatArrayOf(-0.4f, 0.55f, 0.35f)
                sunColor = floatArrayOf(0.92f, 0.72f, 0.54f)
                fogDensity = 0.026f
            }
            TheatreBiome.COASTAL_LANDING -> {
                skyColor = floatArrayOf(0.62f, 0.67f, 0.74f)
                sunDir = floatArrayOf(0.6f, 0.70f, 0.55f)
                sunColor = floatArrayOf(1.02f, 0.98f, 0.90f)
                fogDensity = 0.015f
            }
            TheatreBiome.ALPINE_FORTRESS -> {
                skyColor = floatArrayOf(0.55f, 0.61f, 0.68f)
                sunDir = floatArrayOf(-0.55f, 0.75f, 0.35f)
                sunColor = floatArrayOf(0.98f, 0.98f, 1.0f)
                fogDensity = 0.018f
            }
        }

        GLES30.glClearColor(skyColor[0], skyColor[1], skyColor[2], 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Step simulation
        sim?.step(dt, isMenuPreview = isMenuPreviewMode)

        // Camera setup
        val aspect = surfaceWidth.toFloat() / surfaceHeight.toFloat()
        val fov = if (!isMenuPreviewMode && sim != null && sim.isAds) sim.weapon.adsFov else 64f
        Matrix.perspectiveM(projMat, 0, fov, aspect, 0.12f, 180f)

        val camPos: Vec3
        val lookTarget: Vec3
        if (isMenuPreviewMode || sim == null) {
            menuOrbitTime += dt * 0.16f
            camPos = Vec3(
                x = sin(menuOrbitTime) * 15f,
                y = 5.4f + cos(menuOrbitTime * 0.7f) * 1.1f,
                z = 12f + cos(menuOrbitTime) * 9f
            )
            lookTarget = Vec3(0f, 1.0f, -8f)
        } else {
            camPos = sim.eyePosition()
            val dir = Math3D.forwardFromYawPitch(sim.playerYaw, sim.playerPitch)
            lookTarget = camPos + dir * 10f
        }

        Matrix.setLookAtM(
            viewMat, 0,
            camPos.x, camPos.y, camPos.z,
            lookTarget.x, lookTarget.y, lookTarget.z,
            0f, 1f, 0f
        )
        Matrix.multiplyMM(vpMat, 0, projMat, 0, viewMat, 0)

        shader.use()
        GLES30.glUniform3f(shader.uSunDirLoc, sunDir[0], sunDir[1], sunDir[2])
        GLES30.glUniform3f(shader.uSunColorLoc, sunColor[0], sunColor[1], sunColor[2])
        GLES30.glUniform3f(shader.uAmbientTopLoc, skyColor[0] * 0.72f, skyColor[1] * 0.74f, skyColor[2] * 0.78f)
        GLES30.glUniform3f(shader.uAmbientBottomLoc, 0.18f, 0.16f, 0.14f)
        GLES30.glUniform3f(shader.uCameraPosLoc, camPos.x, camPos.y, camPos.z)
        GLES30.glUniform3f(shader.uFogColorLoc, skyColor[0], skyColor[1], skyColor[2])
        GLES30.glUniform1f(shader.uFogDensityLoc, fogDensity)

        val built = scene!!
        // 1. Terrain & Ocean
        drawMeshIdentity(built.terrainMesh)
        built.oceanMesh?.let { drawMeshIdentity(it) }

        // 2. Offshore troopship (Chapter 3 Coastal Landing)
        built.shipPos?.let { sp ->
            val bobY = sin(menuOrbitTime * 2.5f + (sim?.elapsedTime ?: 0f) * 1.1f) * 0.22f
            drawTransformed(models.troopShip, sp.x, sp.y + bobY, sp.z, 18f, 1f)
        }

        // 3. Trench walls, sandbags, barbed wire, ruins, supply crates
        for (prop in built.props) {
            val mesh = when (prop.kind) {
                PropKind.SANDBAG -> models.sandbagWall
                PropKind.TIMBER_WALL -> models.timberWall
                PropKind.BARBED_WIRE -> models.barbedWire
                PropKind.STONE_RUIN -> models.stoneRuin
                PropKind.SUPPLY_CRATE -> models.supplyCrate
            }
            drawTransformed(mesh, prop.x, prop.y, prop.z, prop.yawDeg, prop.scale)
        }

        // 4. Sector Capture Flagpoles & Dynamic Banners
        val sectorList = sim?.sectors
        for (idx in built.sectors.indices) {
            val spot = built.sectors[idx]
            val fy = Math3D.terrainHeightAt(spot.x, spot.z, activeBiome)
            drawTransformed(models.flagPole, spot.x, fy, spot.z, 0f, 1f)
            val captured = sectorList?.getOrNull(idx)?.isCaptured ?: false
            val progress = sectorList?.getOrNull(idx)?.captureProgress ?: 0f
            val r = if (captured) 0.28f else 0.78f - progress * 0.4f
            val g = if (captured) 0.56f else 0.24f + progress * 0.2f
            val b = if (captured) 0.85f else 0.22f + progress * 0.4f
            drawTransformed(models.flagCloth, spot.x, fy, spot.z, (menuOrbitTime * 25f) % 20f - 10f, 1f, r, g, b)
        }

        // 5. Allied & Enemy 3D Articulated Soldiers
        if (sim != null) {
            val ententeHex = sim.uniform.coatColorHex
            val ententeRgb = floatArrayOf(
                ((ententeHex shr 16) and 0xFF) / 255f,
                ((ententeHex shr 8) and 0xFF) / 255f,
                (ententeHex and 0xFF) / 255f
            )
            val enemyRgb = floatArrayOf(0.42f, 0.44f, 0.37f) // Field-grey greatcoats

            for (s in sim.soldiers) {
                Soldier3DDrawer.drawSoldier(
                    shader = shader,
                    models = models,
                    vpMatrix = vpMat,
                    x = s.x,
                    y = s.y,
                    z = s.z,
                    yawDeg = s.yawDeg,
                    stridePhase = s.stridePhase,
                    isMoving = s.isMoving,
                    isCrouching = s.isCrouching,
                    isDead = s.isDead,
                    deathProgress = s.deathTimer,
                    isEntente = s.isEntente,
                    coatRgb = if (s.isEntente) ententeRgb else enemyRgb
                )
                if (s.muzzleFlashTimer > 0f) {
                    val fRad = 0.24f
                    drawTransformed(models.unitSphere, s.x, s.y + 1.25f, s.z, 0f, fRad, 1f, 0.8f, 0.3f)
                }
            }

            // 6. Bullet Tracers & Explosion / Smoke Particles
            for (tr in sim.tracers) {
                drawTransformed(models.tracerBeam, tr.x, tr.y, tr.z, 0f, 0.7f, 1f, if (tr.isEnemy) 0.45f else 0.90f, 0.25f)
            }
            for (pt in sim.particles) {
                val alpha = (pt.life / pt.maxLife).coerceIn(0f, 1f)
                drawTransformed(models.unitSphere, pt.x, pt.y, pt.z, 0f, pt.size, pt.r, pt.g, pt.b, alpha)
            }

            // 7. First-Person 3D Weapon + Hands (skip if scoped sniper zoom overlay is active)
            val isScoped = sim.isAds && sim.weapon.type == WeaponType.SCOPED_RIFLE
            if (!isMenuPreviewMode && !isScoped) {
                drawFirstPersonWeapon(sim)
            }

            // Emit HUD snapshot smoothly every 2 frames (~30Hz)
            hudEmitCounter++
            if (hudEmitCounter >= 2) {
                hudEmitCounter = 0
                onHudSnapshot(sim.snapshot())
            }
        }
    }

    private fun drawFirstPersonWeapon(sim: BattleSimulation) {
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT)
        // View-space rendering (camera at origin looking down -Z)
        GLES30.glUniform3f(shader.uCameraPosLoc, 0f, 0f, 0f)
        GLES30.glUniform1f(shader.uFogDensityLoc, 0.0f)

        val adsT = if (sim.isAds) 1f else 0f
        val bobX = sin(sim.weaponBobPhase) * 0.012f * (1f - adsT * 0.75f)
        val bobY = cos(sim.weaponBobPhase * 2f) * 0.010f * (1f - adsT * 0.75f)
        val reloadTilt = if (sim.isReloading) -36f else 0f
        val reloadDrop = if (sim.isReloading) -0.14f else 0f

        val baseX = 0.21f * (1f - adsT) + 0.0f * adsT + bobX
        val baseY = -0.22f * (1f - adsT) - 0.042f * adsT + bobY + reloadDrop
        val baseZ = -0.44f + sim.recoilOffset

        val weaponMat = FloatArray(16)
        Matrix.setIdentityM(weaponMat, 0)
        Matrix.translateM(weaponMat, 0, baseX, baseY, baseZ)
        Matrix.rotateM(weaponMat, 0, sim.recoilOffset * 48f + reloadTilt, 1f, 0f, 0f)

        val wMesh = when (sim.weapon.type) {
            WeaponType.BOLT_RIFLE -> models.boltRifleBody
            WeaponType.TRENCH_SMG -> models.smgBody
            WeaponType.LIGHT_MG -> models.lmgBody
            WeaponType.SCOPED_RIFLE -> models.sniperRifleBody
            WeaponType.OFFICER_REVOLVER -> models.revolverBody
        }

        // Draw primary weapon body
        Matrix.multiplyMM(mvpMat, 0, projMat, 0, weaponMat, 0)
        GLES30.glUniformMatrix4fv(shader.uModelLoc, 1, false, weaponMat, 0)
        GLES30.glUniformMatrix4fv(shader.uMvpLoc, 1, false, mvpMat, 0)
        GLES30.glUniform4f(shader.uTintColorLoc, 1f, 1f, 1f, 1f)
        wMesh.draw()

        // Draw first-person hands gripping the weapon
        models.fpArms.draw()

        // Draw animated bolt handle for bolt-action rifles (slides back & lifts up!)
        if (sim.weapon.type == WeaponType.BOLT_RIFLE || sim.weapon.type == WeaponType.SCOPED_RIFLE) {
            val boltT = (sim.boltAnimTimer / 0.52f).coerceIn(0f, 1f)
            val cyclePhase = sin(boltT * Math.PI.toFloat())
            val boltMat = FloatArray(16)
            Matrix.setIdentityM(boltMat, 0)
            Matrix.multiplyMM(boltMat, 0, weaponMat, 0, boltMat, 0)
            Matrix.translateM(boltMat, 0, 0.008f, 0.022f, 0.02f + cyclePhase * 0.085f)
            Matrix.rotateM(boltMat, 0, -cyclePhase * 62f, 0f, 0f, 1f)
            Matrix.multiplyMM(mvpMat, 0, projMat, 0, boltMat, 0)
            GLES30.glUniformMatrix4fv(shader.uModelLoc, 1, false, boltMat, 0)
            GLES30.glUniformMatrix4fv(shader.uMvpLoc, 1, false, mvpMat, 0)
            GLES30.glUniform4f(shader.uTintColorLoc, 1f, 1f, 1f, 1f)
            models.boltHandle.draw()
        }

        // Muzzle flash glow at barrel tip
        if (sim.muzzleFlashTimer > 0f) {
            val flashMat = FloatArray(16)
            Matrix.setIdentityM(flashMat, 0)
            Matrix.multiplyMM(flashMat, 0, weaponMat, 0, flashMat, 0)
            Matrix.translateM(flashMat, 0, 0f, 0.025f, -0.76f)
            Matrix.scaleM(flashMat, 0, 0.14f, 0.14f, 0.22f)
            Matrix.multiplyMM(mvpMat, 0, projMat, 0, flashMat, 0)
            GLES30.glUniformMatrix4fv(shader.uModelLoc, 1, false, flashMat, 0)
            GLES30.glUniformMatrix4fv(shader.uMvpLoc, 1, false, mvpMat, 0)
            GLES30.glUniform4f(shader.uTintColorLoc, 1f, 0.82f, 0.35f, 0.85f)
            models.unitSphere.draw()
        }
    }

    private fun drawMeshIdentity(mesh: GpuMesh) {
        GLES30.glUniformMatrix4fv(shader.uModelLoc, 1, false, identityMat, 0)
        GLES30.glUniformMatrix4fv(shader.uMvpLoc, 1, false, vpMat, 0)
        GLES30.glUniform4f(shader.uTintColorLoc, 1f, 1f, 1f, 1f)
        mesh.draw()
    }

    private fun drawTransformed(
        mesh: GpuMesh,
        x: Float, y: Float, z: Float,
        yawDeg: Float,
        scale: Float,
        r: Float = 1f, g: Float = 1f, b: Float = 1f, a: Float = 1f
    ) {
        Matrix.setIdentityM(modelMat, 0)
        Matrix.translateM(modelMat, 0, x, y, z)
        if (yawDeg != 0f) Matrix.rotateM(modelMat, 0, yawDeg, 0f, 1f, 0f)
        if (scale != 1f) Matrix.scaleM(modelMat, 0, scale, scale, scale)
        Matrix.multiplyMM(mvpMat, 0, vpMat, 0, modelMat, 0)
        GLES30.glUniformMatrix4fv(shader.uModelLoc, 1, false, modelMat, 0)
        GLES30.glUniformMatrix4fv(shader.uMvpLoc, 1, false, mvpMat, 0)
        GLES30.glUniform4f(shader.uTintColorLoc, r, g, b, a)
        mesh.draw()
    }
}
