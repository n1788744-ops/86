package com.example.game.render3d

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural 3D model library built with OpenGL ES 3.0 meshes:
 * - 8 detailed 1st-person WW1 weapons
 * - British (Brodie helmet) & Ottoman (Kabalak helmet) articulated soldiers
 * - Authentic Union Jack (Great Britain) & Ottoman Empire (Crescent & Star) 3D flags
 * - Full 3D city architecture (buildings, bazaar arches, clocktower, gatehouse, palms, stalls)
 * - Blood decal pools & combat VFX primitives
 */
class ModelCatalog3D {
    // First-person weapons
    val boltRifleBody: GpuMesh
    val boltHandle: GpuMesh
    val smgBody: GpuMesh
    val lmgBody: GpuMesh
    val sniperRifleBody: GpuMesh
    val revolverBody: GpuMesh
    val trenchShotgunBody: GpuMesh
    val mauserC96Body: GpuMesh
    val stripLmgBody: GpuMesh
    val fpArms: GpuMesh

    // Articulated soldier meshes
    val soldierTorso: GpuMesh
    val soldierHeadBrodie: GpuMesh
    val soldierHeadOttoman: GpuMesh
    val soldierLeg: GpuMesh
    val soldierArm: GpuMesh
    val worldRifle: GpuMesh

    // Authentic state flags
    val flagPole: GpuMesh
    val britishFlagCloth: GpuMesh
    val ottomanFlagCloth: GpuMesh

    // Battlefield & Whole-City Architecture props
    val sandbagWall: GpuMesh
    val timberWall: GpuMesh
    val barbedWire: GpuMesh
    val stoneRuin: GpuMesh
    val supplyCrate: GpuMesh
    val troopShip: GpuMesh
    val cityHouseLarge: GpuMesh
    val cityHouseBazaar: GpuMesh
    val cityTower: GpuMesh
    val cityGate: GpuMesh
    val marketStall: GpuMesh
    val palmTree: GpuMesh

    // VFX & Blood decals
    val unitSphere: GpuMesh
    val tracerBeam: GpuMesh
    val bloodDecal: GpuMesh

    init {
        boltRifleBody = buildBoltRifle(withScope = false)
        boltHandle = buildBoltHandle()
        smgBody = buildSmg()
        lmgBody = buildLmg()
        sniperRifleBody = buildBoltRifle(withScope = true)
        revolverBody = buildRevolver()
        trenchShotgunBody = buildTrenchShotgun()
        mauserC96Body = buildMauserC96()
        stripLmgBody = buildStripLmg()
        fpArms = buildFirstPersonHands()

        soldierTorso = buildSoldierTorso()
        soldierHeadBrodie = buildSoldierHeadBrodie()
        soldierHeadOttoman = buildSoldierHeadOttoman()
        soldierLeg = buildSoldierLeg()
        soldierArm = buildSoldierArm()
        worldRifle = buildWorldRifle()

        flagPole = buildFlagPole()
        britishFlagCloth = buildBritishUnionJackFlag()
        ottomanFlagCloth = buildOttomanEmpireFlag()

        sandbagWall = buildSandbagWall()
        timberWall = buildTimberWall()
        barbedWire = buildBarbedWire()
        stoneRuin = buildStoneRuin()
        supplyCrate = buildSupplyCrate()
        troopShip = buildTroopship()
        cityHouseLarge = buildCityHouseLarge()
        cityHouseBazaar = buildCityHouseBazaar()
        cityTower = buildCityTower()
        cityGate = buildCityGate()
        marketStall = buildMarketStall()
        palmTree = buildPalmTree()

        unitSphere = buildUnitSphere()
        tracerBeam = buildTracerBeam()
        bloodDecal = buildBloodDecal()
    }

    private fun buildBoltRifle(withScope: Boolean): GpuMesh {
        val m = MeshBuilder(8192)
        val wr = 0.42f; val wg = 0.25f; val wb = 0.14f
        val sr = 0.22f; val sg = 0.24f; val sb = 0.27f
        val br = 0.72f; val bg = 0.58f; val bb = 0.30f

        m.addBox(0f, -0.055f, 0.28f, 0.046f, 0.085f, 0.26f, wr, wg, wb, spec = 0.18f)
        m.addBox(0f, -0.025f, 0.13f, 0.042f, 0.055f, 0.12f, wr * 0.95f, wg * 0.95f, wb * 0.95f, spec = 0.18f)
        m.addBox(0f, -0.055f, 0.415f, 0.047f, 0.088f, 0.015f, br, bg, bb, spec = 0.85f)
        m.addBox(0f, -0.015f, -0.22f, 0.044f, 0.048f, 0.56f, wr, wg, wb, spec = 0.16f)
        m.addBox(0f, 0.012f, -0.20f, 0.036f, 0.024f, 0.44f, wr * 1.06f, wg * 1.06f, wb * 1.06f, spec = 0.16f)
        // Brass nosecap (SMLE signature)
        m.addBox(0f, 0.002f, -0.25f, 0.048f, 0.045f, 0.022f, sr, sg, sb, spec = 0.7f)
        m.addBox(0f, 0.002f, -0.49f, 0.048f, 0.044f, 0.032f, br * 0.85f, bg * 0.85f, bb * 0.85f, spec = 0.8f)

        m.addCylinderZ(0f, 0.018f, 0.02f, 0.020f, 0.020f, 0.16f, 12, sr * 1.25f, sg * 1.25f, sb * 1.25f, spec = 0.78f)
        m.addBox(0f, -0.036f, 0.04f, 0.014f, 0.028f, 0.06f, sr, sg, sb, spec = 0.65f)
        m.addBox(0f, -0.030f, 0.045f, 0.006f, 0.020f, 0.012f, br * 0.8f, bg * 0.8f, bb * 0.8f, spec = 0.8f)
        m.addCylinderZ(0f, 0.020f, -0.38f, 0.011f, 0.015f, 0.68f, 12, sr, sg, sb, spec = 0.82f)
        m.addBox(0f, -0.004f, -0.66f, 0.018f, 0.024f, 0.045f, sr, sg, sb, spec = 0.7f)
        m.addBox(0f, 0.036f, -0.11f, 0.022f, 0.014f, 0.04f, sr * 0.8f, sg * 0.8f, sb * 0.8f, spec = 0.6f)
        m.addBox(0f, 0.036f, -0.69f, 0.006f, 0.016f, 0.012f, sr * 0.8f, sg * 0.8f, sb * 0.8f, spec = 0.6f)

        if (withScope) {
            m.addBox(0f, 0.042f, -0.02f, 0.018f, 0.024f, 0.02f, sr, sg, sb, spec = 0.7f)
            m.addBox(0f, 0.042f, 0.06f, 0.018f, 0.024f, 0.02f, sr, sg, sb, spec = 0.7f)
            m.addCylinderZ(0f, 0.062f, 0.01f, 0.017f, 0.015f, 0.28f, 14, 0.14f, 0.15f, 0.17f, spec = 0.85f)
            m.addCylinderZ(0f, 0.062f, -0.12f, 0.019f, 0.019f, 0.02f, 14, br, bg, bb, spec = 0.9f)
            m.addCylinderZ(0f, 0.062f, 0.14f, 0.017f, 0.017f, 0.02f, 14, br, bg, bb, spec = 0.9f)
        }
        return m.buildGpuMesh()
    }

