package com.example.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.game.model.GameCatalog
import com.example.game.model.ShopConsumable
import com.example.game.model.UniformStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(name = "front_1916_prefs")

data class PlayerProfileState(
    val level: Int = 1,
    val xp: Int = 0,
    val stars: Int = 350,
    val completedChaptersCount: Int = 0,
    val selectedClassId: String = "rifleman",
    val selectedWeaponId: String = "lee_enfield",
    val selectedUniformId: String = "british_khaki",
    val selectedPerkId: String = "hardened",
    val unlockedItemIds: Set<String> = DEFAULT_UNLOCKED_IDS,
    val medkitCount: Int = 2,
    val armorPlateCount: Int = 1,
    val ordnancePackCount: Int = 1,
    val musicVolume: Float = 0.72f,
    val sfxVolume: Float = 0.85f,
    val lookSensitivity: Float = 1.0f,
    val aimAssistEnabled: Boolean = true,
    val invertY: Boolean = false,
    val bloodEffectsEnabled: Boolean = true
) {
    val xpForNextLevel: Int get() = 400 + (level - 1) * 200

    companion object {
        val DEFAULT_UNLOCKED_IDS: Set<String> = buildSet {
            GameCatalog.weapons.values.filter { it.priceStars == 0 }.forEach { add(it.id) }
            UniformStyle.entries.filter { it.priceStars == 0 }.forEach { add(it.id) }
        }
    }
}

class GamePreferencesRepository(private val context: Context) {

    private object Keys {
        val LEVEL = intPreferencesKey("player_level")
        val XP = intPreferencesKey("player_xp")
        val STARS = intPreferencesKey("player_stars")
        val COMPLETED_CHAPTERS = intPreferencesKey("completed_chapters")
        val SELECTED_CLASS = stringPreferencesKey("selected_class")
        val SELECTED_WEAPON = stringPreferencesKey("selected_weapon")
        val SELECTED_UNIFORM = stringPreferencesKey("selected_uniform")
        val SELECTED_PERK = stringPreferencesKey("selected_perk")
        val UNLOCKED_ITEMS = stringPreferencesKey("unlocked_items_csv")
        val MEDKIT_COUNT = intPreferencesKey("medkit_count")
        val ARMOR_COUNT = intPreferencesKey("armor_count")
        val ORDNANCE_COUNT = intPreferencesKey("ordnance_count")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val LOOK_SENSITIVITY = floatPreferencesKey("look_sensitivity")
        val AIM_ASSIST = booleanPreferencesKey("aim_assist")
        val INVERT_Y = booleanPreferencesKey("invert_y")
        val BLOOD_EFFECTS = booleanPreferencesKey("blood_effects")
    }

    val profileFlow: Flow<PlayerProfileState> = context.gameDataStore.data.map { prefs ->
        val rawUnlocked = prefs[Keys.UNLOCKED_ITEMS]
        val parsedUnlocked = if (rawUnlocked.isNullOrBlank()) {
            PlayerProfileState.DEFAULT_UNLOCKED_IDS
        } else {
            PlayerProfileState.DEFAULT_UNLOCKED_IDS + rawUnlocked.split(",").filter { it.isNotBlank() }
        }
        PlayerProfileState(
            level = prefs[Keys.LEVEL] ?: 1,
            xp = prefs[Keys.XP] ?: 0,
            stars = prefs[Keys.STARS] ?: 350,
            completedChaptersCount = prefs[Keys.COMPLETED_CHAPTERS] ?: 0,
            selectedClassId = prefs[Keys.SELECTED_CLASS] ?: "rifleman",
            selectedWeaponId = prefs[Keys.SELECTED_WEAPON] ?: "lee_enfield",
            selectedUniformId = prefs[Keys.SELECTED_UNIFORM] ?: "british_khaki",
            selectedPerkId = prefs[Keys.SELECTED_PERK] ?: "hardened",
            unlockedItemIds = parsedUnlocked,
            medkitCount = prefs[Keys.MEDKIT_COUNT] ?: 2,
            armorPlateCount = prefs[Keys.ARMOR_COUNT] ?: 1,
            ordnancePackCount = prefs[Keys.ORDNANCE_COUNT] ?: 1,
            musicVolume = prefs[Keys.MUSIC_VOLUME] ?: 0.72f,
            sfxVolume = prefs[Keys.SFX_VOLUME] ?: 0.85f,
            lookSensitivity = prefs[Keys.LOOK_SENSITIVITY] ?: 1.0f,
            aimAssistEnabled = prefs[Keys.AIM_ASSIST] ?: true,
            invertY = prefs[Keys.INVERT_Y] ?: false,
            bloodEffectsEnabled = prefs[Keys.BLOOD_EFFECTS] ?: true
        )
    }

