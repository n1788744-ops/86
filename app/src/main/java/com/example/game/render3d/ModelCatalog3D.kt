package com.example.game.render3d

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural 3D model library built with OpenGL ES 3.0 meshes.
 * Includes detailed WW1 weapons, articulated soldiers, trenches, barbed wire,
 * coastal steamship, and VFX primitives.
 */
class ModelCatalog3D {
    // First-person weapons
    val boltRifleBody: GpuMesh
    val boltHandle: GpuMesh
    val smgBody: GpuMesh
    val lmgBody: GpuMesh
    val sniperRifleBody: GpuMesh
    val revolverBody: GpuMesh
    val fpArms: GpuMesh

    // Articulated soldier meshes
    val soldierTorso: GpuMesh
    val soldierHeadAdrian: GpuMesh
    val soldierHeadStahlhelm: GpuMesh
    val soldierLeg: GpuMesh
    val soldierArm: GpuMesh
    val worldRifle: GpuMesh

    // Battlefield environment props
    val sandbagWall: GpuMesh
    val timberWall: GpuMesh
    val barbedWire: GpuMesh
    val stoneRuin: GpuMesh
    val supplyCrate: GpuMesh
    val flagPole: GpuMesh
    val flagCloth: GpuMesh
    val troopShip: GpuMesh

    // VFX primitive
    val unitSphere: GpuMesh
    val tracerBeam: GpuMesh

    init {
        boltRifleBody = buildBoltRifle(withScope = false)
        boltHandle = buildBoltHandle()
        smgBody = buildSmg()
        lmgBody = buildLmg()
        sniperRifleBody = buildBoltRifle(withScope = true)
        revolverBody = buildRevolver()
        fpArms = buildFirstPersonHands()

        soldierTorso = buildSoldierTorso()
        soldierHeadAdrian = buildSoldierHead(isEntente = true)
        soldierHeadStahlhelm = buildSoldierHead(isEntente = false)
        soldierLeg = buildSoldierLeg()
        soldierArm = buildSoldierArm()
        worldRifle = buildWorldRifle()

        sandbagWall = buildSandbagWall()
        timberWall = buildTimberWall()
        barbedWire = buildBarbedWire()
        stoneRuin = buildStoneRuin()
        supplyCrate = buildSupplyCrate()
        flagPole = buildFlagPole()
        flagCloth = buildFlagCloth()
        troopShip = buildTroopship()

        unitSphere = buildUnitSphere()
        tracerBeam = buildTracerBeam()
    }