    private fun buildBoltHandle(): GpuMesh {
        val m = MeshBuilder(1024)
        m.addCylinderZ(0f, 0f, 0f, 0.012f, 0.012f, 0.11f, 10, 0.68f, 0.70f, 0.74f, spec = 0.95f)
        m.addCylinderX(0.028f, 0f, 0.035f, 0.006f, 0.052f, 8, 0.72f, 0.74f, 0.78f, spec = 0.95f)
        m.addSphere(0.058f, -0.006f, 0.035f, 0.015f, 0.015f, 0.015f, 8, 10, 0.78f, 0.80f, 0.84f, spec = 0.98f)
        return m.buildGpuMesh()
    }

    private fun buildSmg(): GpuMesh {
        val m = MeshBuilder(4096)
        val wr = 0.38f; val wg = 0.23f; val wb = 0.13f
        val sr = 0.20f; val sg = 0.22f; val sb = 0.24f

        m.addBox(0f, -0.045f, 0.24f, 0.045f, 0.080f, 0.24f, wr, wg, wb, spec = 0.16f)
        m.addBox(0f, -0.015f, 0.06f, 0.042f, 0.050f, 0.16f, wr, wg, wb, spec = 0.16f)
        m.addCylinderZ(0f, 0.018f, -0.04f, 0.022f, 0.022f, 0.24f, 12, sr, sg, sb, spec = 0.75f)
        m.addCylinderZ(0f, 0.018f, -0.28f, 0.024f, 0.024f, 0.28f, 12, sr * 1.15f, sg * 1.15f, sb * 1.15f, spec = 0.8f)
        m.addCylinderZ(0f, 0.018f, -0.45f, 0.011f, 0.011f, 0.10f, 10, sr * 0.8f, sg * 0.8f, sb * 0.8f, spec = 0.8f)
        m.addBox(-0.085f, 0.012f, -0.07f, 0.14f, 0.024f, 0.036f, sr * 0.85f, sg * 0.85f, sb * 0.85f, spec = 0.65f)
        m.addBox(0f, 0.042f, 0.04f, 0.018f, 0.012f, 0.02f, sr, sg, sb, spec = 0.7f)
        m.addBox(0f, 0.042f, -0.40f, 0.006f, 0.014f, 0.01f, sr, sg, sb, spec = 0.7f)
        return m.buildGpuMesh()
    }

    private fun buildLmg(): GpuMesh {
        val m = MeshBuilder(4096)
        val wr = 0.40f; val wg = 0.24f; val wb = 0.13f
        val sr = 0.21f; val sg = 0.22f; val sb = 0.24f

        m.addBox(0f, -0.045f, 0.25f, 0.052f, 0.092f, 0.25f, wr, wg, wb, spec = 0.15f)
        m.addBox(0f, -0.010f, 0.04f, 0.058f, 0.068f, 0.24f, sr, sg, sb, spec = 0.7f)
        m.addCylinderZ(0f, 0.018f, -0.34f, 0.040f, 0.042f, 0.56f, 14, sr * 0.92f, sg * 0.92f, sb * 0.92f, spec = 0.65f)
        m.addCylinderZ(0f, 0.018f, -0.66f, 0.014f, 0.014f, 0.12f, 10, sr * 0.7f, sg * 0.7f, sb * 0.7f, spec = 0.8f)
        m.addCylinderY(0f, 0.068f, -0.04f, 0.095f, 0.095f, 0.038f, 18, 0.18f, 0.19f, 0.21f, spec = 0.75f)
        m.addCylinderY(0f, 0.088f, -0.04f, 0.032f, 0.032f, 0.016f, 12, 0.68f, 0.55f, 0.28f, spec = 0.85f)
        m.addBox(0f, 0.062f, -0.59f, 0.008f, 0.018f, 0.012f, sr, sg, sb, spec = 0.7f)
        return m.buildGpuMesh()
    }

    private fun buildRevolver(): GpuMesh {
        val m = MeshBuilder(2048)
        val wr = 0.36f; val wg = 0.20f; val wb = 0.10f
        val sr = 0.28f; val sg = 0.30f; val sb = 0.33f
        m.addBox(0f, -0.055f, 0.08f, 0.030f, 0.090f, 0.044f, wr, wg, wb, spec = 0.2f)
        m.addBox(0f, -0.005f, 0.02f, 0.024f, 0.048f, 0.11f, sr, sg, sb, spec = 0.82f)
        m.addCylinderZ(0f, 0.014f, -0.01f, 0.024f, 0.024f, 0.052f, 12, sr * 1.15f, sg * 1.15f, sb * 1.15f, spec = 0.9f)
        m.addCylinderZ(0f, 0.022f, -0.14f, 0.013f, 0.014f, 0.19f, 8, sr, sg, sb, spec = 0.85f)
        m.addBox(0f, 0.040f, -0.22f, 0.005f, 0.012f, 0.012f, sr, sg, sb, spec = 0.8f)
        return m.buildGpuMesh()
    }

