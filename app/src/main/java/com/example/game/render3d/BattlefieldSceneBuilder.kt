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
    SUPPLY_CRATE,
    CITY_HOUSE_LARGE,
    CITY_HOUSE_BAZAAR,
    CITY_TOWER,
    CITY_GATE,
    MARKET_STALL,
    PALM_TREE
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

data class BattlefieldMetadata(
    val props: List<PropInstance>,
    val colliders: List<BoxCollider2D>,
    val sectors: List<SectorFlagSpot>,
    val britishHomeFlagPos: Vec3,
    val shipPos: Vec3?
)

data class BuiltBattlefield(
    val terrainMesh: GpuMesh,
    val oceanMesh: GpuMesh?,
    val props: List<PropInstance>,
    val colliders: List<BoxCollider2D>,
    val sectors: List<SectorFlagSpot>,
    val britishHomeFlagPos: Vec3,
    val shipPos: Vec3?
)

object BattlefieldSceneBuilder {

    fun buildMetadata(biome: TheatreBiome, sectorsCount: Int): BattlefieldMetadata {
        val (props, colliders) = buildPropsAndColliders(biome)

        val sectors = when (biome) {
            TheatreBiome.GAZA_CITY -> listOf(
                SectorFlagSpot(0, "РЫНОЧНАЯ ПЛОЩАДЬ", 0f, 3f),
                SectorFlagSpot(1, "ЦЕНТРАЛЬНЫЙ КВАРТАЛ", 0f, -16f),
                SectorFlagSpot(2, "ЧАСОВАЯ БАШНЯ", 0f, -34f)
            )
            TheatreBiome.JERUSALEM_CITADEL -> listOf(
                SectorFlagSpot(0, "ЮЖНЫЕ ВОРОТА", 0f, 4f),
                SectorFlagSpot(1, "ТРЕУГОЛЬНАЯ ПЛОЩАДЬ", 0f, -17f),
                SectorFlagSpot(2, "ШТАБ ЦИТАДЕЛИ", 0f, -36f)
            )
            else -> {
                val base = mutableListOf(
                    SectorFlagSpot(0, "СЕКТОР A", 0f, 0f),
                    SectorFlagSpot(1, "СЕКТОР B", 2f, -20f)
                )
                if (sectorsCount >= 3) {
                    base.add(SectorFlagSpot(2, "СЕКТОР C", -1f, -35f))
                }
                base
            }
        }

        val homeFlagX = -6.5f
        val homeFlagZ = 18.8f
        val homeFlagPos = Vec3(homeFlagX, Math3D.terrainHeightAt(homeFlagX, homeFlagZ, biome), homeFlagZ)

        val shipPos = if (biome == TheatreBiome.GALLIPOLI_COAST) {
            Vec3(-18f, -2.5f, 66f)
        } else {
            null
        }

        return BattlefieldMetadata(
            props = props,
            colliders = colliders,
            sectors = sectors,
            britishHomeFlagPos = homeFlagPos,
            shipPos = shipPos
        )
    }

    fun build(biome: TheatreBiome, sectorsCount: Int): BuiltBattlefield {
        val terrainMesh = buildTerrainMesh(biome)
        val oceanMesh = if (biome == TheatreBiome.GALLIPOLI_COAST) buildOceanMesh() else null
        val meta = buildMetadata(biome, sectorsCount)

        return BuiltBattlefield(
            terrainMesh = terrainMesh,
            oceanMesh = oceanMesh,
            props = meta.props,
            colliders = meta.colliders,
            sectors = meta.sectors,
            britishHomeFlagPos = meta.britishHomeFlagPos,
            shipPos = meta.shipPos
        )
    }

    private fun buildTerrainMesh(biome: TheatreBiome): GpuMesh {
        val minX = -44f
        val maxX = 44f
        val minZ = -54f
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

                val spec = if (biome == TheatreBiome.GAZA_CITY || biome == TheatreBiome.JERUSALEM_CITADEL) 0.16f else 0.10f

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
        val noise = 0.92f + 0.09f * sin(x * 0.45f) * cos(z * 0.42f)
        val inTrench = abs(z - 18f) < 1.8f || abs(z) < 1.7f || abs(z + 20f) < 1.8f

        return when (biome) {
            TheatreBiome.GALLIPOLI_COAST -> {
                if (z > 20f) {
                    floatArrayOf(0.54f * noise, 0.48f * noise, 0.36f * noise)
                } else if (inTrench) {
                    floatArrayOf(0.32f * noise, 0.27f * noise, 0.20f * noise)
                } else {
                    floatArrayOf(0.42f * noise, 0.40f * noise, 0.28f * noise)
                }
            }
            TheatreBiome.BASRA_OUTSKIRTS -> {
                if (inTrench) {
                    floatArrayOf(0.36f * noise, 0.29f * noise, 0.20f * noise)
                } else {
                    floatArrayOf(0.58f * noise, 0.49f * noise, 0.34f * noise)
                }
            }
            TheatreBiome.GAZA_CITY -> {
                val onMainAvenue = abs(x) < 4.8f || abs(z - 3f) < 4.5f || abs(z + 16f) < 4.5f
                if (onMainAvenue) {
                    // Cobblestone city street
                    floatArrayOf(0.46f * noise, 0.43f * noise, 0.38f * noise)
                } else {
                    // Warm sandstone courtyard earth
                    floatArrayOf(0.58f * noise, 0.50f * noise, 0.37f * noise)
                }
            }
            TheatreBiome.JERUSALEM_CITADEL -> {
                val onStonePlaza = abs(x) < 6.5f || abs(z + 17f) < 5.5f || z < -28f
                if (onStonePlaza) {
                    floatArrayOf(0.54f * noise, 0.50f * noise, 0.44f * noise)
                } else {
                    floatArrayOf(0.47f * noise, 0.42f * noise, 0.35f * noise)
                }
            }
        }
    }