    suspend fun saveLoadout(
        classId: String,
        weaponId: String,
        uniformId: String,
        perkId: String
    ) {
        context.gameDataStore.edit { prefs ->
            prefs[Keys.SELECTED_CLASS] = classId
            prefs[Keys.SELECTED_WEAPON] = weaponId
            prefs[Keys.SELECTED_UNIFORM] = uniformId
            prefs[Keys.SELECTED_PERK] = perkId
        }
    }

    suspend fun updateSettings(
        musicVolume: Float,
        sfxVolume: Float,
        lookSensitivity: Float,
        aimAssist: Boolean,
        invertY: Boolean,
        bloodEffects: Boolean
    ) {
        context.gameDataStore.edit { prefs ->
            prefs[Keys.MUSIC_VOLUME] = musicVolume.coerceIn(0f, 1f)
            prefs[Keys.SFX_VOLUME] = sfxVolume.coerceIn(0f, 1f)
            prefs[Keys.LOOK_SENSITIVITY] = lookSensitivity.coerceIn(0.4f, 2.2f)
            prefs[Keys.AIM_ASSIST] = aimAssist
            prefs[Keys.INVERT_Y] = invertY
            prefs[Keys.BLOOD_EFFECTS] = bloodEffects
        }
    }

    suspend fun purchaseUnlockable(itemId: String, costStars: Int): Boolean {
        var success = false
        context.gameDataStore.edit { prefs ->
            val currentStars = prefs[Keys.STARS] ?: 350
            val existing = (prefs[Keys.UNLOCKED_ITEMS]?.split(",")?.filter { it.isNotBlank() }?.toSet()
                ?: emptySet()) + PlayerProfileState.DEFAULT_UNLOCKED_IDS
            if (!existing.contains(itemId) && currentStars >= costStars) {
                prefs[Keys.STARS] = currentStars - costStars
                prefs[Keys.UNLOCKED_ITEMS] = (existing + itemId).joinToString(",")
                success = true
            }
        }
        return success
    }

    suspend fun purchaseConsumable(item: ShopConsumable): Boolean {
        var success = false
        context.gameDataStore.edit { prefs ->
            val currentStars = prefs[Keys.STARS] ?: 350
            if (currentStars >= item.priceStars) {
                prefs[Keys.STARS] = currentStars - item.priceStars
                when (item) {
                    ShopConsumable.MEDKIT -> prefs[Keys.MEDKIT_COUNT] = (prefs[Keys.MEDKIT_COUNT] ?: 2) + 1
                    ShopConsumable.ARMOR_PLATE -> prefs[Keys.ARMOR_COUNT] = (prefs[Keys.ARMOR_COUNT] ?: 1) + 1
                    ShopConsumable.ORDNANCE_PACK -> prefs[Keys.ORDNANCE_COUNT] = (prefs[Keys.ORDNANCE_COUNT] ?: 1) + 1
                }
                success = true
            }
        }
        return success
    }

    suspend fun setMedkitCount(count: Int) {
        context.gameDataStore.edit { prefs ->
            prefs[Keys.MEDKIT_COUNT] = count.coerceAtLeast(0)
        }
    }

    suspend fun consumePreBattleBuffs(): Pair<Boolean, Boolean> {
        var usedArmor = false
        var usedOrdnance = false
        context.gameDataStore.edit { prefs ->
            val armor = prefs[Keys.ARMOR_COUNT] ?: 1
            if (armor > 0) {
                prefs[Keys.ARMOR_COUNT] = armor - 1
                usedArmor = true
            }
            val ord = prefs[Keys.ORDNANCE_COUNT] ?: 1
            if (ord > 0) {
                prefs[Keys.ORDNANCE_COUNT] = ord - 1
                usedOrdnance = true
            }
        }
        return usedArmor to usedOrdnance
    }

    suspend fun recordBattleReward(
        earnedXp: Int,
        earnedStars: Int,
        chapterCompletedIndex: Int?
    ) {
        context.gameDataStore.edit { prefs ->
            var level = prefs[Keys.LEVEL] ?: 1
            var xp = (prefs[Keys.XP] ?: 0) + earnedXp
            while (xp >= 400 + (level - 1) * 200) {
                xp -= (400 + (level - 1) * 200)
                level += 1
            }
            prefs[Keys.LEVEL] = level
            prefs[Keys.XP] = xp
            prefs[Keys.STARS] = (prefs[Keys.STARS] ?: 350) + earnedStars
            if (chapterCompletedIndex != null) {
                val prev = prefs[Keys.COMPLETED_CHAPTERS] ?: 0
                if (chapterCompletedIndex + 1 > prev) {
                    prefs[Keys.COMPLETED_CHAPTERS] = chapterCompletedIndex + 1
                }
            }
        }
    }
}
