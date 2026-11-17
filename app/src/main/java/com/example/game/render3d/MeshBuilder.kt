package com.example.game.render3d

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class GpuMesh(
    private val vaoId: Int,
    private val vboId: Int,
    val vertexCount: Int
) {
    fun draw() {
        if (vertexCount <= 0) return
        GLES30.glBindVertexArray(vaoId)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
        GLES30.glBindVertexArray(0)
    }

    fun release() {
        val vaos = intArrayOf(vaoId)
        val vbos = intArrayOf(vboId)
        GLES30.glDeleteVertexArrays(1, vaos, 0)
        GLES30.glDeleteBuffers(1, vbos, 0)
    }
}

class MeshBuilder(initialFloats: Int = 4096) {
    private var data = FloatArray(initialFloats)
    private var size = 0

    companion object {
        const val STRIDE_FLOATS = 11 // pos(3) + norm(3) + rgba(4) + spec(1)
        const val STRIDE_BYTES = STRIDE_FLOATS * 4
    }

    private fun ensureCapacity(extra: Int) {
        if (size + extra <= data.size) return
        var newCap = data.size * 2
        while (newCap < size + extra) newCap *= 2
        data = data.copyOf(newCap)
    }

    fun vertex(
        x: Float, y: Float, z: Float,
        nx: Float, ny: Float, nz: Float,
        r: Float, g: Float, b: Float, a: Float = 1f,
        spec: Float = 0.08f
    ) {
        ensureCapacity(STRIDE_FLOATS)
        val i = size
        data[i] = x
        data[i + 1] = y
        data[i + 2] = z
        data[i + 3] = nx
        data[i + 4] = ny
        data[i + 5] = nz
        data[i + 6] = r
        data[i + 7] = g
        data[i + 8] = b
        data[i + 9] = a
        data[i + 10] = spec
        size += STRIDE_FLOATS
    }

    fun addTri(
        p0: Vec3, p1: Vec3, p2: Vec3,
        r: Float, g: Float, b: Float, a: Float = 1f, spec: Float = 0.08f
    ) {
        val n = (p1 - p0).cross(p2 - p0).normalized()
        vertex(p0.x, p0.y, p0.z, n.x, n.y, n.z, r, g, b, a, spec)
        vertex(p1.x, p1.y, p1.z, n.x, n.y, n.z, r, g, b, a, spec)
        vertex(p2.x, p2.y, p2.z, n.x, n.y, n.z, r, g, b, a, spec)
    }

    fun addQuad(
        p0: Vec3, p1: Vec3, p2: Vec3, p3: Vec3,
        r: Float, g: Float, b: Float, a: Float = 1f, spec: Float = 0.08f
    ) {
        addTri(p0, p1, p2, r, g, b, a, spec)
        addTri(p0, p2, p3, r, g, b, a, spec)
    }

    fun addBox(
        cx: Float, cy: Float, cz: Float,
        sx: Float, sy: Float, sz: Float,
        r: Float, g: Float, b: Float, a: Float = 1f, spec: Float = 0.1f
    ) {
        val hx = sx * 0.5f
        val hy = sy * 0.5f
        val hz = sz * 0.5f
        val p000 = Vec3(cx - hx, cy - hy, cz - hz)
        val p100 = Vec3(cx + hx, cy - hy, cz - hz)
        val p110 = Vec3(cx + hx, cy + hy, cz - hz)
        val p010 = Vec3(cx - hx, cy + hy, cz - hz)
        val p001 = Vec3(cx - hx, cy - hy, cz + hz)
        val p101 = Vec3(cx + hx, cy - hy, cz + hz)
        val p111 = Vec3(cx + hx, cy + hy, cz + hz)
        val p011 = Vec3(cx - hx, cy + hy, cz + hz)

        // Front (+Z)
        addQuad(p001, p101, p111, p011, r, g, b, a, spec)
        // Back (-Z)
        addQuad(p100, p000, p010, p110, r * 0.94f, g * 0.94f, b * 0.94f, a, spec)
        // Right (+X)
        addQuad(p101, p100, p110, p111, r * 0.96f, g * 0.96f, b * 0.96f, a, spec)
        // Left (-X)
        addQuad(p000, p001, p011, p010, r * 0.96f, g * 0.96f, b * 0.96f, a, spec)
        // Top (+Y)
        addQuad(p011, p111, p110, p010, (r * 1.04f).coerceAtMost(1f), (g * 1.04f).coerceAtMost(1f), (b * 1.04f).coerceAtMost(1f), a, spec)
        // Bottom (-Y)
        addQuad(p000, p100, p101, p001, r * 0.85f, g * 0.85f, b * 0.85f, a, spec)
    }