    private fun buildOceanMesh(): GpuMesh {
        val m = MeshBuilder(1024)
        val step = 12f
        var z = 22f
        while (z < 130f) {
            var x = -90f
            while (x < 90f) {
                val p00 = Vec3(x, -1.75f, z)
                val p10 = Vec3(x + step, -1.75f, z)
                val p11 = Vec3(x + step, -1.75f, z + step)
                val p01 = Vec3(x, -1.75f, z + step)
                m.addQuad(p00, p01, p11, p10, 0.18f, 0.31f, 0.40f, 0.96f, spec = 0.88f)
                x += step
            }
            z += step
        }
        return m.buildGpuMesh()
    }

    private fun buildPropsAndColliders(biome: TheatreBiome): Pair<List<PropInstance>, List<BoxCollider2D>> {
        val props = mutableListOf<PropInstance>()
        val colliders = mutableListOf<BoxCollider2D>()

        fun addProp(kind: PropKind, x: Float, z: Float, yaw: Float, scale: Float = 1f) {
            val y = Math3D.terrainHeightAt(x, z, biome)
            props.add(PropInstance(kind, x, y, z, yaw, scale))
        }

        fun addBuilding(kind: PropKind, x: Float, z: Float, yaw: Float, halfW: Float, halfD: Float, scale: Float = 1f) {
            addProp(kind, x, z, yaw, scale)
            colliders.add(BoxCollider2D(x - halfW * scale, x + halfW * scale, z - halfD * scale, z + halfD * scale))
        }

        when (biome) {
            TheatreBiome.GALLIPOLI_COAST -> {
                for (x in -18..18 step 4) {
                    addProp(PropKind.TIMBER_WALL, x.toFloat(), 19.6f, 0f)
                    if (abs(x) % 8 != 0) addProp(PropKind.SANDBAG, x.toFloat(), 16.0f, 0f)
                    addProp(PropKind.SANDBAG, x.toFloat(), -18.0f, 180f)
                    addProp(PropKind.TIMBER_WALL, x.toFloat(), -21.6f, 180f)
                }
                for (x in listOf(-12f, -6f, 6f, 12f)) {
                    addProp(PropKind.BARBED_WIRE, x, 10.5f, x * 2f)
                    addProp(PropKind.BARBED_WIRE, x + 2f, -9.5f, -x * 2f)
                }
                addProp(PropKind.STONE_RUIN, -13f, 11f, 38f, 1.3f)
                addProp(PropKind.STONE_RUIN, 14f, -4f, -30f, 1.25f)
                addProp(PropKind.STONE_RUIN, -7f, -32f, 12f, 1.4f)
                addProp(PropKind.SUPPLY_CRATE, -5.0f, 18.2f, 18f)
                addProp(PropKind.SUPPLY_CRATE, 5.5f, 18.0f, -14f)
            }
            TheatreBiome.BASRA_OUTSKIRTS -> {
                for (x in -18..18 step 4) {
                    addProp(PropKind.TIMBER_WALL, x.toFloat(), 19.6f, 0f)
                    if (abs(x) % 8 != 0) addProp(PropKind.SANDBAG, x.toFloat(), 16.0f, 0f)
                    addProp(PropKind.SANDBAG, x.toFloat(), -18.0f, 180f)
                }
                // Mud-brick riverside houses & date palms
                addBuilding(PropKind.CITY_HOUSE_BAZAAR, -14f, 5f, 90f, 3.0f, 3.2f)
                addBuilding(PropKind.CITY_HOUSE_LARGE, 15f, -3f, -90f, 3.0f, 3.4f)
                addBuilding(PropKind.CITY_HOUSE_BAZAAR, -13f, -26f, 0f, 3.1f, 2.8f)
                addBuilding(PropKind.CITY_HOUSE_LARGE, 14f, -29f, 180f, 3.3f, 3.0f)
                for (px in listOf(-20f, -9f, 9f, 20f)) {
                    addProp(PropKind.PALM_TREE, px, 12f, px * 10f)
                    addProp(PropKind.PALM_TREE, px * 0.9f, -13f, -px * 8f)
                }
                addProp(PropKind.BARBED_WIRE, -4f, 8f, 8f)
                addProp(PropKind.BARBED_WIRE, 5f, -8f, -10f)
                addProp(PropKind.SUPPLY_CRATE, -3.5f, 18.0f, 10f)
            }
            TheatreBiome.GAZA_CITY -> {
                // WHOLE CITY: Multi-block avenue layout!
                // Left & Right Street Facades creating real urban avenues & side alleys
                val rowZs = listOf(12f, -3f, -18f, -32f)
                for (rz in rowZs) {
                    // Inner block buildings flanking the central avenue (x = -11 and +11)
                    addBuilding(PropKind.CITY_HOUSE_LARGE, -11.5f, rz, 90f, 3.0f, 3.4f)
                    addBuilding(PropKind.CITY_HOUSE_BAZAAR, 11.5f, rz, -90f, 2.8f, 3.1f)
                    // Outer city blocks across side streets (x = -24 and +24)
                    addBuilding(PropKind.CITY_HOUSE_BAZAAR, -24.5f, rz + 2f, 90f, 2.8f, 3.1f)
                    addBuilding(PropKind.CITY_HOUSE_LARGE, 24.5f, rz - 2f, -90f, 3.0f, 3.4f)
                }
                // Iconic Ottoman Clocktower landmark at northern end of city
                addBuilding(PropKind.CITY_TOWER, 8.5f, -38f, 0f, 2.2f, 2.2f)
                addBuilding(PropKind.CITY_HOUSE_LARGE, -10f, -42f, 0f, 3.3f, 3.0f)
                addBuilding(PropKind.CITY_HOUSE_LARGE, 10f, -44f, 0f, 3.3f, 3.0f)

                // Market stalls & street sandbag barricades for tactical urban firefights
                addProp(PropKind.MARKET_STALL, -4.6f, 6.5f, 90f)
                addProp(PropKind.MARKET_STALL, 4.8f, -0.5f, -90f)
                addProp(PropKind.SANDBAG, -2.8f, 15.2f, 0f)
                addProp(PropKind.SANDBAG, 2.8f, 15.2f, 0f)
                addProp(PropKind.SANDBAG, -3.4f, -10f, 14f)
                addProp(PropKind.SANDBAG, 3.4f, -22f, -12f)
                addProp(PropKind.PALM_TREE, -5.5f, -13f, 20f)
                addProp(PropKind.PALM_TREE, 5.5f, -28f, -40f)
                addProp(PropKind.SUPPLY_CRATE, -4.0f, 17.6f, 22f)
                addProp(PropKind.SUPPLY_CRATE, 4.2f, -15.5f, -15f)
            }
            TheatreBiome.JERUSALEM_CITADEL -> {
                // WHOLE CITY + CITADEL GATE: Historic stone quarters & fortress square
                addProp(PropKind.CITY_GATE, 0f, 8.5f, 0f, 1.1f)
                // Gatehouse side bastion colliders (leaving wide walkable central archway [-2.6..2.6]!)
                colliders.add(BoxCollider2D(-6.4f, -2.7f, 6.8f, 10.2f))
                colliders.add(BoxCollider2D(2.7f, 6.4f, 6.8f, 10.2f))

                for (rz in listOf(15f, -4f, -19f, -34f)) {
                    addBuilding(PropKind.CITY_HOUSE_LARGE, -12.5f, rz, 90f, 3.0f, 3.4f)
                    addBuilding(PropKind.CITY_HOUSE_LARGE, 12.5f, rz, -90f, 3.0f, 3.4f)
                    addBuilding(PropKind.CITY_HOUSE_BAZAAR, -25.0f, rz, 90f, 2.8f, 3.1f)
                    addBuilding(PropKind.CITY_HOUSE_BAZAAR, 25.0f, rz, -90f, 2.8f, 3.1f)
                }
                addBuilding(PropKind.CITY_TOWER, -9.0f, -40f, 0f, 2.2f, 2.2f)
                addBuilding(PropKind.CITY_TOWER, 9.0f, -40f, 0f, 2.2f, 2.2f)
                addBuilding(PropKind.CITY_HOUSE_LARGE, 0f, -44f, 0f, 3.4f, 3.0f)

                addProp(PropKind.MARKET_STALL, -5.5f, -11f, 90f)
                addProp(PropKind.MARKET_STALL, 5.5f, -11f, -90f)
                addProp(PropKind.SANDBAG, 0f, 15.6f, 0f)
                addProp(PropKind.SANDBAG, -4.0f, -14f, 20f)
                addProp(PropKind.SANDBAG, 4.0f, -26f, -20f)
                addProp(PropKind.STONE_RUIN, -6.0f, -29f, 35f, 1.2f)
                addProp(PropKind.PALM_TREE, -6.5f, 1.5f, 10f)
                addProp(PropKind.PALM_TREE, 6.5f, 1.5f, -35f)
                addProp(PropKind.SUPPLY_CRATE, -3.2f, 18.0f, 12f)
            }
        }

        return props to colliders
    }
}