    /**
     * NEW 3D WEAPON: Winchester M1897 Trench Shotgun with perforated heat shield & pump slide
     */
    private fun buildTrenchShotgun(): GpuMesh {
        val m = MeshBuilder(4096)
        val wr = 0.44f; val wg = 0.24f; val wb = 0.12f
        val sr = 0.20f; val sg = 0.22f; val sb = 0.25f

        m.addBox(0f, -0.050f, 0.26f, 0.048f, 0.088f, 0.26f, wr, wg, wb, spec = 0.18f)
        m.addBox(0f, 0.005f, 0.02f, 0.042f, 0.062f, 0.19f, sr * 1.1f, sg * 1.1f, sb * 1.1f, spec = 0.82f)
        // Heavy 12-gauge barrel + perforated upper heat shield
        m.addCylinderZ(0f, 0.024f, -0.34f, 0.016f, 0.018f, 0.56f, 12, sr, sg, sb, spec = 0.85f)
        m.addCylinderZ(0f, 0.026f, -0.32f, 0.021f, 0.021f, 0.36f, 10, 0.28f, 0.30f, 0.33f, spec = 0.78f)
        // Under-barrel tubular magazine
        m.addCylinderZ(0f, -0.006f, -0.32f, 0.013f, 0.013f, 0.50f, 10, sr * 0.9f, sg * 0.9f, sb * 0.9f, spec = 0.75f)
        // Ribbed wooden pump forend slide
        m.addCylinderZ(0f, -0.006f, -0.24f, 0.024f, 0.024f, 0.19f, 10, wr * 0.92f, wg * 0.92f, wb * 0.92f, spec = 0.20f)
        // Bayonet lug + brass bead sight
        m.addBox(0f, -0.012f, -0.58f, 0.022f, 0.028f, 0.04f, sr, sg, sb, spec = 0.7f)
        m.addSphere(0f, 0.044f, -0.60f, 0.005f, 0.005f, 0.005f, 6, 6, 0.85f, 0.72f, 0.28f, spec = 0.95f)
        return m.buildGpuMesh()
    }

    /**
     * NEW 3D WEAPON: Mauser C96 "Broomhandle" Pistol-Carbine
     */
    private fun buildMauserC96(): GpuMesh {
        val m = MeshBuilder(2048)
        val wr = 0.42f; val wg = 0.23f; val wb = 0.11f
        val sr = 0.23f; val sg = 0.25f; val sb = 0.28f

        // Iconic broomhandle cylindrical wood grip
        m.addCylinderY(0f, -0.058f, 0.10f, 0.019f, 0.016f, 0.105f, 10, wr, wg, wb, spec = 0.22f)
        // Forward internal box magazine ahead of trigger guard
        m.addBox(0f, -0.032f, -0.04f, 0.025f, 0.068f, 0.046f, sr * 0.9f, sg * 0.9f, sb * 0.9f, spec = 0.75f)
        // Receiver & bolt housing
        m.addBox(0f, 0.016f, 0.02f, 0.026f, 0.040f, 0.16f, sr * 1.1f, sg * 1.1f, sb * 1.1f, spec = 0.85f)
        // Slender tapered barrel
        m.addCylinderZ(0f, 0.024f, -0.17f, 0.010f, 0.013f, 0.22f, 10, sr, sg, sb, spec = 0.88f)
        m.addBox(0f, 0.040f, -0.04f, 0.016f, 0.010f, 0.032f, sr, sg, sb, spec = 0.8f)
        m.addBox(0f, 0.038f, -0.27f, 0.004f, 0.012f, 0.010f, sr, sg, sb, spec = 0.8f)
        return m.buildGpuMesh()
    }

    /**
     * NEW 3D WEAPON: Hotchkiss M1909 Strip-Fed LMG with brass cooling fins
     */
    private fun buildStripLmg(): GpuMesh {
        val m = MeshBuilder(4096)
        val wr = 0.38f; val wg = 0.22f; val wb = 0.12f
        val sr = 0.22f; val sg = 0.24f; val sb = 0.26f
        val br = 0.78f; val bg = 0.62f; val bb = 0.28f

        m.addBox(0f, -0.042f, 0.24f, 0.048f, 0.085f, 0.24f, wr, wg, wb, spec = 0.16f)
        m.addBox(0f, 0.008f, 0.02f, 0.056f, 0.064f, 0.24f, sr, sg, sb, spec = 0.75f)
        // Barrel + 4 brass radiator cooling rings
        m.addCylinderZ(0f, 0.020f, -0.38f, 0.016f, 0.018f, 0.64f, 12, sr, sg, sb, spec = 0.82f)
        for (k in 0..3) {
            m.addCylinderZ(0f, 0.020f, -0.15f - k * 0.045f, 0.030f, 0.030f, 0.018f, 12, br, bg, bb, spec = 0.88f)
        }
        // Horizontal brass feed strip sticking out the right side
        m.addBox(0.095f, 0.015f, -0.01f, 0.14f, 0.012f, 0.065f, br, bg, bb, spec = 0.9f)
        m.addBox(0f, 0.048f, -0.66f, 0.006f, 0.016f, 0.010f, sr, sg, sb, spec = 0.75f)
        return m.buildGpuMesh()
    }

    private fun buildFirstPersonHands(): GpuMesh {
        val m = MeshBuilder(2048)
        val kr = 0.76f; val kg = 0.58f; val kb = 0.47f
        val cr = 0.46f; val cg = 0.42f; val cb = 0.30f // British khaki cuffs

        m.addBox(-0.07f, -0.11f, -0.10f, 0.068f, 0.068f, 0.22f, cr, cg, cb, spec = 0.05f)
        m.addSphere(-0.02f, -0.035f, -0.22f, 0.036f, 0.030f, 0.046f, 8, 10, kr, kg, kb, spec = 0.12f)
        m.addBox(0.08f, -0.10f, 0.16f, 0.070f, 0.068f, 0.20f, cr, cg, cb, spec = 0.05f)
        m.addSphere(0.036f, -0.032f, 0.085f, 0.034f, 0.032f, 0.046f, 8, 10, kr, kg, kb, spec = 0.12f)
        return m.buildGpuMesh()
    }

