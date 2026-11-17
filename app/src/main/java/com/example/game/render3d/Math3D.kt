package com.example.game.render3d

import com.example.game.model.TheatreBiome
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
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

    /**
     * Ray vs Sphere intersection distance (> 0f if hit, -1f if miss).
     */
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
     * Procedural battlefield terrain height shared by 3D mesh builder, player physics, and AI.
     * Coordinates span roughly X in [-48..48], Z in [-56..40].
     * Player starts near Z = +22 facing North (-Z).
     */
    fun terrainHeightAt(x: Float, z: Float, biome: TheatreBiome): Float {
        var h = 0f

        // Base terrain macro relief per biome
        when (biome) {
            TheatreBiome.MARNE_TRENCHES -> {
                h += 0.35f * sin(x * 0.08f) * cos(z * 0.07f)
                // Slight ridge in the north
                if (z < -18f) h += ((-18f - z) * 0.05f).coerceAtMost(1.8f)
            }
            TheatreBiome.VERDUN_CRATERS -> {
                h += 0.5f * sin(x * 0.11f + 0.6f) * cos(z * 0.09f)
                // Shell craters
                h -= craterDip(x, z, -6f, 2f, 4.2f, 1.05f)
                h -= craterDip(x, z, 8f, -4f, 4.8f, 1.15f)
                h -= craterDip(x, z, -12f, -14f, 4.0f, 0.95f)
                h -= craterDip(x, z, 5f, -20f, 5.0f, 1.2f)
                h -= craterDip(x, z, 15f, 8f, 3.8f, 0.9f)
            }
            TheatreBiome.COASTAL_LANDING -> {
                // Slopes down toward sea in the south (z > 24), rises toward cliffs in the north (z < -10)
                val slope = ((20f - z) * 0.095f).coerceIn(-2.2f, 4.5f)
                h += slope + 0.45f * sin(x * 0.14f) * cos(z * 0.11f)
                // Rocky ridge bumps
                h += 0.35f * sin(x * 0.35f + z * 0.25f)
            }
            TheatreBiome.ALPINE_FORTRESS -> {
                // Steep terraced mountain slope
                val rise = ((24f - z) * 0.11f).coerceIn(0f, 5.6f)
                h += rise + 0.55f * sin(x * 0.13f) * sin(z * 0.12f)
                // Outer rock walls on sides
                if (kotlin.math.abs(x) > 26f) {
                    h += (kotlin.math.abs(x) - 26f) * 0.32f
                }
            }
        }

        // Carved friendly & forward trenches (easy natural cover!)
        // Friendly trench around Z = 18
        h -= trenchChannel(z, 18f, 2.1f, 0.88f) * smoothBounds(x, 24f)
        // Mid no-man's-land trench around Z = 0
        h -= trenchChannel(z, 0f, 1.9f, 0.78f) * smoothBounds(x, 22f)
        // Enemy forward trench around Z = -20
        h -= trenchChannel(z, -20f, 2.0f, 0.85f) * smoothBounds(x, 24f)

        return h
    }

    private fun trenchChannel(z: Float, centerZ: Float, halfWidth: Float, depth: Float): Float {
        val d = kotlin.math.abs(z - centerZ)
        if (d >= halfWidth) return 0f
        val t = 1f - (d / halfWidth)
        return depth * t * t * (3f - 2f * t)
    }

    private fun smoothBounds(x: Float, limit: Float): Float {
        val ax = kotlin.math.abs(x)
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
        // Slight raised rim around 0.85r, deep center
        return if (norm > 0.75f) {
            val rim = sin((norm - 0.75f) / 0.25f * PI.toFloat())
            -depth * 0.22f * rim
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
