package com.example.game.render3d

import com.example.game.model.TheatreBiome
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class PropKind {
    SANDBAG,
    TIMBER_WALL,
    BARBED_WIRE,
    STONE_RUIN,
    SUPPLY_CRATE
}

data class PropInstance(
    val kind: PropKind,
    val x: Float,
    val y: Float,
    val z: Float,
    val yawDeg: Float,
    val scale: Float = 1f
)

data class SectorFlagSpot(
    val index: Int,
    val nameRu: String,
    val x: Float,
    val z: Float
)

data class BuiltBattlefield(
    val terrainMesh: GpuMesh,
    val oceanMesh: GpuMesh?,
    val props: List<PropInstance>,
    val sectors: List<SectorFlagSpot>,
    val shipPos: Vec3?
)

object BattlefieldSceneBuilder {

    fun build(biome: TheatreBiome, sectorsCount: Int): BuiltBattlefield {
        val terrainMesh = buildTerrainMesh(biome)
        val oceanMesh = if (biome == TheatreBiome.COASTAL_LANDING) buildOceanMesh() else null
        val props = buildProps(biome)

        val sectors = mutableListOf(
            SectorFlagSpot(0, "СЕКТОР A", 0f, 0f),
            SectorFlagSpot(1, "СЕКТОР B", 2f, -20f)
        )
        if (sectorsCount >= 3) {
            sectors.add(SectorFlagSpot(2, "СЕКТОР C", -1f, -35f))
        }

        val shipPos = if (biome == TheatreBiome.COASTAL_LANDING) {
            Vec3(-18f, -2.5f, 66f)
        } else {
            null
        }

        return BuiltBattlefield(
            terrainMesh = terrainMesh,
            oceanMesh = oceanMesh,
            props = props,
            sectors = sectors,
            shipPos = shipPos
        )
    }

    private fun buildTerrainMesh(biome: TheatreBiome): GpuMesh {
        val minX = -42f
        val maxX = 42f
        val minZ = -52f
        val maxZ = 40f
        val step = 1.6f

        val cols = ((maxX - minX) / step).toInt()
        val rows = ((maxZ - minZ) / step).toInt()
        val m = MeshBuilder(cols * rows * 6 * MeshBuilder.STRIDE_FLOATS)

        for (rz in 0 until rows) {
            val z0 = minZ + rz * step
            val z1 = z0 + step
            for (cx in 0 until cols) {
                val x0 = minX + cx * step
                val x1 = x0 + step

                val p00 = Vec3(x0, Math3D.terrainHeightAt(x0, z0, biome), z0)
                val p10 = Vec3(x1, Math3D.terrainHeightAt(x1, z0, biome), z0)
                val p11 = Vec3(x1, Math3D.terrainHeightAt(x1, z1, biome), z1)
                val p01 = Vec3(x0, Math3D.terrainHeightAt(x0, z1, biome), z1)

                val n00 = Math3D.terrainNormalAt(x0, z0, biome)
                val n10 = Math3D.terrainNormalAt(x1, z0, biome)
                val n11 = Math3D.terrainNormalAt(x1, z1, biome)
                val n01 = Math3D.terrainNormalAt(x0, z1, biome)

                val c00 = terrainColorAt(x0, p00.y, z0, biome)
                val c10 = terrainColorAt(x1, p10.y, z0, biome)
                val c11 = terrainColorAt(x1, p11.y, z1, biome)
                val c01 = terrainColorAt(x0, p01.y, z1, biome)

                val spec = if (biome == TheatreBiome.MARNE_TRENCHES || biome == TheatreBiome.VERDUN_CRATERS) 0.14f else 0.08f

                m.vertex(p00.x, p00.y, p00.z, n00.x, n00.y, n00.z, c00[0], c00[1], c00[2], 1f, spec)
                m.vertex(p01.x, p01.y, p01.z, n01.x, n01.y, n01.z, c01[0], c01[1], c01[2], 1f, spec)
                m.vertex(p11.x, p11.y, p11.z, n11.x, n11.y, n11.z, c11[0], c11[1], c11[2], 1f, spec)

                m.vertex(p00.x, p00.y, p00.z, n00.x, n00.y, n00.z, c00[0], c00[1], c00[2], 1f, spec)
                m.vertex(p11.x, p11.y, p11.z, n11.x, n11.y, n11.z, c11[0], c11[1], c11[2], 1f, spec)
                m.vertex(p10.x, p10.y, p10.z, n10.x, n10.y, n10.z, c10[0], c10[1], c10[2], 1f, spec)
            }
        }
        return m.buildGpuMesh()
    }

