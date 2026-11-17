package com.example.game.render3d

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.game.model.SoldierClass
import com.example.game.model.UniformStyle
import com.example.game.model.WeaponType
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ClassShowcaseRenderer : GLSurfaceView.Renderer {
    private lateinit var shader: LitShaderProgram
    private lateinit var models: ModelCatalog3D

    private val projMat = FloatArray(16)
    private val viewMat = FloatArray(16)
    private val vpMat = FloatArray(16)
    private val modelMat = FloatArray(16)
    private val mvpMat = FloatArray(16)

    @Volatile
    var uniformStyle: UniformStyle = UniformStyle.HORIZON_BLUE

    @Volatile
    var weaponType: WeaponType = WeaponType.BOLT_RIFLE

    @Volatile
    var soldierClass: SoldierClass = SoldierClass.RIFLEMAN

    @Volatile
    var userYawOffset: Float = 0f

    private var autoSpinYaw = 18f
    private var lastTimeNs = System.nanoTime()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.11f, 0.13f, 0.15f, 1f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        shader = LitShaderProgram()
        models = ModelCatalog3D()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.coerceAtLeast(1).toFloat()
        Matrix.perspectiveM(projMat, 0, 38f, aspect, 0.2f, 40f)
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = System.nanoTime()
        val dt = ((now - lastTimeNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        lastTimeNs = now
        autoSpinYaw = (autoSpinYaw + dt * 14f) % 360f

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        shader.use()

        // Camera looking at full-body soldier standing on trench duckboard
        Matrix.setLookAtM(
            viewMat, 0,
            0f, 1.15f, 3.25f,
            0f, 0.92f, 0f,
            0f, 1f, 0f
        )
        Matrix.multiplyMM(vpMat, 0, projMat, 0, viewMat, 0)

        GLES30.glUniform3f(shader.uSunDirLoc, 0.55f, 0.72f, 0.42f)
        GLES30.glUniform3f(shader.uSunColorLoc, 1.05f, 0.96f, 0.84f)
        GLES30.glUniform3f(shader.uAmbientTopLoc, 0.42f, 0.48f, 0.54f)
        GLES30.glUniform3f(shader.uAmbientBottomLoc, 0.20f, 0.18f, 0.16f)
        GLES30.glUniform3f(shader.uCameraPosLoc, 0f, 1.15f, 3.25f)
        GLES30.glUniform3f(shader.uFogColorLoc, 0.11f, 0.13f, 0.15f)
        GLES30.glUniform1f(shader.uFogDensityLoc, 0.03f)

        // Draw trench props behind soldier for grounded environmental context
        drawProp(models.sandbagWall, -1.25f, 0f, -1.35f, 18f, 0.85f)
        drawProp(models.sandbagWall, 1.25f, 0f, -1.35f, -18f, 0.85f)
        drawProp(models.timberWall, 0f, 0f, -1.65f, 0f, 1.1f)
        drawProp(models.supplyCrate, -0.85f, 0f, -0.35f, 24f, 0.75f)

        // Draw standing 3D Soldier in selected uniform
        val cHex = uniformStyle.coatColorHex
        val coatRgb = floatArrayOf(
            ((cHex shr 16) and 0xFF) / 255f,
            ((cHex shr 8) and 0xFF) / 255f,
            (cHex and 0xFF) / 255f
        )

        Soldier3DDrawer.drawSoldier(
            shader = shader,
            models = models,
            vpMatrix = vpMat,
            x = 0f,
            y = 0f,
            z = 0f,
            yawDeg = autoSpinYaw + userYawOffset,
            stridePhase = 0f,
            isMoving = false,
            isCrouching = false,
            isDead = false,
            deathProgress = 0f,
            isEntente = true,
            coatRgb = coatRgb,
            scale = 1.0f
        )

        // Draw floating showcase 3D Weapon beside/below for crisp weapon inspection
        val wMesh = when (weaponType) {
            WeaponType.BOLT_RIFLE -> models.boltRifleBody
            WeaponType.TRENCH_SMG -> models.smgBody
            WeaponType.LIGHT_MG -> models.lmgBody
            WeaponType.SCOPED_RIFLE -> models.sniperRifleBody
            WeaponType.OFFICER_REVOLVER -> models.revolverBody
        }
        Matrix.setIdentityM(modelMat, 0)
        Matrix.translateM(modelMat, 0, 0f, 0.28f, 0.95f)
        Matrix.rotateM(modelMat, 0, autoSpinYaw * 1.4f + userYawOffset + 90f, 0f, 1f, 0f)
        Matrix.scaleM(modelMat, 0, 0.78f, 0.78f, 0.78f)
        Matrix.multiplyMM(mvpMat, 0, vpMat, 0, modelMat, 0)
        GLES30.glUniformMatrix4fv(shader.uModelLoc, 1, false, modelMat, 0)
        GLES30.glUniformMatrix4fv(shader.uMvpLoc, 1, false, mvpMat, 0)
        GLES30.glUniform4f(shader.uTintColorLoc, 1f, 1f, 1f, 1f)
        wMesh.draw()
    }

    private fun drawProp(mesh: GpuMesh, x: Float, y: Float, z: Float, yaw: Float, scale: Float) {
        Matrix.setIdentityM(modelMat, 0)
        Matrix.translateM(modelMat, 0, x, y, z)
        Matrix.rotateM(modelMat, 0, yaw, 0f, 1f, 0f)
        Matrix.scaleM(modelMat, 0, scale, scale, scale)
        Matrix.multiplyMM(mvpMat, 0, vpMat, 0, modelMat, 0)
        GLES30.glUniformMatrix4fv(shader.uModelLoc, 1, false, modelMat, 0)
        GLES30.glUniformMatrix4fv(shader.uMvpLoc, 1, false, mvpMat, 0)
        GLES30.glUniform4f(shader.uTintColorLoc, 1f, 1f, 1f, 1f)
        mesh.draw()
    }
}