    private fun buildSoldierTorso(): GpuMesh {
        val m = MeshBuilder(4096)
        val coat = 0.88f
        val leatherR = 0.42f; val leatherG = 0.27f; val leatherB = 0.15f
        val brassR = 0.82f; val brassG = 0.68f; val brassB = 0.32f

        m.addBox(0f, 1.14f, 0f, 0.42f, 0.50f, 0.25f, coat, coat, coat, spec = 0.06f)
        m.addBox(0f, 0.82f, 0f, 0.45f, 0.22f, 0.27f, coat * 0.92f, coat * 0.92f, coat * 0.92f, spec = 0.05f)
        m.addCylinderY(0f, 1.41f, 0f, 0.11f, 0.10f, 0.08f, 10, coat * 0.85f, coat * 0.85f, coat * 0.85f, spec = 0.05f)
        m.addBox(0f, 0.95f, 0f, 0.44f, 0.065f, 0.265f, leatherR, leatherG, leatherB, spec = 0.25f)
        m.addBox(0f, 0.95f, 0.136f, 0.07f, 0.068f, 0.016f, brassR, brassG, brassB, spec = 0.85f)
        m.addBox(-0.12f, 0.95f, 0.145f, 0.10f, 0.09f, 0.045f, leatherR * 1.1f, leatherG * 1.1f, leatherB * 1.1f, spec = 0.22f)
        m.addBox(0.12f, 0.95f, 0.145f, 0.10f, 0.09f, 0.045f, leatherR * 1.1f, leatherG * 1.1f, leatherB * 1.1f, spec = 0.22f)
        m.addBox(-0.11f, 1.18f, 0.128f, 0.045f, 0.42f, 0.018f, leatherR, leatherG, leatherB, spec = 0.22f)
        m.addBox(0.11f, 1.18f, 0.128f, 0.045f, 0.42f, 0.018f, leatherR, leatherG, leatherB, spec = 0.22f)
        m.addBox(0f, 1.17f, -0.17f, 0.30f, 0.34f, 0.13f, 0.50f, 0.46f, 0.34f, spec = 0.08f)
        m.addCylinderX(0f, 1.37f, -0.16f, 0.058f, 0.34f, 10, 0.56f, 0.52f, 0.42f, spec = 0.06f)
        return m.buildGpuMesh()
    }

    /**
     * British Army M1915 Brodie Steel Helmet (wide shallow dish brim)
     */
    private fun buildSoldierHeadBrodie(): GpuMesh {
        val m = MeshBuilder(2048)
        m.addSphere(0f, 1.54f, 0f, 0.115f, 0.135f, 0.12f, 8, 12, 0.78f, 0.60f, 0.48f, spec = 0.12f)
        m.addBox(0f, 1.51f, 0.118f, 0.072f, 0.018f, 0.018f, 0.24f, 0.18f, 0.14f, spec = 0.05f)
        // Brodie dome + signature wide flat steel rim
        m.addSphere(0f, 1.60f, 0f, 0.138f, 0.095f, 0.142f, 8, 14, 0.40f, 0.39f, 0.28f, spec = 0.45f, maxPhiRatio = 0.55f)
        m.addCylinderY(0f, 1.60f, 0f, 0.195f, 0.178f, 0.018f, 16, 0.38f, 0.37f, 0.27f, spec = 0.5f)
        return m.buildGpuMesh()
    }

    /**
     * Ottoman Empire Kabalak / Enveriye Wrapped Sun Helmet
     */
    private fun buildSoldierHeadOttoman(): GpuMesh {
        val m = MeshBuilder(2048)
        m.addSphere(0f, 1.54f, 0f, 0.115f, 0.135f, 0.12f, 8, 12, 0.72f, 0.54f, 0.42f, spec = 0.12f)
        m.addBox(0f, 1.51f, 0.118f, 0.076f, 0.020f, 0.018f, 0.16f, 0.12f, 0.10f, spec = 0.05f)
        // Wrapped Kabalak crown + front peak + neck cloth
        m.addCylinderY(0f, 1.64f, 0f, 0.135f, 0.120f, 0.12f, 12, 0.56f, 0.50f, 0.36f, spec = 0.08f)
        m.addSphere(0f, 1.70f, 0f, 0.122f, 0.065f, 0.122f, 6, 12, 0.58f, 0.52f, 0.38f, spec = 0.08f, maxPhiRatio = 0.52f)
        m.addBox(0f, 1.58f, 0.10f, 0.22f, 0.022f, 0.11f, 0.52f, 0.46f, 0.33f, spec = 0.08f)
        // Small brass crescent badge on front
        m.addBox(0f, 1.64f, 0.132f, 0.032f, 0.032f, 0.012f, 0.86f, 0.72f, 0.30f, spec = 0.9f)
        return m.buildGpuMesh()
    }

    private fun buildSoldierLeg(): GpuMesh {
        val m = MeshBuilder(1024)
        m.addBox(0f, -0.22f, 0f, 0.15f, 0.44f, 0.16f, 0.85f, 0.85f, 0.85f, spec = 0.05f)
        m.addCylinderY(0f, -0.54f, 0f, 0.062f, 0.068f, 0.24f, 10, 0.52f, 0.47f, 0.36f, spec = 0.06f)
        m.addBox(0f, -0.70f, 0.03f, 0.11f, 0.10f, 0.21f, 0.22f, 0.16f, 0.12f, spec = 0.28f)
        return m.buildGpuMesh()
    }

    private fun buildSoldierArm(): GpuMesh {
        val m = MeshBuilder(1024)
        m.addBox(0f, -0.20f, 0f, 0.11f, 0.40f, 0.11f, 0.86f, 0.86f, 0.86f, spec = 0.05f)
        m.addSphere(0f, -0.43f, 0f, 0.05f, 0.06f, 0.05f, 6, 8, 0.76f, 0.58f, 0.46f, spec = 0.12f)
        return m.buildGpuMesh()
    }

    private fun buildWorldRifle(): GpuMesh {
        val m = MeshBuilder(1024)
        m.addBox(0f, 0f, -0.2f, 0.045f, 0.065f, 0.82f, 0.42f, 0.25f, 0.14f, spec = 0.18f)
        m.addCylinderZ(0f, 0.025f, -0.42f, 0.014f, 0.018f, 0.78f, 8, 0.20f, 0.22f, 0.25f, spec = 0.8f)
        return m.buildGpuMesh()
    }

    private fun buildFlagPole(): GpuMesh {
        val m = MeshBuilder(1024)
        m.addCylinderY(0f, 2.35f, 0f, 0.055f, 0.042f, 4.7f, 10, 0.45f, 0.43f, 0.38f, spec = 0.5f)
        m.addSphere(0f, 4.75f, 0f, 0.10f, 0.10f, 0.10f, 6, 8, 0.85f, 0.70f, 0.30f, spec = 0.9f)
        return m.buildGpuMesh()
    }