    private fun terrainColorAt(x: Float, y: Float, z: Float, biome: TheatreBiome): FloatArray {
        val noise = 0.92f + 0.10f * sin(x * 0.45f) * cos(z * 0.42f)
        val inTrench = abs(z - 18f) < 1.8f || abs(z) < 1.7f || abs(z + 20f) < 1.8f

        return when (biome) {
            TheatreBiome.MARNE_TRENCHES -> {
                if (inTrench) {
                    floatArrayOf(0.24f * noise, 0.20f * noise, 0.14f * noise) // Wet trench mud
                } else {
                    floatArrayOf(0.32f * noise, 0.34f * noise, 0.20f * noise) // Grassy mud
                }
            }
            TheatreBiome.VERDUN_CRATERS -> {
                if (y < -0.35f || inTrench) {
                    floatArrayOf(0.20f * noise, 0.17f * noise, 0.14f * noise) // Scorched crater mud
                } else {
                    floatArrayOf(0.34f * noise, 0.29f * noise, 0.21f * noise) // Churning earth
                }
            }
            TheatreBiome.COASTAL_LANDING -> {
                if (z > 20f) {
                    floatArrayOf(0.46f * noise, 0.42f * noise, 0.34f * noise) // Shingle beach
                } else if (inTrench) {
                    floatArrayOf(0.30f * noise, 0.27f * noise, 0.22f * noise)
                } else {
                    floatArrayOf(0.36f * noise, 0.36f * noise, 0.28f * noise) // Coastal grass & stone
                }
            }
            TheatreBiome.ALPINE_FORTRESS -> {
                if (y > 3.2f) {
                    floatArrayOf(0.76f * noise, 0.78f * noise, 0.82f * noise) // Alpine snow patches
                } else if (inTrench) {
                    floatArrayOf(0.28f * noise, 0.29f * noise, 0.31f * noise)
                } else {
                    floatArrayOf(0.42f * noise, 0.42f * noise, 0.40f * noise) // Limestone scree
                }
            }
        }
    }

    private fun buildOceanMesh(): GpuMesh {
        val m = MeshBuilder(1024)
        // Expansive coastal sea behind player starting beach (Z = 24 to 135)
        val step = 12f
        var z = 22f
        while (z < 130f) {
            var x = -90f
            while (x < 90f) {
                val p00 = Vec3(x, -1.75f, z)
                val p10 = Vec3(x + step, -1.75f, z)
                val p11 = Vec3(x + step, -1.75f, z + step)
                val p01 = Vec3(x, -1.75f, z + step)
                m.addQuad(p00, p01, p11, p10, 0.20f, 0.30f, 0.37f, 0.96f, spec = 0.85f)
                x += step
            }
            z += step
        }
        return m.buildGpuMesh()
    }

    private fun buildProps(biome: TheatreBiome): List<PropInstance> {
        val list = mutableListOf<PropInstance>()

        fun addProp(kind: PropKind, x: Float, z: Float, yaw: Float, scale: Float = 1f) {
            val y = Math3D.terrainHeightAt(x, z, biome)
            list.add(PropInstance(kind, x, y, z, yaw, scale))
        }

        // Friendly trench timber revetments & sandbag parapets (Z = 16..20)
        for (x in -18..18 step 4) {
            addProp(PropKind.TIMBER_WALL, x.toFloat(), 19.6f, 0f)
            if (abs(x) % 8 != 0) {
                addProp(PropKind.SANDBAG, x.toFloat(), 16.0f, 0f)
            }
        }
        addProp(PropKind.SUPPLY_CRATE, -5.5f, 18.2f, 18f)
        addProp(PropKind.SUPPLY_CRATE, 6.2f, 18.0f, -12f)

        // Mid-field barbed wire & sandbag cover pockets (Z = 4..10 and Z = -8..-12)
        for (x in listOf(-14f, -8f, -3f, 4f, 10f, 15f)) {
            addProp(PropKind.BARBED_WIRE, x, 8.5f, (x * 4f) % 18f)
        }
        for (x in listOf(-12f, -5f, 5f, 12f)) {
            addProp(PropKind.SANDBAG, x, 2.2f, (x * 3f))
            addProp(PropKind.BARBED_WIRE, x + 2.5f, -10.5f, (-x * 3f))
        }

        // Enemy forward trench line (Z = -18..-22)
        for (x in -18..18 step 4) {
            addProp(PropKind.SANDBAG, x.toFloat(), -18.0f, 180f)
            addProp(PropKind.TIMBER_WALL, x.toFloat(), -21.6f, 180f)
        }

        // Biome-specific architectural fortifications & ruins
        when (biome) {
            TheatreBiome.MARNE_TRENCHES -> {
                addProp(PropKind.STONE_RUIN, -11f, -6f, 25f, 1.15f)
                addProp(PropKind.STONE_RUIN, 13f, -26f, -20f, 1.2f)
                addProp(PropKind.SUPPLY_CRATE, -2.5f, -20.2f, 10f)
            }
            TheatreBiome.VERDUN_CRATERS -> {
                addProp(PropKind.STONE_RUIN, -9f, -15f, 15f, 1.35f)
                addProp(PropKind.STONE_RUIN, 9f, -28f, -12f, 1.4f)
                addProp(PropKind.BARBED_WIRE, 0f, -5f, 6f)
            }
            TheatreBiome.COASTAL_LANDING -> {
                // Coastal rock ruins and cliffside barbed wire like Screenshot #2
                addProp(PropKind.STONE_RUIN, -13f, 11f, 38f, 1.3f)
                addProp(PropKind.STONE_RUIN, 14f, -4f, -30f, 1.25f)
                addProp(PropKind.STONE_RUIN, -7f, -32f, 12f, 1.4f)
                addProp(PropKind.BARBED_WIRE, -10f, 12f, 15f)
                addProp(PropKind.BARBED_WIRE, 9f, 11.5f, -14f)
                addProp(PropKind.SUPPLY_CRATE, 3f, -35f, 22f)
            }
            TheatreBiome.ALPINE_FORTRESS -> {
                // Heavy alpine fortress stone walls along the ridge
                for (x in listOf(-14f, -6f, 6f, 14f)) {
                    addProp(PropKind.STONE_RUIN, x, -12f, (x * 1.5f), 1.3f)
                    addProp(PropKind.STONE_RUIN, x, -33f, (-x * 1.2f), 1.45f)
                }
                addProp(PropKind.SUPPLY_CRATE, 0f, -36f, 0f)
            }
        }

        return list
    }
}
