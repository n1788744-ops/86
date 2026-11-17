package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.audio.Ww1AudioEngine
import com.example.game.engine.BattleSimulation
import com.example.game.model.GameCatalog
import com.example.game.model.SoldierClass
import com.example.game.model.TheatreBiome
import com.example.game.render3d.BattlefieldSceneBuilder
import com.example.game.render3d.Math3D
import com.example.game.render3d.Vec3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Фронт 1916", appName)
    }

    @Test
    fun `catalog contains all 6 classes, 4 story chapters, and shop weapons`() {
        assertEquals(6, SoldierClass.entries.size)
        assertEquals(4, GameCatalog.chapters.size)
        assertTrue(GameCatalog.weapons.values.count { it.priceStars > 0 } >= 5)
        SoldierClass.entries.forEach { sc ->
            assertTrue("Missing default weapon for ${sc.id}", GameCatalog.weapons.containsKey(sc.defaultWeaponId))
            assertTrue("Missing alt weapon for ${sc.id}", GameCatalog.weapons.containsKey(sc.altWeaponId))
        }
    }

    @Test
    fun `ray sphere hit detection and city colliders work`() {
        val hitDist = Math3D.raySphereDistance(
            origin = Vec3(0f, 1.5f, 10f),
            dir = Vec3(0f, 0f, -1f),
            center = Vec3(0f, 1.5f, 0f),
            radius = 0.5f
        )
        assertTrue(hitDist in 9.4f..9.6f)

        val trenchY = Math3D.terrainHeightAt(0f, 18f, TheatreBiome.GALLIPOLI_COAST)
        val parapetY = Math3D.terrainHeightAt(0f, 14f, TheatreBiome.GALLIPOLI_COAST)
        assertTrue("Trench floor ($trenchY) should be lower than parapet ($parapetY)", trenchY < parapetY)

        val gazaMeta = BattlefieldSceneBuilder.buildMetadata(TheatreBiome.GAZA_CITY, 3)
        assertTrue("Gaza city should have building colliders", gazaMeta.colliders.size >= 16)
        assertEquals(3, gazaMeta.sectors.size)
    }

    @Test
    fun `battle simulation fires bolt rifle, decrements ammo, and supports medkits`() {
        val audio = Ww1AudioEngine()
        val chapter = GameCatalog.chapters[2] // Gaza City
        val rifleman = SoldierClass.RIFLEMAN
        val weapon = GameCatalog.weapons[rifleman.defaultWeaponId]!!
        val sim = BattleSimulation(
            chapter = chapter,
            soldierClass = rifleman,
            weapon = weapon,
            uniform = rifleman.defaultUniform,
            perkId = "hardened",
            initialMedkits = 2,
            bloodEffectsEnabled = true,
            audio = audio
        )
        val initialAmmo = sim.ammoInMag
        sim.triggerSingleShot()
        assertEquals(initialAmmo - 1, sim.ammoInMag)

        sim.health = 50f
        sim.useMedkit()
        assertEquals(95f, sim.health, 0.01f)
        assertEquals(1, sim.medkitsRemaining)
    }
}