    /**
     * Authentic 3D Flag of Great Britain (Union Jack) — visible on both faces!
     */
    private fun buildBritishUnionJackFlag(): GpuMesh {
        val m = MeshBuilder(4096)
        val x0 = 0.05f; val x1 = 1.55f
        val y0 = 3.65f; val y1 = 4.62f
        val cx = (x0 + x1) * 0.5f
        val cy = (y0 + y1) * 0.5f

        val navR = 0.02f; val navG = 0.14f; val navB = 0.42f
        val redR = 0.78f; val redG = 0.07f; val redB = 0.16f
        val whtR = 0.95f; val whtG = 0.95f; val whtB = 0.95f

        // Base navy blue cloth
        m.addBox(cx, cy, 0f, x1 - x0, y1 - y0, 0.020f, navR, navG, navB, spec = 0.06f)

        // Diagonal white saltires (front +Z and back -Z slightly proud)
        fun addDoubleSidedQuad(ax: Float, ay: Float, bx: Float, by: Float, cxp: Float, cyp: Float, dx: Float, dy: Float, zOff: Float, r: Float, g: Float, b: Float) {
            m.addQuad(Vec3(ax, ay, zOff), Vec3(bx, by, zOff), Vec3(cxp, cyp, zOff), Vec3(dx, dy, zOff), r, g, b, spec = 0.06f)
            m.addQuad(Vec3(dx, dy, -zOff), Vec3(cxp, cyp, -zOff), Vec3(bx, by, -zOff), Vec3(ax, ay, -zOff), r, g, b, spec = 0.06f)
        }

        // White diagonals (top-left to bottom-right and bottom-left to top-right)
        val wDiag = 0.11f
        addDoubleSidedQuad(x0, y1 - wDiag, x0 + wDiag * 1.4f, y1, x1, y0 + wDiag, x1 - wDiag * 1.4f, y0, 0.013f, whtR, whtG, whtB)
        addDoubleSidedQuad(x0, y0 + wDiag, x1 - wDiag * 1.4f, y1, x1, y1 - wDiag, x0 + wDiag * 1.4f, y0, 0.013f, whtR, whtG, whtB)

        // Red inner diagonals (St. Patrick saltire)
        val rDiag = 0.052f
        addDoubleSidedQuad(x0, y1 - rDiag, x0 + rDiag * 1.4f, y1, x1, y0 + rDiag, x1 - rDiag * 1.4f, y0, 0.016f, redR, redG, redB)
        addDoubleSidedQuad(x0, y0 + rDiag, x1 - rDiag * 1.4f, y1, x1, y1 - rDiag, x0 + rDiag * 1.4f, y0, 0.016f, redR, redG, redB)

        // White St. George Cross fimbriation
        m.addBox(cx, cy, 0f, x1 - x0 + 0.004f, 0.28f, 0.036f, whtR, whtG, whtB, spec = 0.06f)
        m.addBox(cx, cy, 0f, 0.28f, y1 - y0 + 0.004f, 0.036f, whtR, whtG, whtB, spec = 0.06f)

        // Red St. George Cross
        m.addBox(cx, cy, 0f, x1 - x0 + 0.006f, 0.17f, 0.042f, redR, redG, redB, spec = 0.08f)
        m.addBox(cx, cy, 0f, 0.17f, y1 - y0 + 0.006f, 0.042f, redR, redG, redB, spec = 0.08f)

        return m.buildGpuMesh()
    }

    /**
     * Authentic 3D Flag of the Ottoman Empire (Crimson field + White Crescent & 5-Pointed Star)
     */
    private fun buildOttomanEmpireFlag(): GpuMesh {
        val m = MeshBuilder(4096)
        val x0 = 0.05f; val x1 = 1.55f
        val y0 = 3.65f; val y1 = 4.62f
        val cx = (x0 + x1) * 0.5f
        val cy = (y0 + y1) * 0.5f

        val redR = 0.86f; val redG = 0.05f; val redB = 0.10f
        val whtR = 0.96f; val whtG = 0.96f; val whtB = 0.96f

        // Crimson cloth
        m.addBox(cx, cy, 0f, x1 - x0, y1 - y0, 0.022f, redR, redG, redB, spec = 0.06f)

        // Crescent built as C-arc segments around (cresX, cy) on both sides
        val cresX = x0 + (x1 - x0) * 0.40f
        val outerR = 0.25f
        val innerR = 0.19f
        val innerOffX = 0.065f
        val segs = 22
        val startA = 0.52f
        val endA = 2f * PI.toFloat() - 0.52f
        for (i in 0 until segs) {
            val a0 = startA + (endA - startA) * (i.toFloat() / segs)
            val a1 = startA + (endA - startA) * ((i + 1).toFloat() / segs)
            val o0 = Vec3(cresX + cos(a0) * outerR, cy + sin(a0) * outerR, 0.015f)
            val o1 = Vec3(cresX + cos(a1) * outerR, cy + sin(a1) * outerR, 0.015f)
            val i1 = Vec3(cresX + innerOffX + cos(a1) * innerR, cy + sin(a1) * innerR, 0.015f)
            val i0 = Vec3(cresX + innerOffX + cos(a0) * innerR, cy + sin(a0) * innerR, 0.015f)
            m.addQuad(o0, i0, i1, o1, whtR, whtG, whtB, spec = 0.08f)

            // Back side (-Z)
            val bo0 = o0.copy(z = -0.015f)
            val bo1 = o1.copy(z = -0.015f)
            val bi1 = i1.copy(z = -0.015f)
            val bi0 = i0.copy(z = -0.015f)
            m.addQuad(bo1, bi1, bi0, bo0, whtR, whtG, whtB, spec = 0.08f)
        }

        // 5-Pointed Star to the right of crescent
        val starX = cresX + 0.30f
        val starOuter = 0.115f
        val starInner = 0.046f
        for (p in 0 until 5) {
            val aTip = (p * 72f + 18f) * (PI.toFloat() / 180f)
            val aL = ((p * 72f) - 18f) * (PI.toFloat() / 180f)
            val aR = ((p * 72f) + 54f) * (PI.toFloat() / 180f)
            val tip = Vec3(starX + cos(aTip) * starOuter, cy + sin(aTip) * starOuter, 0.016f)
            val left = Vec3(starX + cos(aL) * starInner, cy + sin(aL) * starInner, 0.016f)
            val right = Vec3(starX + cos(aR) * starInner, cy + sin(aR) * starInner, 0.016f)
            val center = Vec3(starX, cy, 0.016f)
            m.addTri(center, left, tip, whtR, whtG, whtB, spec = 0.08f)
            m.addTri(center, tip, right, whtR, whtG, whtB, spec = 0.08f)

            val bTip = tip.copy(z = -0.016f)
            val bLeft = left.copy(z = -0.016f)
            val bRight = right.copy(z = -0.016f)
            val bCenter = center.copy(z = -0.016f)
            m.addTri(bCenter, bTip, bLeft, whtR, whtG, whtB, spec = 0.08f)
            m.addTri(bCenter, bRight, bTip, whtR, whtG, whtB, spec = 0.08f)
        }
        return m.buildGpuMesh()
    }