    /**
     * Cylinder along Y axis centered at (cx, cy, cz)
     */
    fun addCylinderY(
        cx: Float, cy: Float, cz: Float,
        radiusBottom: Float, radiusTop: Float, height: Float,
        segments: Int,
        r: Float, g: Float, b: Float, a: Float = 1f, spec: Float = 0.15f
    ) {
        val y0 = cy - height * 0.5f
        val y1 = cy + height * 0.5f
        val step = (2.0 * PI / segments).toFloat()

        for (i in 0 until segments) {
            val a0 = i * step
            val a1 = (i + 1) * step
            val c0 = cos(a0)
            val s0 = sin(a0)
            val c1 = cos(a1)
            val s1 = sin(a1)

            val b0 = Vec3(cx + c0 * radiusBottom, y0, cz + s0 * radiusBottom)
            val b1 = Vec3(cx + c1 * radiusBottom, y0, cz + s1 * radiusBottom)
            val t1 = Vec3(cx + c1 * radiusTop, y1, cz + s1 * radiusTop)
            val t0 = Vec3(cx + c0 * radiusTop, y1, cz + s0 * radiusTop)

            addQuad(b0, t0, t1, b1, r, g, b, a, spec)
            if (radiusTop > 0.001f) {
                addTri(Vec3(cx, y1, cz), t1, t0, r * 1.04f, g * 1.04f, b * 1.04f, a, spec)
            }
            if (radiusBottom > 0.001f) {
                addTri(Vec3(cx, y0, cz), b0, b1, r * 0.85f, g * 0.85f, b * 0.85f, a, spec)
            }
        }
    }

    /**
     * Cylinder along Z axis centered at (cx, cy, cz) — great for rifle barrels & scopes!
     */
    fun addCylinderZ(
        cx: Float, cy: Float, cz: Float,
        radiusFront: Float, radiusBack: Float, length: Float,
        segments: Int,
        r: Float, g: Float, b: Float, a: Float = 1f, spec: Float = 0.65f
    ) {
        val z0 = cz - length * 0.5f // front (-Z)
        val z1 = cz + length * 0.5f // back (+Z)
        val step = (2.0 * PI / segments).toFloat()

        for (i in 0 until segments) {
            val a0 = i * step
            val a1 = (i + 1) * step
            val c0 = cos(a0)
            val s0 = sin(a0)
            val c1 = cos(a1)
            val s1 = sin(a1)

            val f0 = Vec3(cx + c0 * radiusFront, cy + s0 * radiusFront, z0)
            val f1 = Vec3(cx + c1 * radiusFront, cy + s1 * radiusFront, z0)
            val k1 = Vec3(cx + c1 * radiusBack, cy + s1 * radiusBack, z1)
            val k0 = Vec3(cx + c0 * radiusBack, cy + s0 * radiusBack, z1)

            addQuad(f0, k0, k1, f1, r, g, b, a, spec)
            if (radiusFront > 0.001f) {
                addTri(Vec3(cx, cy, z0), f0, f1, r * 0.7f, g * 0.7f, b * 0.7f, a, spec)
            }
            if (radiusBack > 0.001f) {
                addTri(Vec3(cx, cy, z1), k1, k0, r * 0.9f, g * 0.9f, b * 0.9f, a, spec)
            }
        }
    }

    /**
     * Cylinder along X axis — great for bolt handles, horizontal posts, wheel axles!
     */
    fun addCylinderX(
        cx: Float, cy: Float, cz: Float,
        radius: Float, length: Float,
        segments: Int,
        r: Float, g: Float, b: Float, a: Float = 1f, spec: Float = 0.65f
    ) {
        val x0 = cx - length * 0.5f
        val x1 = cx + length * 0.5f
        val step = (2.0 * PI / segments).toFloat()
        for (i in 0 until segments) {
            val a0 = i * step
            val a1 = (i + 1) * step
            val c0 = cos(a0)
            val s0 = sin(a0)
            val c1 = cos(a1)
            val s1 = sin(a1)
            val p0 = Vec3(x0, cy + c0 * radius, cz + s0 * radius)
            val p1 = Vec3(x0, cy + c1 * radius, cz + s1 * radius)
            val q1 = Vec3(x1, cy + c1 * radius, cz + s1 * radius)
            val q0 = Vec3(x1, cy + c0 * radius, cz + s0 * radius)
            addQuad(p0, p1, q1, q0, r, g, b, a, spec)
        }
    }

