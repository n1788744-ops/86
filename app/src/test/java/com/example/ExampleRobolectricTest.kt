package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.audio.Ww1AudioEngine
import com.example.game.engine.BattleSimulation
import com.example.game.model.GameCatalog
import com.example.game.model.SoldierClass
import com.example.game.model.TheatreBiome
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
    fun `catalog contains all 6 classes and 4 story chapters`() {
        assertEquals(6, SoldierClass.entries.size)
        assertEquals(4, GameCatalog.chapters.size)
        SoldierClass.entries.forEach { sc ->
            assertTrue("Missing default weapon for ${sc.id}", GameCatalog.weapons.containsKey(sc.defaultWeaponId))
            assertTrue("Missing alt weapon for ${sc.id}", GameCatalog.weapons.containsKey(sc.altWeaponId))
        }
    }

    @Test
    fun `ray sphere hit detection and terrain trenches work`() {
        val hitDist = Math3D.raySphereDistance(
            origin = Vec3(0f, 1.5f, 10f),
            dir = Vec3(0f, 0f, -1f),
            center = Vec3(0f, 1.5f, 0f),
            radius = 0.5f
        )
        assertTrue(hitDist in 9.4f..9.6f)

        val trenchY = Math3D.terrainHeightAt(0f, 18f, TheatreBiome.MARNE_TRENCHES)
        val parapetY = Math3D.terrainHeightAt(0f, 14f, TheatreBiome.MARNE_TRENCHES)
        assertTrue("Trench floor ($trenchY) should be lower than parapet ($parapetY)", trenchY < parapetY)
    }

    @Test
    fun `battle simulation fires bolt rifle and decrements ammo`() {
        val audio = Ww1AudioEngine()
        val chapter = GameCatalog.chapters.first()
        val rifleman = SoldierClass.RIFLEMAN
        val weapon = GameCatalog.weapons[rifleman.defaultWeaponId]!!
        val sim = BattleSimulation(
            chapter = chapter,
            soldierClass = rifleman,
            weapon = weapon,
            uniform = rifleman.defaultUniform,
            perkId = "hardened",
            audio = audio
        )
        val initialAmmo = sim.ammoInMag
        sim.triggerSingleShot()
        assertEquals(initialAmmo - 1, sim.ammoInMag)
        assertTrue(sim.boltAnimTimer > 0f)
    }
}