    private fun buildBoltRifle(withScope: Boolean): GpuMesh {
        val m = MeshBuilder(8192)
        // Walnut wood colors
        val wr = 0.42f; val wg = 0.25f; val wb = 0.14f
        // Gunmetal steel colors
        val sr = 0.22f; val sg = 0.24f; val sb = 0.27f
        // Polished steel / brass accents
        val br = 0.72f; val bg = 0.58f; val bb = 0.30f

        // Buttstock & wrist
        m.addBox(0f, -0.055f, 0.28f, 0.046f, 0.085f, 0.26f, wr, wg, wb, spec = 0.18f)
        m.addBox(0f, -0.025f, 0.13f, 0.042f, 0.055f, 0.12f, wr * 0.95f, wg * 0.95f, wb * 0.95f, spec = 0.18f)
        // Brass buttplate
        m.addBox(0f, -0.055f, 0.415f, 0.047f, 0.088f, 0.015f, br, bg, bb, spec = 0.85f)

        // Forend wood stock
        m.addBox(0f, -0.015f, -0.22f, 0.044f, 0.048f, 0.56f, wr, wg, wb, spec = 0.16f)
        // Upper handguard
        m.addBox(0f, 0.012f, -0.20f, 0.036f, 0.024f, 0.44f, wr * 1.06f, wg * 1.06f, wb * 1.06f, spec = 0.16f)

        // Steel barrel bands
        m.addBox(0f, 0.002f, -0.25f, 0.048f, 0.045f, 0.022f, sr, sg, sb, spec = 0.7f)
        m.addBox(0f, 0.002f, -0.46f, 0.046f, 0.042f, 0.024f, sr, sg, sb, spec = 0.7f)

        // Receiver body
        m.addCylinderZ(0f, 0.018f, 0.02f, 0.020f, 0.020f, 0.16f, 12, sr * 1.25f, sg * 1.25f, sb * 1.25f, spec = 0.78f)
        // Trigger guard
        m.addBox(0f, -0.036f, 0.04f, 0.014f, 0.028f, 0.06f, sr, sg, sb, spec = 0.65f)
        // Trigger
        m.addBox(0f, -0.030f, 0.045f, 0.006f, 0.020f, 0.012f, br * 0.8f, bg * 0.8f, bb * 0.8f, spec = 0.8f)

        // Long rifled steel barrel
        m.addCylinderZ(0f, 0.020f, -0.38f, 0.011f, 0.015f, 0.68f, 12, sr, sg, sb, spec = 0.82f)
        // Bayonet lug under muzzle
        m.addBox(0f, -0.004f, -0.66f, 0.018f, 0.024f, 0.045f, sr, sg, sb, spec = 0.7f)
        // Rear & front iron sights
        m.addBox(0f, 0.036f, -0.11f, 0.022f, 0.014f, 0.04f, sr * 0.8f, sg * 0.8f, sb * 0.8f, spec = 0.6f)
        m.addBox(0f, 0.036f, -0.69f, 0.006f, 0.016f, 0.012f, sr * 0.8f, sg * 0.8f, sb * 0.8f, spec = 0.6f)

        if (withScope) {
            // Telescopic brass-ringed optic mounted over receiver
            m.addBox(0f, 0.042f, -0.02f, 0.018f, 0.024f, 0.02f, sr, sg, sb, spec = 0.7f)
            m.addBox(0f, 0.042f, 0.06f, 0.018f, 0.024f, 0.02f, sr, sg, sb, spec = 0.7f)
            m.addCylinderZ(0f, 0.062f, 0.01f, 0.017f, 0.015f, 0.28f, 14, 0.14f, 0.15f, 0.17f, spec = 0.85f)
            // Brass scope rings
            m.addCylinderZ(0f, 0.062f, -0.12f, 0.019f, 0.019f, 0.02f, 14, br, bg, bb, spec = 0.9f)
            m.addCylinderZ(0f, 0.062f, 0.14f, 0.017f, 0.017f, 0.02f, 14, br, bg, bb, spec = 0.9f)
        }

        return m.buildGpuMesh()
    }

    private fun buildBoltHandle(): GpuMesh {
        val m = MeshBuilder(1024)
        // Polished chrome-steel bolt body + stem + spherical bolt knob (like Screenshot #2!)
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
        // Tubular receiver & perforated cooling shroud
        m.addCylinderZ(0f, 0.018f, -0.04f, 0.022f, 0.022f, 0.24f, 12, sr, sg, sb, spec = 0.75f)
        m.addCylinderZ(0f, 0.018f, -0.28f, 0.024f, 0.024f, 0.28f, 12, sr * 1.15f, sg * 1.15f, sb * 1.15f, spec = 0.8f)
        m.addCylinderZ(0f, 0.018f, -0.45f, 0.011f, 0.011f, 0.10f, 10, sr * 0.8f, sg * 0.8f, sb * 0.8f, spec = 0.8f)
        // Side stick magazine (left side, MP-18 iconic silhouette)
        m.addBox(-0.085f, 0.012f, -0.07f, 0.14f, 0.024f, 0.036f, sr * 0.85f, sg * 0.85f, sb * 0.85f, spec = 0.65f)
        // Sights
        m.addBox(0f, 0.042f, 0.04f, 0.018f, 0.012f, 0.02f, sr, sg, sb, spec = 0.7f)
        m.addBox(0f, 0.042f, -0.40f, 0.006f, 0.014f, 0.01f, sr, sg, sb, spec = 0.7f)
        return m.buildGpuMesh()
    }