    private fun buildSandbagWall(): GpuMesh {
        val m = MeshBuilder(4096)
        for (row in 0 until 3) {
            val y = 0.13f + row * 0.23f
            val offsetX = if (row % 2 == 1) 0.24f else 0f
            for (c in 0 until 4) {
                val x = -0.85f + c * 0.56f + offsetX
                val shade = 0.88f + ((row * 3 + c) % 4) * 0.04f
                m.addSphere(
                    cx = x, cy = y, cz = ((c % 2) * 0.04f - 0.02f),
                    rx = 0.29f, ry = 0.135f, rz = 0.20f,
                    stacks = 5, slices = 8,
                    r = 0.65f * shade, g = 0.58f * shade, b = 0.43f * shade,
                    spec = 0.04f
                )
            }
        }
        return m.buildGpuMesh()
    }

    private fun buildTimberWall(): GpuMesh {
        val m = MeshBuilder(2048)
        val wr = 0.36f; val wg = 0.26f; val wb = 0.16f
        m.addBox(-0.95f, 0.65f, 0f, 0.14f, 1.35f, 0.14f, wr * 0.85f, wg * 0.85f, wb * 0.85f, spec = 0.06f)
        m.addBox(0f, 0.65f, 0f, 0.14f, 1.35f, 0.14f, wr * 0.85f, wg * 0.85f, wb * 0.85f, spec = 0.06f)
        m.addBox(0.95f, 0.65f, 0f, 0.14f, 1.35f, 0.14f, wr * 0.85f, wg * 0.85f, wb * 0.85f, spec = 0.06f)
        for (i in 0 until 5) {
            val shade = 0.90f + (i % 3) * 0.06f
            m.addBox(0f, 0.15f + i * 0.23f, 0.06f, 2.15f, 0.19f, 0.06f, wr * shade, wg * shade, wb * shade, spec = 0.05f)
        }
        return m.buildGpuMesh()
    }

    private fun buildBarbedWire(): GpuMesh {
        val m = MeshBuilder(4096)
        val wr = 0.34f; val wg = 0.25f; val wb = 0.17f
        for (sx in floatArrayOf(-0.95f, 0.95f)) {
            m.addBox(sx, 0.48f, 0f, 0.08f, 1.05f, 0.08f, wr, wg, wb, spec = 0.05f)
            m.addBox(sx, 0.44f, 0.18f, 0.07f, 0.92f, 0.07f, wr * 0.9f, wg * 0.9f, wb * 0.9f, spec = 0.05f)
            m.addBox(sx, 0.44f, -0.18f, 0.07f, 0.92f, 0.07f, wr * 0.9f, wg * 0.9f, wb * 0.9f, spec = 0.05f)
        }
        val ir = 0.36f; val ig = 0.32f; val ib = 0.28f
        for (y in floatArrayOf(0.25f, 0.52f, 0.78f)) {
            m.addCylinderX(0f, y, 0f, 0.015f, 2.2f, 6, ir, ig, ib, spec = 0.55f)
            m.addCylinderX(0f, y - 0.08f, 0.16f, 0.013f, 2.2f, 6, ir, ig, ib, spec = 0.55f)
            m.addCylinderX(0f, y - 0.08f, -0.16f, 0.013f, 2.2f, 6, ir, ig, ib, spec = 0.55f)
        }
        for (k in 0..8) {
            val x = -0.9f + k * 0.22f
            val y = 0.48f + 0.15f * sin(k * 1.4f)
            m.addBox(x, y, 0.12f, 0.03f, 0.28f, 0.03f, ir, ig, ib, spec = 0.6f)
            m.addBox(x + 0.08f, y, -0.12f, 0.03f, 0.28f, 0.03f, ir, ig, ib, spec = 0.6f)
        }
        return m.buildGpuMesh()
    }

    private fun buildStoneRuin(): GpuMesh {
        val m = MeshBuilder(2048)
        val sr = 0.58f; val sg = 0.52f; val sb = 0.42f
        m.addBox(0f, 0.65f, 0f, 2.6f, 1.3f, 0.55f, sr, sg, sb, spec = 0.06f)
        m.addBox(-0.75f, 1.55f, 0f, 1.0f, 0.55f, 0.50f, sr * 0.94f, sg * 0.94f, sb * 0.94f, spec = 0.06f)
        m.addBox(0.85f, 1.45f, 0f, 0.75f, 0.35f, 0.48f, sr * 1.04f, sg * 1.04f, sb * 1.04f, spec = 0.06f)
        m.addBox(-0.3f, 0.25f, 0.32f, 0.65f, 0.45f, 0.35f, sr * 0.82f, sg * 0.84f, sb * 0.78f, spec = 0.05f)
        return m.buildGpuMesh()
    }

    private fun buildSupplyCrate(): GpuMesh {
        val m = MeshBuilder(1024)
        m.addBox(0f, 0.32f, 0f, 0.85f, 0.64f, 0.62f, 0.45f, 0.36f, 0.22f, spec = 0.08f)
        m.addBox(0f, 0.65f, 0f, 0.89f, 0.06f, 0.66f, 0.36f, 0.28f, 0.17f, spec = 0.08f)
        m.addBox(0f, 0.32f, 0f, 0.87f, 0.08f, 0.64f, 0.22f, 0.24f, 0.26f, spec = 0.6f)
        return m.buildGpuMesh()
    }

    private fun buildTroopship(): GpuMesh {
        val m = MeshBuilder(4096)
        m.addBox(0f, 1.2f, 0f, 26f, 2.4f, 6.2f, 0.46f, 0.19f, 0.16f, spec = 0.22f)
        m.addBox(0f, 3.3f, 0f, 27.5f, 2.0f, 6.4f, 0.26f, 0.28f, 0.30f, spec = 0.35f)
        m.addBox(-15.5f, 2.5f, 0f, 4.5f, 3.6f, 3.4f, 0.25f, 0.27f, 0.29f, spec = 0.35f)
        m.addBox(15.2f, 2.4f, 0f, 3.8f, 3.4f, 3.8f, 0.25f, 0.27f, 0.29f, spec = 0.35f)
        m.addBox(0f, 5.2f, 0f, 11.5f, 2.1f, 4.6f, 0.80f, 0.79f, 0.75f, spec = 0.12f)
        m.addBox(-1.5f, 6.8f, 0f, 5.2f, 1.4f, 3.6f, 0.84f, 0.83f, 0.80f, spec = 0.15f)
        m.addCylinderY(-2.0f, 8.2f, 0f, 0.85f, 0.80f, 4.2f, 12, 0.22f, 0.22f, 0.24f, spec = 0.4f)
        m.addCylinderY(2.2f, 8.2f, 0f, 0.85f, 0.80f, 4.2f, 12, 0.22f, 0.22f, 0.24f, spec = 0.4f)
        m.addCylinderY(-9.0f, 9.0f, 0f, 0.20f, 0.10f, 10.5f, 8, 0.42f, 0.35f, 0.25f, spec = 0.15f)
        m.addCylinderY(9.0f, 8.8f, 0f, 0.20f, 0.10f, 10.0f, 8, 0.42f, 0.35f, 0.25f, spec = 0.15f)
        return m.buildGpuMesh()
    }

