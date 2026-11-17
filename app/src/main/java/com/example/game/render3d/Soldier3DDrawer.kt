package com.example.game.render3d

import android.opengl.GLES30
import android.opengl.Matrix
import kotlin.math.sin

object Soldier3DDrawer {
    private val modelMat = FloatArray(16)
    private val partMat = FloatArray(16)
    private val mvpMat = FloatArray(16)

    fun drawSoldier(
        shader: LitShaderProgram,
        models: ModelCatalog3D,
        vpMatrix: FloatArray,
        x: Float,
        y: Float,
        z: Float,
        yawDeg: Float,
        stridePhase: Float,
        isMoving: Boolean,
        isCrouching: Boolean,
        isDead: Boolean,
        deathProgress: Float,
        isEntente: Boolean,
        coatRgb: FloatArray,
        scale: Float = 1f
    ) {
        Matrix.setIdentityM(modelMat, 0)
        Matrix.translateM(modelMat, 0, x, y, z)
        Matrix.rotateM(modelMat, 0, yawDeg, 0f, 1f, 0f)
        if (scale != 1f) {
            Matrix.scaleM(modelMat, 0, scale, scale, scale)
        }
        if (isDead) {
            // Fall backward cleanly onto the mud
            val angle = deathProgress.coerceIn(0f, 1f) * 86f
            Matrix.translateM(modelMat, 0, 0f, 0.18f, 0f)
            Matrix.rotateM(modelMat, 0, -angle, 1f, 0f, 0f)
        } else if (isCrouching) {
            Matrix.translateM(modelMat, 0, 0f, -0.26f, 0f)
        }

        val legSwing = if (isMoving && !isDead) sin(stridePhase) * 28f else 0f
        val coatR = coatRgb[0]
        val coatG = coatRgb[1]
        val coatB = coatRgb[2]

        // 1. Torso (greatcoat + leather straps + backpack)
        drawPart(shader, vpMatrix, modelMat, models.soldierTorso, coatR, coatG, coatB)

        // 2. Head & Helmet (Adrian for Entente, Stahlhelm for Central Powers)
        val headMesh = if (isEntente) models.soldierHeadAdrian else models.soldierHeadStahlhelm
        drawPart(shader, vpMatrix, modelMat, headMesh, 1f, 1f, 1f)

        // 3. Left & Right Legs
        drawTransformedPart(shader, vpMatrix, modelMat, models.soldierLeg, -0.11f, 0.75f, 0f, legSwing, 0f, 0f, coatR * 0.9f, coatG * 0.9f, coatB * 0.9f)
        drawTransformedPart(shader, vpMatrix, modelMat, models.soldierLeg, 0.11f, 0.75f, 0f, -legSwing, 0f, 0f, coatR * 0.9f, coatG * 0.9f, coatB * 0.9f)

        // 4. Left & Right Arms (aiming rifle forward +Z)
        val aimPitch = if (isDead) -15f else -62f
        drawTransformedPart(shader, vpMatrix, modelMat, models.soldierArm, -0.24f, 1.32f, 0.04f, aimPitch, 22f, 0f, coatR, coatG, coatB)
        drawTransformedPart(shader, vpMatrix, modelMat, models.soldierArm, 0.24f, 1.32f, 0.04f, aimPitch, -10f, 0f, coatR, coatG, coatB)

        // 5. Held 3D Rifle
        Matrix.setIdentityM(partMat, 0)
        Matrix.multiplyMM(partMat, 0, modelMat, 0, partMat, 0)
        Matrix.translateM(partMat, 0, 0.08f, 1.16f, 0.34f)
        Matrix.rotateM(partMat, 0, 180f, 0f, 1f, 0f)
        drawPart(shader, vpMatrix, partMat, models.worldRifle, 1f, 1f, 1f)
    }

    private fun drawTransformedPart(
        shader: LitShaderProgram,
        vpMatrix: FloatArray,
        parentMat: FloatArray,
        mesh: GpuMesh,
        tx: Float, ty: Float, tz: Float,
        rx: Float, ry: Float, rz: Float,
        r: Float, g: Float, b: Float
    ) {
        val local = FloatArray(16)
        Matrix.setIdentityM(local, 0)
        Matrix.translateM(local, 0, tx, ty, tz)
        if (ry != 0f) Matrix.rotateM(local, 0, ry, 0f, 1f, 0f)
        if (rx != 0f) Matrix.rotateM(local, 0, rx, 1f, 0f, 0f)
        if (rz != 0f) Matrix.rotateM(local, 0, rz, 0f, 0f, 1f)
        Matrix.multiplyMM(partMat, 0, parentMat, 0, local, 0)
        drawPart(shader, vpMatrix, partMat, mesh, r, g, b)
    }

    private fun drawPart(
        shader: LitShaderProgram,
        vpMatrix: FloatArray,
        mMat: FloatArray,
        mesh: GpuMesh,
        r: Float, g: Float, b: Float
    ) {
        Matrix.multiplyMM(mvpMat, 0, vpMatrix, 0, mMat, 0)
        GLES30.glUniformMatrix4fv(shader.uModelLoc, 1, false, mMat, 0)
        GLES30.glUniformMatrix4fv(shader.uMvpLoc, 1, false, mvpMat, 0)
        GLES30.glUniform4f(shader.uTintColorLoc, r, g, b, 1f)
        mesh.draw()
    }
}