    private fun buildLmg(): GpuMesh {
        val m = MeshBuilder(4096)
        val wr = 0.40f; val wg = 0.24f; val wb = 0.13f
        val sr = 0.21f; val sg = 0.22f; val sb = 0.24f

        // Heavy buttstock & grip
        m.addBox(0f, -0.045f, 0.25f, 0.052f, 0.092f, 0.25f, wr, wg, wb, spec = 0.15f)
        m.addBox(0f, -0.010f, 0.04f, 0.058f, 0.068f, 0.24f, sr, sg, sb, spec = 0.7f)
        // Lewis massive ribbed cooling jacket
        m.addCylinderZ(0f, 0.018f, -0.34f, 0.040f, 0.042f, 0.56f, 14, sr * 0.92f, sg * 0.92f, sb * 0.92f, spec = 0.65f)
        // Barrel muzzle
        m.addCylinderZ(0f, 0.018f, -0.66f, 0.014f, 0.014f, 0.12f, 10, sr * 0.7f, sg * 0.7f, sb * 0.7f, spec = 0.8f)
        // Top 47-round pan drum magazine
        m.addCylinderY(0f, 0.068f, -0.04f, 0.095f, 0.095f, 0.038f, 18, 0.18f, 0.19f, 0.21f, spec = 0.75f)
        m.addCylinderY(0f, 0.088f, -0.04f, 0.032f, 0.032f, 0.016f, 12, 0.68f, 0.55f, 0.28f, spec = 0.85f)
        // Front sight post
        m.addBox(0f, 0.062f, -0.59f, 0.008f, 0.018f, 0.012f, sr, sg, sb, spec = 0.7f)
        return m.buildGpuMesh()
    }

    private fun buildRevolver(): GpuMesh {
        val m = MeshBuilder(2048)
        val wr = 0.36f; val wg = 0.20f; val wb = 0.10f
        val sr = 0.28f; val sg = 0.30f; val sb = 0.33f
        // Grip
        m.addBox(0f, -0.055f, 0.08f, 0.030f, 0.090f, 0.044f, wr, wg, wb, spec = 0.2f)
        // Frame
        m.addBox(0f, -0.005f, 0.02f, 0.024f, 0.048f, 0.11f, sr, sg, sb, spec = 0.82f)
        // Fluted cylinder
        m.addCylinderZ(0f, 0.014f, -0.01f, 0.024f, 0.024f, 0.052f, 12, sr * 1.15f, sg * 1.15f, sb * 1.15f, spec = 0.9f)
        // Octagonal barrel
        m.addCylinderZ(0f, 0.022f, -0.14f, 0.013f, 0.014f, 0.19f, 8, sr, sg, sb, spec = 0.85f)
        m.addBox(0f, 0.040f, -0.22f, 0.005f, 0.012f, 0.012f, sr, sg, sb, spec = 0.8f)
        return m.buildGpuMesh()
    }

    private fun buildFirstPersonHands(): GpuMesh {
        val m = MeshBuilder(2048)
        // Skin tone & wool sleeve cuff
        val kr = 0.76f; val kg = 0.58f; val kb = 0.47f
        val cr = 0.38f; val cg = 0.42f; val cb = 0.34f

        // Left supporting hand & forearm under forend
        m.addBox(-0.07f, -0.11f, -0.10f, 0.068f, 0.068f, 0.22f, cr, cg, cb, spec = 0.05f)
        m.addSphere(-0.02f, -0.035f, -0.22f, 0.036f, 0.030f, 0.046f, 8, 10, kr, kg, kb, spec = 0.12f)

        // Right trigger/bolt hand & wrist
        m.addBox(0.08f, -0.10f, 0.16f, 0.070f, 0.068f, 0.20f, cr, cg, cb, spec = 0.05f)
        m.addSphere(0.036f, -0.032f, 0.085f, 0.034f, 0.032f, 0.046f, 8, 10, kr, kg, kb, spec = 0.12f)
        return m.buildGpuMesh()
    }