    /**
     * WHOLE CITY: Multi-story Sandstone Townhouse (6.4m x 6.8m x 5.6m)
     */
    private fun buildCityHouseLarge(): GpuMesh {
        val m = MeshBuilder(4096)
        val stR = 0.72f; val stG = 0.64f; val stB = 0.50f
        val wdR = 0.34f; val wdG = 0.23f; val wdB = 0.14f
        val winR = 0.12f; val winG = 0.13f; val winB = 0.15f

        // Main limestone block
        m.addBox(0f, 3.1f, 0f, 6.4f, 6.2f, 5.6f, stR, stG, stB, spec = 0.06f)
        // Roof parapet ledge
        m.addBox(0f, 6.4f, 0f, 6.7f, 0.55f, 5.9f, stR * 0.90f, stG * 0.90f, stB * 0.90f, spec = 0.06f)
        // Small rooftop dome / stairwell
        m.addBox(-1.2f, 7.0f, -0.8f, 2.2f, 1.1f, 2.2f, stR * 0.95f, stG * 0.95f, stB * 0.95f, spec = 0.06f)

        // Doors & windows on front (+Z) and back (-Z)
        for (zSide in floatArrayOf(2.82f, -2.82f)) {
            // Wooden front door
            m.addBox(0f, 1.15f, zSide, 1.25f, 2.3f, 0.10f, wdR, wdG, wdB, spec = 0.12f)
            // Upper floor shuttered windows
            for (wx in floatArrayOf(-1.8f, 0f, 1.8f)) {
                m.addBox(wx, 4.3f, zSide, 0.85f, 1.25f, 0.10f, winR, winG, winB, spec = 0.3f)
                m.addBox(wx, 5.02f, zSide, 1.05f, 0.16f, 0.16f, wdR, wdG, wdB, spec = 0.1f)
            }
        }
        // Side windows (+X / -X)
        for (xSide in floatArrayOf(3.22f, -3.22f)) {
            for (wz in floatArrayOf(-1.4f, 1.4f)) {
                m.addBox(xSide, 4.3f, wz, 0.10f, 1.20f, 0.85f, winR, winG, winB, spec = 0.3f)
                m.addBox(xSide, 1.9f, wz, 0.10f, 1.05f, 0.78f, winR, winG, winB, spec = 0.3f)
            }
        }
        // Wooden balcony overhang
        m.addBox(0f, 3.15f, 3.15f, 3.2f, 0.22f, 0.85f, wdR * 1.1f, wdG * 1.1f, wdB * 1.1f, spec = 0.08f)
        m.addBox(0f, 3.65f, 3.52f, 3.2f, 0.75f, 0.12f, wdR, wdG, wdB, spec = 0.08f)
        return m.buildGpuMesh()
    }

    /**
     * WHOLE CITY: 2-Story Bazaar Corner Building with Arches & Warm Terracotta Trim
     */
    private fun buildCityHouseBazaar(): GpuMesh {
        val m = MeshBuilder(4096)
        val stR = 0.68f; val stG = 0.56f; val stB = 0.43f
        val trR = 0.58f; val trG = 0.34f; val trB = 0.24f

        // Upper story
        m.addBox(0f, 4.0f, 0f, 6.0f, 3.2f, 5.2f, stR, stG, stB, spec = 0.06f)
        // Ground pillars & shaded colonnade
        m.addBox(-2.4f, 1.2f, 2.1f, 1.0f, 2.4f, 1.0f, stR * 0.92f, stG * 0.92f, stB * 0.92f, spec = 0.06f)
        m.addBox(2.4f, 1.2f, 2.1f, 1.0f, 2.4f, 1.0f, stR * 0.92f, stG * 0.92f, stB * 0.92f, spec = 0.06f)
        m.addBox(0f, 1.2f, -0.8f, 5.8f, 2.4f, 3.4f, stR * 0.86f, stG * 0.86f, stB * 0.86f, spec = 0.06f)
        // Terracotta awning & parapet
        m.addBox(0f, 2.45f, 2.65f, 6.2f, 0.28f, 1.3f, trR, trG, trB, spec = 0.08f)
        m.addBox(0f, 5.75f, 0f, 6.25f, 0.40f, 5.45f, stR * 0.88f, stG * 0.88f, stB * 0.88f, spec = 0.06f)
        // Upper windows
        for (wx in floatArrayOf(-1.6f, 0f, 1.6f)) {
            m.addBox(wx, 4.1f, 2.62f, 0.78f, 1.15f, 0.10f, 0.14f, 0.14f, 0.16f, spec = 0.25f)
        }
        return m.buildGpuMesh()
    }

    /**
     * WHOLE CITY: Tall Ottoman Stone Clocktower / Minaret Landmark
     */
    private fun buildCityTower(): GpuMesh {
        val m = MeshBuilder(4096)
        val stR = 0.74f; val stG = 0.68f; val stB = 0.56f
        // Stone base plinth
        m.addBox(0f, 2.2f, 0f, 4.2f, 4.4f, 4.2f, stR * 0.92f, stG * 0.92f, stB * 0.92f, spec = 0.08f)
        // Main tall shaft
        m.addBox(0f, 8.2f, 0f, 3.0f, 8.0f, 3.0f, stR, stG, stB, spec = 0.08f)
        // Gallery balcony ring
        m.addBox(0f, 11.8f, 0f, 3.8f, 0.65f, 3.8f, stR * 0.86f, stG * 0.86f, stB * 0.86f, spec = 0.1f)
        // Clock faces on 4 sides
        for (s in floatArrayOf(1.52f, -1.52f)) {
            m.addBox(0f, 9.8f, s, 1.3f, 1.3f, 0.08f, 0.92f, 0.88f, 0.78f, spec = 0.2f)
            m.addBox(s, 9.8f, 0f, 0.08f, 1.3f, 1.3f, 0.92f, 0.88f, 0.78f, spec = 0.2f)
        }
        // Lead dome & brass finial spire
        m.addSphere(0f, 13.0f, 0f, 1.55f, 1.45f, 1.55f, 8, 14, 0.26f, 0.30f, 0.34f, spec = 0.55f, maxPhiRatio = 0.55f)
        m.addCylinderY(0f, 14.9f, 0f, 0.08f, 0.03f, 1.6f, 8, 0.85f, 0.70f, 0.30f, spec = 0.9f)
        return m.buildGpuMesh()
    }

