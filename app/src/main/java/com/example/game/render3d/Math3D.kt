package com.example.game.render3d

import com.example.game.model.TheatreBiome
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vec3(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)
    fun dot(o: Vec3) = x * o.x + y * o.y + z * o.z
    fun lengthSq() = x * x + y * y + z * z
    fun length() = sqrt(lengthSq())
    fun normalized(): Vec3 {
        val len = length()
        return if (len > 1e-6f) Vec3(x / len, y / len, z / len) else Vec3(0f, 1f, 0f)
    }
    fun cross(o: Vec3) = Vec3(
        y * o.z - z * o.y,
        z * o.x - x * o.z,
        x * o.y - y * o.x
    )
}

data class BoxCollider2D(
    val minX: Float,
    val maxX: Float,
    val minZ: Float,
    val maxZ: Float
) {
    fun pushOut(px: Float, pz: Float, radius: Float): Pair<Float, Float> {
        val cx = px.coerceIn(minX, maxX)
        val cz = pz.coerceIn(minZ, maxZ)
        val dx = px - cx
        val dz = pz - cz
        val dSq = dx * dx + dz * dz
        if (dSq >= radius * radius) return px to pz
        if (dSq > 1e-6f) {
            val d = sqrt(dSq)
            val push = radius - d
            return (px + (dx / d) * push) to (pz + (dz / d) * push)
        }
        // Inside AABB: push out along nearest edge
        val dLeft = abs(px - minX)
        val dRight = abs(maxX - px)
        val dBack = abs(pz - minZ)
        val dFront = abs(maxZ - pz)
        val m = minOf(dLeft, dRight, dBack, dFront)
        return when (m) {
            dLeft -> (minX - radius) to pz
            dRight -> (maxX + radius) to pz
            dBack -> px to (minZ - radius)
            else -> px to (maxZ + radius)
        }
    }
}

object Math3D {
    fun degToRad(deg: Float): Float = deg * (PI.toFloat() / 180f)

    fun forwardFromYawPitch(yawDeg: Float, pitchDeg: Float): Vec3 {
        val yaw = degToRad(yawDeg)
        val pitch = degToRad(pitchDeg)
        val cosP = cos(pitch)
        return Vec3(
            x = sin(yaw) * cosP,
            y = sin(pitch),
            z = -cos(yaw) * cosP
        ).normalized()
    }

    fun rightFromYaw(yawDeg: Float): Vec3 {
        val yaw = degToRad(yawDeg)
        return Vec3(cos(yaw), 0f, sin(yaw)).normalized()
    }

    fun raySphereDistance(
        origin: Vec3,
        dir: Vec3,
        center: Vec3,
        radius: Float
    ): Float {
        val oc = origin - center
        val b = oc.dot(dir)
        val c = oc.dot(oc) - radius * radius
        val disc = b * b - c
        if (disc < 0f) return -1f
        val t = -b - sqrt(disc)
        return if (t > 0.1f) t else -1f
    }

    /**
     * Shared procedural terrain height for Gallipoli coast, Basra riverfront, Gaza city, and Jerusalem citadel.
     */
    fun terrainHeightAt(x: Float, z: Float, biome: TheatreBiome): Float {
        var h = 0f

        when (biome) {
            TheatreBiome.GALLIPOLI_COAST -> {
                val slope = ((20f - z) * 0.095f).coerceIn(-2.2f, 4.5f)
                h += slope + 0.40f * sin(x * 0.14f) * cos(z * 0.11f)
                h += 0.28f * sin(x * 0.35f + z * 0.25f)
                h -= trenchChannel(z, 18f, 2.1f, 0.85f) * smoothBounds(x, 24f)
                h -= trenchChannel(z, 0f, 1.9f, 0.78f) * smoothBounds(x, 22f)
                h -= trenchChannel(z, -20f, 2.0f, 0.85f) * smoothBounds(x, 24f)
            }
            TheatreBiome.BASRA_OUTSKIRTS -> {
                h += 0.32f * sin(x * 0.09f) * cos(z * 0.08f)
                h -= craterDip(x, z, -5f, 4f, 4.0f, 0.85f)
                h -= craterDip(x, z, 7f, -8f, 4.4f, 0.95f)
                h -= trenchChannel(z, 18f, 2.1f, 0.82f) * smoothBounds(x, 24f)
                h -= trenchChannel(z, -20f, 2.0f, 0.82f) * smoothBounds(x, 24f)
            }
            TheatreBiome.GAZA_CITY -> {
                // Flat terraced city streets with slight rise toward the northern clocktower plaza
                val gentleRise = ((18f - z) * 0.032f).coerceIn(0f, 2.2f)
                h += gentleRise + 0.08f * sin(x * 0.2f) * cos(z * 0.2f)
                // Shallow bomb crater in mid square
                h -= craterDip(x, z, -2.5f, -9f, 3.2f, 0.45f)
            }
            TheatreBiome.JERUSALEM_CITADEL -> {
                // Historic stone city rising gently toward the fortified citadel in the north
                val citadelRise = ((22f - z) * 0.055f).coerceIn(0f, 3.4f)
                h += citadelRise + 0.12f * sin(x * 0.16f) * cos(z * 0.18f)
            }
        }

        return h
    }

    private fun trenchChannel(z: Float, centerZ: Float, halfWidth: Float, depth: Float): Float {
        val d = abs(z - centerZ)
        if (d >= halfWidth) return 0f
        val t = 1f - (d / halfWidth)
        return depth * t * t * (3f - 2f * t)
    }

    private fun smoothBounds(x: Float, limit: Float): Float {
        val ax = abs(x)
        if (ax >= limit) return 0f
        if (ax <= limit - 4f) return 1f
        return ((limit - ax) / 4f).coerceIn(0f, 1f)
    }

    private fun craterDip(x: Float, z: Float, cx: Float, cz: Float, r: Float, depth: Float): Float {
        val dx = x - cx
        val dz = z - cz
        val dist = sqrt(dx * dx + dz * dz)
        if (dist > r) return 0f
        val norm = dist / r
        return if (norm > 0.75f) {
            val rim = sin((norm - 0.75f) / 0.25f * PI.toFloat())
            -depth * 0.20f * rim
        } else {
            val k = 1f - (norm / 0.75f)
            depth * k * k
        }
    }

    fun terrainNormalAt(x: Float, z: Float, biome: TheatreBiome): Vec3 {
        val eps = 0.45f
        val hL = terrainHeightAt(x - eps, z, biome)
        val hR = terrainHeightAt(x + eps, z, biome)
        val hD = terrainHeightAt(x, z - eps, biome)
        val hU = terrainHeightAt(x, z + eps, biome)
        return Vec3(hL - hR, 2f * eps, hD - hU).normalized()
    }
}