    private fun buildSoldierTorso(): GpuMesh {
        val m = MeshBuilder(4096)
        // Base white/neutral wool coat so uTintColor applies uniform color cleanly!
        val coat = 0.88f
        val leatherR = 0.42f; val leatherG = 0.27f; val leatherB = 0.15f
        val brassR = 0.82f; val brassG = 0.68f; val brassB = 0.32f

        // Greatcoat torso + lower skirt
        m.addBox(0f, 1.14f, 0f, 0.42f, 0.50f, 0.25f, coat, coat, coat, spec = 0.06f)
        m.addBox(0f, 0.82f, 0f, 0.45f, 0.22f, 0.27f, coat * 0.92f, coat * 0.92f, coat * 0.92f, spec = 0.05f)
        // Collar
        m.addCylinderY(0f, 1.41f, 0f, 0.11f, 0.10f, 0.08f, 10, coat * 0.85f, coat * 0.85f, coat * 0.85f, spec = 0.05f)

        // Leather waist belt + brass buckle
        m.addBox(0f, 0.95f, 0f, 0.44f, 0.065f, 0.265f, leatherR, leatherG, leatherB, spec = 0.25f)
        m.addBox(0f, 0.95f, 0.136f, 0.07f, 0.068f, 0.016f, brassR, brassG, brassB, spec = 0.85f)

        // Front cartridge pouches
        m.addBox(-0.12f, 0.95f, 0.145f, 0.10f, 0.09f, 0.045f, leatherR * 1.1f, leatherG * 1.1f, leatherB * 1.1f, spec = 0.22f)
        m.addBox(0.12f, 0.95f, 0.145f, 0.10f, 0.09f, 0.045f, leatherR * 1.1f, leatherG * 1.1f, leatherB * 1.1f, spec = 0.22f)

        // Shoulder Y-straps
        m.addBox(-0.11f, 1.18f, 0.128f, 0.045f, 0.42f, 0.018f, leatherR, leatherG, leatherB, spec = 0.22f)
        m.addBox(0.11f, 1.18f, 0.128f, 0.045f, 0.42f, 0.018f, leatherR, leatherG, leatherB, spec = 0.22f)

        // Backpack & rolled blanket
        m.addBox(0f, 1.17f, -0.17f, 0.30f, 0.34f, 0.13f, 0.45f, 0.40f, 0.30f, spec = 0.08f)
        m.addCylinderX(0f, 1.37f, -0.16f, 0.058f, 0.34f, 10, 0.52f, 0.48f, 0.40f, spec = 0.06f)

        return m.buildGpuMesh()
    }

    private fun buildSoldierHead(isEntente: Boolean): GpuMesh {
        val m = MeshBuilder(2048)
        // Face
        m.addSphere(0f, 1.54f, 0f, 0.115f, 0.135f, 0.12f, 8, 12, 0.78f, 0.60f, 0.48f, spec = 0.12f)
        // Moustache detail for WW1 soldier charm
        m.addBox(0f, 1.51f, 0.118f, 0.075f, 0.018f, 0.018f, 0.24f, 0.18f, 0.14f, spec = 0.05f)

        if (isEntente) {
            // French M1915 Adrian steel helmet (tinted slightly darker or painted slate-blue)
            m.addSphere(0f, 1.59f, 0f, 0.138f, 0.115f, 0.148f, 8, 14, 0.34f, 0.42f, 0.50f, spec = 0.55f, maxPhiRatio = 0.58f)
            // Helmet brim
            m.addCylinderY(0f, 1.59f, 0.01f, 0.158f, 0.138f, 0.025f, 14, 0.32f, 0.40f, 0.48f, spec = 0.55f)
            // Signature Adrian top crest
            m.addBox(0f, 1.70f, 0f, 0.028f, 0.032f, 0.18f, 0.36f, 0.44f, 0.52f, spec = 0.65f)
        } else {
            // Central Powers M1916 Stahlhelm
            m.addSphere(0f, 1.59f, 0f, 0.142f, 0.112f, 0.148f, 8, 14, 0.28f, 0.30f, 0.27f, spec = 0.45f, maxPhiRatio = 0.58f)
            // Deep side skirt & visor
            m.addCylinderY(0f, 1.56f, -0.01f, 0.152f, 0.144f, 0.07f, 14, 0.26f, 0.28f, 0.25f, spec = 0.45f)
        }
        return m.buildGpuMesh()
    }

    private fun buildSoldierLeg(): GpuMesh {
        val m = MeshBuilder(1024)
        // Trouser leg (origin at hip y=0, extends down to -0.75)
        m.addBox(0f, -0.22f, 0f, 0.15f, 0.44f, 0.16f, 0.85f, 0.85f, 0.85f, spec = 0.05f)
        // Puttee wrapped calf
        m.addCylinderY(0f, -0.54f, 0f, 0.062f, 0.068f, 0.24f, 10, 0.48f, 0.45f, 0.36f, spec = 0.06f)
        // Leather combat boot
        m.addBox(0f, -0.70f, 0.03f, 0.11f, 0.10f, 0.21f, 0.22f, 0.16f, 0.12f, spec = 0.28f)
        return m.buildGpuMesh()
    }