    /**
     * Smooth UV-sphere (used for helmets, heads, grenade bodies, explosion puffs, bolt knobs)
     */
    fun addSphere(
        cx: Float, cy: Float, cz: Float,
        rx: Float, ry: Float, rz: Float,
        stacks: Int, slices: Int,
        r: Float, g: Float, b: Float, a: Float = 1f, spec: Float = 0.2f,
        maxPhiRatio: Float = 1f
    ) {
        for (i in 0 until stacks) {
            val phi0 = (i.toFloat() / stacks) * PI.toFloat() * maxPhiRatio
            val phi1 = ((i + 1).toFloat() / stacks) * PI.toFloat() * maxPhiRatio
            for (j in 0 until slices) {
                val th0 = (j.toFloat() / slices) * 2f * PI.toFloat()
                val th1 = ((j + 1).toFloat() / slices) * 2f * PI.toFloat()

                val p00 = spherePt(cx, cy, cz, rx, ry, rz, phi0, th0)
                val p10 = spherePt(cx, cy, cz, rx, ry, rz, phi1, th0)
                val p11 = spherePt(cx, cy, cz, rx, ry, rz, phi1, th1)
                val p01 = spherePt(cx, cy, cz, rx, ry, rz, phi0, th1)

                val n00 = Vec3((p00.x - cx) / rx, (p00.y - cy) / ry, (p00.z - cz) / rz).normalized()
                val n10 = Vec3((p10.x - cx) / rx, (p10.y - cy) / ry, (p10.z - cz) / rz).normalized()
                val n11 = Vec3((p11.x - cx) / rx, (p11.y - cy) / ry, (p11.z - cz) / rz).normalized()
                val n01 = Vec3((p01.x - cx) / rx, (p01.y - cy) / ry, (p01.z - cz) / rz).normalized()

                vertex(p00.x, p00.y, p00.z, n00.x, n00.y, n00.z, r, g, b, a, spec)
                vertex(p10.x, p10.y, p10.z, n10.x, n10.y, n10.z, r, g, b, a, spec)
                vertex(p11.x, p11.y, p11.z, n11.x, n11.y, n11.z, r, g, b, a, spec)

                vertex(p00.x, p00.y, p00.z, n00.x, n00.y, n00.z, r, g, b, a, spec)
                vertex(p11.x, p11.y, p11.z, n11.x, n11.y, n11.z, r, g, b, a, spec)
                vertex(p01.x, p01.y, p01.z, n01.x, n01.y, n01.z, r, g, b, a, spec)
            }
        }
    }

    private fun spherePt(
        cx: Float, cy: Float, cz: Float,
        rx: Float, ry: Float, rz: Float,
        phi: Float, theta: Float
    ): Vec3 {
        val sp = sin(phi)
        return Vec3(
            cx + rx * sp * cos(theta),
            cy + ry * cos(phi),
            cz + rz * sp * sin(theta)
        )
    }

    fun buildGpuMesh(): GpuMesh {
        val vertexCount = size / STRIDE_FLOATS
        val vaos = IntArray(1)
        val vbos = IntArray(1)
        GLES30.glGenVertexArrays(1, vaos, 0)
        GLES30.glGenBuffers(1, vbos, 0)

        val fb = ByteBuffer.allocateDirect(size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        fb.put(data, 0, size)
        fb.position(0)

        GLES30.glBindVertexArray(vaos[0])
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, size * 4, fb, GLES30.GL_STATIC_DRAW)

        // pos (0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, STRIDE_BYTES, 0)
        // normal (1)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, STRIDE_BYTES, 3 * 4)
        // color (2)
        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 4, GLES30.GL_FLOAT, false, STRIDE_BYTES, 6 * 4)
        // specular (3)
        GLES30.glEnableVertexAttribArray(3)
        GLES30.glVertexAttribPointer(3, 1, GLES30.GL_FLOAT, false, STRIDE_BYTES, 10 * 4)

        GLES30.glBindVertexArray(0)
        return GpuMesh(vaos[0], vbos[0], vertexCount)
    }
}