    /**
     * WHOLE CITY: Fortified Stone City Gatehouse
     */
    private fun buildCityGate(): GpuMesh {
        val m = MeshBuilder(2048)
        val stR = 0.64f; val stG = 0.58f; val stB = 0.48f
        // Left & right bastion towers
        m.addBox(-4.2f, 2.8f, 0f, 3.0f, 5.6f, 3.4f, stR, stG, stB, spec = 0.06f)
        m.addBox(4.2f, 2.8f, 0f, 3.0f, 5.6f, 3.4f, stR, stG, stB, spec = 0.06f)
        // Overhead gatehouse bridge (walkable underneath!)
        m.addBox(0f, 4.6f, 0f, 5.6f, 2.0f, 2.6f, stR * 0.94f, stG * 0.94f, stB * 0.94f, spec = 0.06f)
        // Crenellations
        for (cx in listOf(-5.0f, -3.4f, -1.6f, 0f, 1.6f, 3.4f, 5.0f)) {
            m.addBox(cx, 5.85f, 1.1f, 0.8f, 0.55f, 0.5f, stR * 0.88f, stG * 0.88f, stB * 0.88f, spec = 0.06f)
        }
        return m.buildGpuMesh()
    }

    private fun buildMarketStall(): GpuMesh {
        val m = MeshBuilder(1024)
        val wr = 0.42f; val wg = 0.28f; val wb = 0.16f
        m.addBox(-1.1f, 1.1f, -0.7f, 0.10f, 2.2f, 0.10f, wr, wg, wb, spec = 0.08f)
        m.addBox(1.1f, 1.1f, -0.7f, 0.10f, 2.2f, 0.10f, wr, wg, wb, spec = 0.08f)
        m.addBox(-1.1f, 1.1f, 0.7f, 0.10f, 2.2f, 0.10f, wr, wg, wb, spec = 0.08f)
        m.addBox(1.1f, 1.1f, 0.7f, 0.10f, 2.2f, 0.10f, wr, wg, wb, spec = 0.08f)
        // Slanted canvas canopy
        m.addBox(0f, 2.25f, 0f, 2.5f, 0.10f, 1.8f, 0.76f, 0.34f, 0.22f, spec = 0.05f)
        // Counter table
        m.addBox(0f, 0.82f, 0.2f, 2.2f, 0.16f, 0.8f, wr * 1.1f, wg * 1.1f, wb * 1.1f, spec = 0.08f)
        return m.buildGpuMesh()
    }

    private fun buildPalmTree(): GpuMesh {
        val m = MeshBuilder(2048)
        // Segmented trunk
        for (i in 0 until 6) {
            m.addCylinderY(i * 0.08f, 0.45f + i * 0.85f, 0f, 0.24f - i * 0.015f, 0.22f - i * 0.015f, 0.90f, 8, 0.44f, 0.33f, 0.22f, spec = 0.06f)
        }
        // Crown of palm fronds
        val topX = 0.40f
        val topY = 5.4f
        for (f in 0 until 7) {
            val a = f * (2f * PI.toFloat() / 7f)
            val dx = cos(a)
            val dz = sin(a)
            m.addQuad(
                Vec3(topX, topY + 0.1f, 0f),
                Vec3(topX + dx * 1.1f - dz * 0.22f, topY + 0.35f, dz * 1.1f + dx * 0.22f),
                Vec3(topX + dx * 2.1f, topY - 0.35f, dz * 2.1f),
                Vec3(topX + dx * 1.1f + dz * 0.22f, topY + 0.35f, dz * 1.1f - dx * 0.22f),
                0.24f, 0.44f, 0.20f, spec = 0.1f
            )
            // Back face so fronds are visible from below too
            m.addQuad(
                Vec3(topX + dx * 1.1f + dz * 0.22f, topY + 0.35f, dz * 1.1f - dx * 0.22f),
                Vec3(topX + dx * 2.1f, topY - 0.35f, dz * 2.1f),
                Vec3(topX + dx * 1.1f - dz * 0.22f, topY + 0.35f, dz * 1.1f + dx * 0.22f),
                Vec3(topX, topY + 0.1f, 0f),
                0.20f, 0.38f, 0.16f, spec = 0.1f
            )
        }
        return m.buildGpuMesh()
    }

    private fun buildUnitSphere(): GpuMesh {
        val m = MeshBuilder(1024)
        m.addSphere(0f, 0f, 0f, 1f, 1f, 1f, 6, 8, 1f, 1f, 1f, spec = 0.1f)
        return m.buildGpuMesh()
    }

    private fun buildTracerBeam(): GpuMesh {
        val m = MeshBuilder(512)
        m.addBox(0f, 0f, 0f, 0.04f, 0.04f, 1.6f, 1f, 0.86f, 0.42f, spec = 0.9f)
        return m.buildGpuMesh()
    }

    private fun buildBloodDecal(): GpuMesh {
        val m = MeshBuilder(512)
        // Flat crimson blood splatter disc slightly above ground
        m.addCylinderY(0f, 0.03f, 0f, 0.62f, 0.62f, 0.02f, 12, 0.48f, 0.03f, 0.04f, a = 0.88f, spec = 0.65f)
        return m.buildGpuMesh()
    }

    fun release() {
        boltRifleBody.release()
        boltHandle.release()
        smgBody.release()
        lmgBody.release()
        sniperRifleBody.release()
        revolverBody.release()
        trenchShotgunBody.release()
        mauserC96Body.release()
        stripLmgBody.release()
        fpArms.release()
        soldierTorso.release()
        soldierHeadBrodie.release()
        soldierHeadOttoman.release()
        soldierLeg.release()
        soldierArm.release()
        worldRifle.release()
        flagPole.release()
        britishFlagCloth.release()
        ottomanFlagCloth.release()
        sandbagWall.release()
        timberWall.release()
        barbedWire.release()
        stoneRuin.release()
        supplyCrate.release()
        troopShip.release()
        cityHouseLarge.release()
        cityHouseBazaar.release()
        cityTower.release()
        cityGate.release()
        marketStall.release()
        palmTree.release()
        unitSphere.release()
        tracerBeam.release()
        bloodDecal.release()
    }
}