    private fun buildSoldierArm(): GpuMesh {
        val m = MeshBuilder(1024)
        // Origin at shoulder (0,0,0), arm points down/forward
        m.addBox(0f, -0.20f, 0f, 0.11f, 0.40f, 0.11f, 0.86f, 0.86f, 0.86f, spec = 0.05f)
        m.addSphere(0f, -0.43f, 0f, 0.05f, 0.06f, 0.05f, 6, 8, 0.78f, 0.60f, 0.48f, spec = 0.12f)
        return m.buildGpuMesh()
    }

    private fun buildWorldRifle(): GpuMesh {
        val m = MeshBuilder(1024)
        m.addBox(0f, 0f, -0.2f, 0.045f, 0.065f, 0.82f, 0.42f, 0.25f, 0.14f, spec = 0.18f)
        m.addCylinderZ(0f, 0.025f, -0.42f, 0.014f, 0.018f, 0.78f, 8, 0.20f, 0.22f, 0.25f, spec = 0.8f)
        return m.buildGpuMesh()
    }

    private fun buildSandbagWall(): GpuMesh {
        val m = MeshBuilder(4096)
        // 3 courses of staggered burlap sandbags
        val rows = 3
        val cols = 4
        for (row in 0 until rows) {
            val y = 0.13f + row * 0.23f
            val offsetX = if (row % 2 == 1) 0.24f else 0f
            for (c in 0 until cols) {
                val x = -0.85f + c * 0.56f + offsetX
                val shade = 0.88f + ((row * 3 + c) % 4) * 0.04f
                m.addSphere(
                    cx = x, cy = y, cz = ((c % 2) * 0.04f - 0.02f),
                    rx = 0.29f, ry = 0.135f, rz = 0.20f,
                    stacks = 5, slices = 8,
                    r = 0.62f * shade, g = 0.56f * shade, b = 0.42f * shade,
                    spec = 0.04f
                )
            }
        }
        return m.buildGpuMesh()
    }

    private fun buildTimberWall(): GpuMesh {
        val m = MeshBuilder(2048)
        val wr = 0.34f; val wg = 0.24f; val wb = 0.15f
        // Vertical trench posts
        m.addBox(-0.95f, 0.65f, 0f, 0.14f, 1.35f, 0.14f, wr * 0.85f, wg * 0.85f, wb * 0.85f, spec = 0.06f)
        m.addBox(0f, 0.65f, 0f, 0.14f, 1.35f, 0.14f, wr * 0.85f, wg * 0.85f, wb * 0.85f, spec = 0.06f)
        m.addBox(0.95f, 0.65f, 0f, 0.14f, 1.35f, 0.14f, wr * 0.85f, wg * 0.85f, wb * 0.85f, spec = 0.06f)
        // Horizontal planks
        for (i in 0 until 5) {
            val shade = 0.90f + (i % 3) * 0.06f
            m.addBox(0f, 0.15f + i * 0.23f, 0.06f, 2.15f, 0.19f, 0.06f, wr * shade, wg * shade, wb * shade, spec = 0.05f)
        }
        return m.buildGpuMesh()
    }

    private fun buildBarbedWire(): GpuMesh {
        val m = MeshBuilder(4096)
        val wr = 0.34f; val wg = 0.25f; val wb = 0.17f
        // Crossed X-stakes at two ends
        for (sx in floatArrayOf(-0.95f, 0.95f)) {
            m.addBox(sx, 0.48f, 0f, 0.08f, 1.05f, 0.08f, wr, wg, wb, spec = 0.05f)
            m.addBox(sx, 0.44f, 0.18f, 0.07f, 0.92f, 0.07f, wr * 0.9f, wg * 0.9f, wb * 0.9f, spec = 0.05f)
            m.addBox(sx, 0.44f, -0.18f, 0.07f, 0.92f, 0.07f, wr * 0.9f, wg * 0.9f, wb * 0.9f, spec = 0.05f)
        }
        // Horizontal rusted wire strands & helical coil segments
        val ir = 0.36f; val ig = 0.32f; val ib = 0.28f
        for (y in floatArrayOf(0.25f, 0.52f, 0.78f)) {
            m.addCylinderX(0f, y, 0f, 0.015f, 2.2f, 6, ir, ig, ib, spec = 0.55f)
            m.addCylinderX(0f, y - 0.08f, 0.16f, 0.013f, 2.2f, 6, ir, ig, ib, spec = 0.55f)
            m.addCylinderX(0f, y - 0.08f, -0.16f, 0.013f, 2.2f, 6, ir, ig, ib, spec = 0.55f)
        }
        // Wire coil rings
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
        val sr = 0.46f; val sg = 0.45f; val sb = 0.42f
        // Base weathered stone wall + jagged broken parapet
        m.addBox(0f, 0.65f, 0f, 2.6f, 1.3f, 0.55f, sr, sg, sb, spec = 0.06f)
        m.addBox(-0.75f, 1.55f, 0f, 1.0f, 0.55f, 0.50f, sr * 0.94f, sg * 0.94f, sb * 0.94f, spec = 0.06f)
        m.addBox(0.85f, 1.45f, 0f, 0.75f, 0.35f, 0.48f, sr * 1.04f, sg * 1.04f, sb * 1.04f, spec = 0.06f)
        // Mossy/charred stone blocks
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

    private fun buildFlagPole(): GpuMesh {
        val m = MeshBuilder(1024)
        m.addCylinderY(0f, 2.1f, 0f, 0.05f, 0.04f, 4.2f, 10, 0.45f, 0.43f, 0.38f, spec = 0.5f)
        m.addSphere(0f, 4.25f, 0f, 0.09f, 0.09f, 0.09f, 6, 8, 0.82f, 0.68f, 0.30f, spec = 0.9f)
        return m.buildGpuMesh()
    }

    private fun buildFlagCloth(): GpuMesh {
        val m = MeshBuilder(512)
        // Neutral white cloth tinted dynamically by capture state
        m.addBox(0.65f, 3.65f, 0f, 1.25f, 0.78f, 0.04f, 0.95f, 0.95f, 0.95f, spec = 0.05f)
        return m.buildGpuMesh()
    }

    /**
     * WW1 Iron Troopship / Steamer anchored offshore (evoking user screenshot #2)
     */
    private fun buildTroopship(): GpuMesh {
        val m = MeshBuilder(4096)
        // Lower red-rust waterline hull
        m.addBox(0f, 1.2f, 0f, 26f, 2.4f, 6.2f, 0.46f, 0.19f, 0.16f, spec = 0.22f)
        // Upper dark-grey steel hull
        m.addBox(0f, 3.3f, 0f, 27.5f, 2.0f, 6.4f, 0.26f, 0.28f, 0.30f, spec = 0.35f)
        // Bow & stern tapered prow
        m.addBox(-15.5f, 2.5f, 0f, 4.5f, 3.6f, 3.4f, 0.25f, 0.27f, 0.29f, spec = 0.35f)
        m.addBox(15.2f, 2.4f, 0f, 3.8f, 3.4f, 3.8f, 0.25f, 0.27f, 0.29f, spec = 0.35f)
        // White bridge superstructure & decks
        m.addBox(0f, 5.2f, 0f, 11.5f, 2.1f, 4.6f, 0.80f, 0.79f, 0.75f, spec = 0.12f)
        m.addBox(-1.5f, 6.8f, 0f, 5.2f, 1.4f, 3.6f, 0.84f, 0.83f, 0.80f, spec = 0.15f)
        // Twin tall smokestacks (funnels)
        m.addCylinderY(-2.0f, 8.2f, 0f, 0.85f, 0.80f, 4.2f, 12, 0.22f, 0.22f, 0.24f, spec = 0.4f)
        m.addCylinderY(2.2f, 8.2f, 0f, 0.85f, 0.80f, 4.2f, 12, 0.22f, 0.22f, 0.24f, spec = 0.4f)
        // Masts
        m.addCylinderY(-9.0f, 9.0f, 0f, 0.20f, 0.10f, 10.5f, 8, 0.42f, 0.35f, 0.25f, spec = 0.15f)
        m.addCylinderY(9.0f, 8.8f, 0f, 0.20f, 0.10f, 10.0f, 8, 0.42f, 0.35f, 0.25f, spec = 0.15f)
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

    fun release() {
        boltRifleBody.release()
        boltHandle.release()
        smgBody.release()
        lmgBody.release()
        sniperRifleBody.release()
        revolverBody.release()
        fpArms.release()
        soldierTorso.release()
        soldierHeadAdrian.release()
        soldierHeadStahlhelm.release()
        soldierLeg.release()
        soldierArm.release()
        worldRifle.release()
        sandbagWall.release()
        timberWall.release()
        barbedWire.release()
        stoneRuin.release()
        supplyCrate.release()
        flagPole.release()
        flagCloth.release()
        troopShip.release()
        unitSphere.release()
        tracerBeam.release()
    }
}
